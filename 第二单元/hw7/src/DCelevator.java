import com.oocourse.elevator3.TimableOutput;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.Lock;

public class DCelevator extends Thread {
    private final int id;
    private final String type;
    private final int capacity;
    private int curPeople;
    private int curFloor;
    private final TransFloor transFloor; //争夺点
    private boolean direction;
    private final int waitTime;
    private RequestTable requestTable; /*TODO:双轿厢电梯初始请求发给哪个type在dispathcher决定*/
    private RequestTable otherRequestTable;
    private final HashMap<Integer, ArrayList<Person>> peopleMap;
    private DCelevator otherElevator;
    private Action action1;
    private boolean isWake; //看这个over的线程是唤醒的还是不是唤醒的
    private final ReadWriteLock requestTableLock = new ReentrantReadWriteLock();
    private final Lock readLock = requestTableLock.readLock();
    private final Lock writeLock = requestTableLock.writeLock();
    
    public DCelevator(String type, int id, TransFloor transFloor, int capacity, int waitTime) {
        this.id = id;
        this.type = type;
        this.capacity = capacity;
        this.curPeople = 0;
        this.transFloor = transFloor;
        this.curFloor = type.equals("A") ? below() : above();
        this.direction = type.equals("A");
        this.waitTime = waitTime;
        this.requestTable = new RequestTable();
        this.peopleMap = new HashMap<>();
        this.isWake = false;
    }
    
    @Override
    public void run() {
        while (true) {
            Action action;
            // 使用读写锁锁定请求表
            writeLock.lock();
            try {
                action = getAction();
                action1 = action;
                if (action == Action.WAIT) {
                    elevatorWait();
                }
            } finally {
                writeLock.unlock();
            }
            if (action1 == Action.OVER) {
                break;
            } else if (action1 == Action.OPEN) {
                elevatorOpen();
            } else if (action1 == Action.REVERSE) {
                elevatorReverse();
            } else if (action1 == Action.MOVE) {
                elevatorMove();
            }
        }
    }
    
    public Action getAction() { //双轿厢电梯不会接受到第一类重置请求和第二类重置请求
        readLock.lock();
        try {
            if (canOut() || canIn()) {
                return Action.OPEN;
            } else if (canWait()) {
                return Action.WAIT;
            } else if (canReverse()) {
                return Action.REVERSE;
            } else if (canOver()) {
                return Action.OVER;
            }
            return Action.MOVE;
        } finally {
            readLock.unlock();
        }
    }
    
    public boolean canOut() {
        if ((curFloor == transFloor.getTransFloor()) && !peopleMap.isEmpty()) {
            return true;//到了换乘楼层。并且电梯里还有乘客还没到达目的地,强制下
        }
        return peopleMap.containsKey(curFloor); //不是transFloor就看这一层有无要下即可
    }
    
    public boolean canIn() { //能否上人的判断
        if (curPeople == capacity) { //满了，不让进
            return false;
        } else { //未满
            return requestTable.checkIn(curFloor, this.direction);
        }
    }
    
    public boolean canWait() { //电梯里没人，并且没有新增乘坐需求，输入流说还可以等待
        readLock.lock();
        try {
            if (curPeople == 0 && requestTable.isPersonEmpty() && !requestTable.getEndFlag()) {
                return true;
            } else if (curPeople == 0 && requestTable.isPersonEmpty()
                    && requestTable.getEndFlag()) {
                if (!(otherRequestTable.isPersonEmpty() && otherRequestTable.getEndFlag()
                        && otherElevator.getCurPeople() == 0)) {
                    return true;
                }
            }
            return false;
        } finally {
            readLock.unlock();
        }
    }
    
    public boolean canReverse() { //电梯里没人，并且还有需求，看需求的人所在的楼层
        if (curPeople == 0 && !requestTable.isPersonEmpty()) {
            return requestTable.checkReversed(curFloor, this.direction);
        }
        return false; //惰性
    }
    
    public boolean canOver() { //电梯里没人，没新增需求，输入流告知所有输入均已结束
        readLock.lock();
        try {
            if (!isWake) {
                if (curPeople == 0 && requestTable.isPersonEmpty() && requestTable.getEndFlag()) {
                    if (otherRequestTable.isPersonEmpty() && otherRequestTable.getEndFlag()) {
                        if (otherElevator.getCurPeople() == 0) {
                            otherElevator.isWake = !otherElevator.isWake;
                            otherRequestTable.wakeMeUp();
                            return true;
                        }
                    }
                }
                return false;
            }
            return curPeople == 0 && requestTable.isPersonEmpty() && requestTable.getEndFlag();
        } finally {
            readLock.unlock();
        }
    }
    
