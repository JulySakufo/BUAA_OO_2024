import com.oocourse.elevator2.ResetRequest;
import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class RequestTable {
    private final HashMap<Integer, ArrayList<Person>> requestMap;
    private boolean endFlag; //标志是否输入流结束，以决定是否结束所有线程
    private final ArrayList<ResetRequest> reset;
    
    //当前各层等待电梯服务的乘客。<fromFloor,乘客信息>
    public RequestTable() {
        this.requestMap = new HashMap<>();
        this.endFlag = false;
        this.reset = new ArrayList<>();
    }
    
    public synchronized void addPerson(int fromFloor, Person person) {
        if (!requestMap.containsKey(fromFloor)) {
            ArrayList<Person> arrayList = new ArrayList<>();
            arrayList.add(person);
            requestMap.put(fromFloor, arrayList);
        } else {
            ArrayList<Person> arrayList = requestMap.get(fromFloor);
            arrayList.add(person);
        }
        notifyAll(); //生产了，唤醒可能沉睡的
    }
    
    public synchronized void addReset(ResetRequest resetRequest) {
        reset.add(resetRequest);
        notifyAll();
    }
    
    public synchronized Person removePerson(int curFloor, boolean direction) {
        ArrayList<Person> arrayList = requestMap.get(curFloor);
        if (arrayList == null) {
            notifyAll();
            return null;
        }
        if (direction) { //小的先上
            arrayList.sort(Comparator.comparingInt(Person::getToFloor));
        } else { //大的先下
            arrayList.sort((a, b) -> b.getToFloor() - a.getToFloor());
        }
        if (!arrayList.isEmpty()) {
            for (Person person : arrayList) {
                if (person.getDirection() == direction) {
                    arrayList.remove(person); //只删除一次，不用迭代器
                    if (arrayList.isEmpty()) { //这个人被除去了
                        requestMap.remove(curFloor);//除去键值对
                    }
                    notifyAll();
                    return person;
                }
            }
        }
        notifyAll();
        return null;
    }
    
    public synchronized ResetRequest removeResetRequest() {
        notifyAll();
        return reset.remove(0);
    }
    
    public synchronized boolean isResetEmpty() {
        notifyAll();
        return reset.isEmpty();
    }
    
    public synchronized boolean isPersonEmpty() {
        notifyAll();
        return requestMap.isEmpty();
    }
    
    public synchronized boolean getEndFlag() {
        notifyAll();
        return this.endFlag;
    }
    
    public synchronized void setEndFlag(boolean endFlag) {
        this.endFlag = endFlag;
        notifyAll();
    }
    
    public synchronized void waitPerson() {
        try {
            wait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        notifyAll();
    }
    
    public synchronized boolean checkIn(int curFloor, boolean direction) {
        if (requestMap.containsKey(curFloor)) {
            ArrayList<Person> arrayList = requestMap.get(curFloor);
            for (Person person : arrayList) {
                if (person.getDirection() == direction) { //如果乘客有相同方向的
                    notifyAll();
                    return true;
                }
            }
        }
        notifyAll();
        return false; //相反方向不行
    }
    
    public synchronized boolean checkReversed(int curFloor, boolean direction) {
        for (Integer key : requestMap.keySet()) {
            if ((key > curFloor && direction) || (key < curFloor && !direction)) {
                notifyAll();
                return false;
            }
        }
        notifyAll();
        return true;//没有更高的楼层有需求了，掉头
    }
    
    public void printReceive(int id) {
        for (Integer key : requestMap.keySet()) {
            ArrayList<Person> arrayList = requestMap.get(key);
            for (Person person : arrayList) {
                TimableOutput.println(String.format("RECEIVE-%d-%d", person.getPersonId(), id));
            }
        }
    }
}