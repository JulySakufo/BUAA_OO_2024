import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;
import java.util.HashMap;

public class Elevator extends Thread {
    private static final int MAX_PEOPLE = 6;
    private final int id;
    private int curPeople;
    private int curFloor;
    private boolean direction;
    private final RequestTable requestTable; //各有各的侯乘表
    private final HashMap<Integer, ArrayList<Person>> peopleMap;//电梯里有哪些人，到哪里去
    //<toFloor,乘客信息>
    
    @Override
    public void run() { //具体行动
        while (true) {
            Action action = getAction();
            if (action == Action.OVER) {
                break;//结束线程
            } else if (action == Action.OPEN) { //电梯开门
                open();
            } else if (action == Action.REVERSE) {
                reverse();
            } else if (action == Action.MOVE) {
                move();
            } else {
                elevatorWait();
            }
        }
    }
    
    public Elevator(RequestTable requestTable, int id) {
        this.requestTable = requestTable;
        this.id = id;
        this.curPeople = 0;
        this.curFloor = 1;
        this.direction = true; //true->up,false->down，这样不用if判断字符串,取反即可
        this.peopleMap = new HashMap<>();
    }
    
    public Action getAction() { //接下来要做什么行动
        if (canOut() || canIn()) { //有人上或下是电梯开门与否的条件
            return Action.OPEN;
        } else if (canWait()) {
            return Action.WAIT;
        } else if (canReverse()) {
            return Action.REVERSE;
        } else if (canOver()) {
            return Action.OVER;
        }
        return Action.MOVE; //上述情况均不满足，则默认需要移动
    }
    
    public boolean canOut() { //出，必须能出
        return peopleMap.containsKey(curFloor);
    }
    
    public boolean canIn() { //能否上人的判断
        if (curPeople == MAX_PEOPLE) { //满了，不让进
            return false;
        } else { //未满
            return requestTable.checkIn(curFloor, this.direction);
        }
    }
    
    public boolean canWait() { //电梯里没人，并且没有新增乘坐需求，输入流说还可以等待
        return curPeople == 0 && requestTable.isEmpty() && !requestTable.getEndFlag();
    }
    
    public boolean canReverse() { //电梯里没人，并且还有需求，看需求的人所在的楼层
        if (curPeople == 0 && !requestTable.isEmpty()) {
            return requestTable.checkReversed(curFloor, this.direction);
        }
        return false; //惰性
    }
    
    public boolean canOver() { //电梯里没人，没新增需求，输入流告知所有输入均已结束
        return curPeople == 0 && requestTable.isEmpty() && requestTable.getEndFlag();
    }
    
    public void open() { //开门，乘客下上，关门
        TimableOutput.println("OPEN" + "-" + curFloor + "-" + id);
        personOut();
        personIn();//先下后上的逻辑
        try {
            sleep(400); //开关门持续时间为0.4s
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TimableOutput.println("CLOSE" + "-" + curFloor + "-" + id);
    }
    
    public void reverse() { //只需要改变运行方向，没有其他变化
        direction = !direction;
    }
    
    public void move() {
        try {
            sleep(400); //花费0.4s到达下一楼层
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (direction) {
            curFloor++;
        } else {
            curFloor--;
        }
        TimableOutput.println("ARRIVE" + "-" + curFloor + "-" + id);
    }
    
    public void personOut() {
        if (canOut()) {
            ArrayList<Person> arrayList = peopleMap.get(curFloor);
            for (Person person : arrayList) {
                int personId = person.getPersonId();
                TimableOutput.println("OUT" + "-" + personId + "-" + curFloor + "-" + id);
            }
            curPeople = curPeople - arrayList.size(); //电梯人数减少
            peopleMap.remove(curFloor);//移除键值对
        }
    }
    
    public void personIn() {
        if (canIn()) {
            while (curPeople < MAX_PEOPLE) {
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
                TimableOutput.println("IN" + "-" + personId + "-" + curFloor + "-" + id);
            }
        }
    }
    
    public void elevatorWait() {
        requestTable.waitPerson();
    }
}