    public void elevatorOpen() { //开门，乘客下上，关门
        TimableOutput.println("OPEN" + "-" + curFloor + "-" + id + "-" + type);
        personOut();
        try {
            sleep(400); //开关门持续时间为0.4s
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        personIn();//先下后上的逻辑，让它先沉睡一会儿，不然可能会出现先in再receive的情况
        TimableOutput.println("CLOSE" + "-" + curFloor + "-" + id + "-" + type);
    }
    
    public void elevatorReverse() { //只需要改变运行方向，没有其他变化
        direction = !direction;
    }
    
    public void elevatorMove() {
        try {
            sleep(waitTime); //花费移动一层楼的时间
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (direction) {
            if (type.equals("A")) { //A电梯，且将移动到换乘楼层，先check，B电梯是否在换乘楼层
                if (curFloor + 1 == transFloor.getTransFloor()) { //换乘电梯是新的最高点
                    synchronized (transFloor) { //换乘站是共享对象，锁住
                        curFloor++;
                        TimableOutput.println("ARRIVE" + "-" + curFloor + "-" + id + "-" + type);
                        elevatorReverse();//A到达换乘站只能向下走了
                        elevatorOpen(); //到达换乘站，下人，接人
                        try {
                            sleep(waitTime); //运行一层的时间
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        curFloor--;//离开换乘站，为另一个电梯留出空间
                        TimableOutput.println("ARRIVE" + "-" + curFloor + "-" + id + "-" + type);
                    }
                } else {
                    curFloor++;
                    TimableOutput.println("ARRIVE" + "-" + curFloor + "-" + id + "-" + type);
                }
            } else if (type.equals("B")) { //B电梯，向上移动无所谓
                curFloor++;
                TimableOutput.println("ARRIVE" + "-" + curFloor + "-" + id + "-" + type);
            }
        } else {
            if (type.equals("A")) { //A电梯，向下移动无所谓
                curFloor--;
                TimableOutput.println("ARRIVE" + "-" + curFloor + "-" + id + "-" + type);
            } else if (type.equals("B")) {
                if (curFloor - 1 == transFloor.getTransFloor()) {
                    synchronized (transFloor) { //换乘站是共享对象，锁住
                        curFloor--;
                        TimableOutput.println("ARRIVE" + "-" + curFloor + "-" + id + "-" + type);
                        elevatorReverse();//B到达换乘站只能向上走了
                        elevatorOpen();
                        try {
                            sleep(waitTime); //move的时间
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        curFloor++; //离开换乘站
                        TimableOutput.println("ARRIVE" + "-" + curFloor + "-" + id + "-" + type);
                    } //离开换乘站后释放锁
                } else {
                    curFloor--;
                    TimableOutput.println("ARRIVE" + "-" + curFloor + "-" + id + "-" + type);
                }
            }
        }
    }
    
    public void personOut() {
        if (canOut()) {
            if (curFloor == transFloor.getTransFloor()) { //是因为到达了换乘站，必须换乘了，所有更高追求的人都得下
                putDownAll();//将放下来的人加入另一部电梯的侯乘表中，让另一部电梯来接
            } else { //已经到站的
                ArrayList<Person> arrayList = peopleMap.get(curFloor);
                for (Person person : arrayList) {
                    int p = person.getPersonId();
                    TimableOutput.println("OUT-" + p + "-" + curFloor + "-" + id + "-" + type);
                }
                curPeople = curPeople - arrayList.size(); //电梯人数减少
                peopleMap.remove(curFloor);//移除键值对
            }
        }
    }
    
    public void personIn() {
        if (canIn()) {
            while (curPeople < capacity) {
                Person person = requestTable.removePerson(curFloor, direction);
                if (person == null) {
                    break;
                }
                int personId = person.getPersonId();
                int toFloor = person.getToFloor();
                curPeople++;
                if (!peopleMap.containsKey(toFloor)) {
                    ArrayList<Person> peopleList = new ArrayList<>();
                    peopleList.add(person);
                    peopleMap.put(toFloor, peopleList);
                } else {
                    ArrayList<Person> peopleList = peopleMap.get(toFloor);
                    peopleList.add(person);
                }
                TimableOutput.println("IN-" + personId + "-" + curFloor + "-" + id + "-" + type);
            }
        }
    }
    
    public synchronized void putDownAll() { //与普通电梯不同，此函数只负责放下操作
        if (!peopleMap.isEmpty()) {
            synchronized (otherRequestTable) {
                for (Integer key : peopleMap.keySet()) {
                    if (peopleMap.containsKey(key)) {
                        ArrayList<Person> arrayList = peopleMap.get(key);
                        for (Person person : arrayList) { //立即原地放下
                            int personId = person.getPersonId();
                            quickPrintOut(personId);
                            if (curFloor != key) { //如果有在当前楼能下的，直接下，不用加入另一个电梯的侯乘表里了
                                otherRequestTable.addPerson(curFloor, person); //将放下来的人加入另一轿厢的侯乘表中
                                quickPrintReceive(personId);//加入到侯乘表中立刻打印receive消息
                            }
                        }
                    }
                }
                curPeople = 0;
                peopleMap.clear();//清空电梯里的人
            }
        }
    }
    
    public void elevatorWait() {
        requestTable.waitPerson();
    }
    
    public void initializePerson(int fromFloor, Person person) { //分裂成双轿厢电梯的初始化
        requestTable.addPerson(fromFloor, person);
    }
    
    public void printReceiveDC() {
        requestTable.printReceiveDC(id, type);
    }
    
    public void addOtherRequestTable(RequestTable t) {
        this.otherRequestTable = t;
    }
    
    public RequestTable getRequestTable() { //只在初始化双轿厢电梯的时候使用1次
        return this.requestTable;
    }
    
    public void quickPrintReceive(int personId) {
        if (type.equals("A")) {
            TimableOutput.println(String.format("RECEIVE-%d-%d-B", personId, id));
        } else {
            TimableOutput.println(String.format("RECEIVE-%d-%d-A", personId, id));
        }
    }
    
    public void quickPrintOut(int personId) {
        TimableOutput.println("OUT-" + personId + "-" + curFloor + "-" + id + "-" + type);
    }
    
    public int getTransFloor() {
        return transFloor.getTransFloor();
    }
    
    public synchronized int getCurPeople() {
        //notifyAll();
        return this.curPeople;
    }
    
    public void setOtherElevator(DCelevator dcElevator) {
        this.otherElevator = dcElevator;
    }
    
    public int below() {
        return transFloor.getTransFloor() - 1;
    }
    
    public int above() {
        return transFloor.getTransFloor() + 1;
    }
    
    public void copyRequestTable(RequestTable requestTable) {
        this.requestTable = requestTable;
    }
}
