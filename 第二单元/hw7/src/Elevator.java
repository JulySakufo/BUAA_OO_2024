import com.oocourse.elevator3.DoubleCarResetRequest;
import com.oocourse.elevator3.NormalResetRequest;
import com.oocourse.elevator3.TimableOutput;

import java.util.ArrayList;
import java.util.HashMap;

public class Elevator extends Thread {
    private final int id;
    private int capacity; //能容纳的最大人数
    private int curPeople;
    private int curFloor;
    private boolean direction;
    private int waitTime; //运行一层的时间，即等待时间
    private final RequestTable requestTable; //各有各的侯乘表
    private final HashMap<Integer, ArrayList<Person>> peopleMap;//电梯里有哪些人，到哪里去
    //<toFloor,乘客信息>
    private Action action1;
    
    @Override
    public void run() { //具体行动
        while (true) {
            synchronized (requestTable) {
                Action action = getAction();
                action1 = action;
                if (action == Action.WAIT) {
                    elevatorWait();
                }
            }
            if (action1 == Action.OVER) {
                break;//结束线程
            } else if (action1 == Action.OPEN) { //电梯开门
                elevatorOpen();
            } else if (action1 == Action.REVERSE) {
                elevatorReverse();
            } else if (action1 == Action.MOVE) {
                elevatorMove();
            } else if (action1 == Action.NORMAL_RESET) {
                elevatorNormalReset();
            } else if (action1 == Action.DOUBLE_CAR_RESET) {
                elevatorSplit();/*TODO:想想会不会在break的空隙还给原来电梯加乘客，要不要上锁*/
                break;//变成双轿厢电梯，结束当前线程
            }
        }
    }
    
    public Elevator(RequestTable requestTable, int id) {
        this.requestTable = requestTable;
        this.id = id;
        this.curPeople = 0;
        this.curFloor = 1;
        this.waitTime = 400;
        this.capacity = 6;
        this.direction = true; //true->up,false->down，这样不用if判断字符串,取反即可
        this.peopleMap = new HashMap<>();
    }
    
    public Action getAction() { //接下来要做什么行动
        synchronized (requestTable) {
            if (canNormalReset()) { //其次是重置，有重置剩下的操作不再执行而执行重置操作
                return Action.NORMAL_RESET;
            } else if (canDoubleCarReset()) {
                return Action.DOUBLE_CAR_RESET;
            } else if (canOut() || canIn()) { //有人上或下是电梯开门与否的条件
                return Action.OPEN;
            } else if (canWait()) {
                return Action.WAIT;
            } else if (canReverse()) {
                return Action.REVERSE;
            } else if (canOver()) { //结束具有最低的优先级
                return Action.OVER;
            }
            return Action.MOVE; //上述情况均不满足，则默认需要移动
        }
    }
    
    public boolean canOut() { //出，必须能出
        return peopleMap.containsKey(curFloor);
    }
    
    public boolean canIn() { //能否上人的判断
        if (curPeople == capacity) { //满了，不让进
            return false;
        } else { //未满
            return requestTable.checkIn(curFloor, this.direction);
        }
    }
    
    public boolean canWait() { //电梯里没人，并且没有新增乘坐需求，输入流说还可以等待
        return curPeople == 0 && requestTable.isPersonEmpty() && !requestTable.getEndFlag();
    }
    
    public boolean canReverse() { //电梯里没人，并且还有需求，看需求的人所在的楼层
        if (curPeople == 0 && !requestTable.isPersonEmpty()) {
            return requestTable.checkReversed(curFloor, this.direction);
        }
        return false; //惰性
    }
    
    public boolean canOver() { //电梯里没人，没新增需求，输入流告知所有输入均已结束
        return curPeople == 0 && requestTable.isPersonEmpty() && requestTable.getEndFlag();
    }
    
    public boolean canNormalReset() { //只要有reset请求，就要执行，不管人的请求
        return !requestTable.isNormalResetEmpty();
    }
    
    public boolean canDoubleCarReset() {
        return !requestTable.isDoubleCarResetEmpty();
    }
    
    public void elevatorOpen() { //开门，乘客下上，关门
        TimableOutput.println("OPEN" + "-" + curFloor + "-" + id);
        personOut();
        try {
            sleep(400); //开关门持续时间为0.4s
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        personIn();//先下后上的逻辑，让它先沉睡一会儿，不然可能会出现先in再receive的情况
        TimableOutput.println("CLOSE" + "-" + curFloor + "-" + id);
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
                TimableOutput.println("IN" + "-" + personId + "-" + curFloor + "-" + id);
            }
        }
    }
    
    public void elevatorWait() {
        requestTable.waitPerson();
    }
    
    public void elevatorNormalReset() { //TODO:修改后的容量比原来大怎么办(采取原电梯乘坐)，小怎么办(同方向电梯check运送？)
        //强制out，条件in
        synchronized (requestTable) { //不让dispatcher使用add添加了，只让电梯进行reset操作，表不让任何人使用
            putDownAll();
            //TODO:暂时采用原地放下，原地上升的方式接送乘客，改进处：看两层内有无能到达的乘客，让乘客的等待时间尽量小
            NormalResetRequest normalResetRequest = requestTable.removeNormalResetRequest();
            this.capacity = normalResetRequest.getCapacity();
            this.waitTime = (int) (normalResetRequest.getSpeed() * 1000);
            TimableOutput.println("RESET_BEGIN-" + id);
            try {
                sleep(1200); //重置时间1.2s，sleep不释放锁
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            TimableOutput.println("RESET_END-" + id);
            requestTable.printReceive(id);//先receive，表示重新receive之前分配给该电梯的请求
            //receive之前放出的，reset过程就结束了，具体的open-in-close过程不再属于reset过程，而是reset之后要做的事情
        } //执行完后自动释放锁
    }
    
    public void elevatorSplit() {
        synchronized (requestTable) { //锁表，不让dispatcher再加乘客进来
            synchronized (Dispatcher.getRequestTable(id, "A")) {
                synchronized (Dispatcher.getRequestTable(id, "B")) { //所有相关的表全部锁起来
                    Dispatcher.setFlag(id);
                    RequestTable requestTable1 = Dispatcher.getRequestTable(id, "A");
                    RequestTable requestTable2 = Dispatcher.getRequestTable(id, "B");
                    putDownAll();
                    DoubleCarResetRequest dcResetRequest = requestTable.removeDCresetRequest();
                    int transFloor = dcResetRequest.getTransferFloor();
                    int capacity = dcResetRequest.getCapacity();
                    int waitTime = (int) (dcResetRequest.getSpeed() * 1000);
                    TransFloor trans = new TransFloor(transFloor);
                    DCelevator dcElevatorA = new DCelevator("A", id, trans, capacity, waitTime);
                    DCelevator dcElevatorB = new DCelevator("B", id, trans, capacity, waitTime);
                    dcElevatorA.copyRequestTable(requestTable1);
                    dcElevatorB.copyRequestTable(requestTable2); //使用静态方法
                    HashMap<Integer, ArrayList<Person>> requestMap = requestTable.getRequestMap();
                    for (Integer key : requestMap.keySet()) { //拷贝给双轿厢电梯
                        ArrayList<Person> arrayList = requestMap.get(key);
                        for (Person person : arrayList) {
                            boolean direction = person.getDirection();
                            if (direction) {
                                if (key < transFloor) {
                                    dcElevatorA.initializePerson(key, person);
                                } else {
                                    dcElevatorB.initializePerson(key, person);
                                }
                            } else {
                                if (key <= transFloor) { //将在换乘楼层及换乘楼层下的给A
                                    dcElevatorA.initializePerson(key, person);
                                } else { //在换乘楼层上的塞给B
                                    dcElevatorB.initializePerson(key, person);
                                }
                            }
                        }
                    }
                    dcElevatorA.addOtherRequestTable(dcElevatorB.getRequestTable());
                    dcElevatorB.addOtherRequestTable(dcElevatorA.getRequestTable());//将对方的侯乘表加入
                    dcElevatorA.getRequestTable().setTransFloor(trans);
                    dcElevatorB.getRequestTable().setTransFloor(trans);
                    dcElevatorA.setOtherElevator(dcElevatorB);
                    dcElevatorB.setOtherElevator(dcElevatorA);
                    TimableOutput.println("RESET_BEGIN-" + id);
                    try {
                        sleep(1200); //重置时间1.2s，sleep不释放锁
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    TimableOutput.println("RESET_END-" + id);
                    dcElevatorA.printReceiveDC();
                    dcElevatorB.printReceiveDC();
                    //启动双轿厢电梯
                    dcElevatorA.start();
                    dcElevatorB.start();
                }
            }
        }
    }
    
    public synchronized boolean getDirection() {
        return this.direction;
    }
    
    public synchronized int getCurFloor() {
        return this.curFloor;
    }
    
    public synchronized void putDownAll() {
        if (!peopleMap.isEmpty()) {
            TimableOutput.println("OPEN" + "-" + curFloor + "-" + id);
            for (Integer key : peopleMap.keySet()) {
                if (peopleMap.containsKey(key)) {
                    ArrayList<Person> arrayList = peopleMap.get(key);
                    for (Person person : arrayList) { //立即原地放下
                        int personId = person.getPersonId();
                        if (curFloor != key) { //如果有在当前楼能下的，直接下，不用加入侯乘表里了
                            requestTable.addPerson(curFloor, person); //将放下来的人加入侯乘表
                        }
                        TimableOutput.println("OUT-" + personId + "-" + curFloor + "-" + id);
                    }
                }
            }
            curPeople = 0;
            peopleMap.clear();//清空电梯里的人
            try {
                sleep(400); //开关门持续时间为0.4s
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            TimableOutput.println("CLOSE" + "-" + curFloor + "-" + id);
        }
    }
}
