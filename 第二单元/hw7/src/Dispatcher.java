import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.NormalResetRequest;
import com.oocourse.elevator3.DoubleCarResetRequest;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.TimableOutput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Dispatcher extends Thread {
    private final ArrayList<RequestTable> totalRequestTable;
    private static final HashMap<Integer, ArrayList<RequestTable>> DCRequestTable = new HashMap<>();
    private int count;
    private static final boolean[] flag = new boolean[7];
    private static int lastId = -1;
    
    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest(); //乘客信息
            if (request == null) { //输入结束
                for (RequestTable requestTable : totalRequestTable) {
                    requestTable.setEndFlag(true);
                }
                try { /*TODO:修改睡眠解决线程安全的问题*/
                    sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                for (Integer key : DCRequestTable.keySet()) {
                    DCRequestTable.get(key).get(0).setEndFlag(true);
                    DCRequestTable.get(key).get(1).setEndFlag(true);
                }
                break; //结束输入流线程
            } else {
                if (request instanceof PersonRequest) {
                    dealPersonRequest(request);
                } else if (request instanceof NormalResetRequest) { //只有basic才会收到重置请求
                    dealNormalResetRequest(request);
                } else if (request instanceof DoubleCarResetRequest) {
                    dealDoubleCarResetRequest(request);
                }
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Dispatcher(ArrayList<RequestTable> totalRequestTable) {
        this.totalRequestTable = totalRequestTable;
        this.count = 0;
        for (int i = 1; i <= 6; i++) {
            ArrayList<RequestTable> arrayList = new ArrayList<>();
            RequestTable requestTable1 = new RequestTable();
            RequestTable requestTable2 = new RequestTable();
            arrayList.add(requestTable1);
            arrayList.add(requestTable2);
            DCRequestTable.put(i, arrayList);
            flag[i] = false;
        }
    }
    
    public static void addDCrequestTable(int id, ArrayList<RequestTable> requestTables) {
        DCRequestTable.put(id, requestTables);
    } //维护双轿厢电梯的侯乘表，添加进双轿厢电梯的侯乘表的过程在split进行
    
    public void dealPersonRequest(Request request) {
        int elevatorId = count % 6 + 1;
        int personId = ((PersonRequest) request).getPersonId();
        int fromFloor = ((PersonRequest) request).getFromFloor();
        int toFloor = ((PersonRequest) request).getToFloor();
        boolean direction = fromFloor < toFloor;
        Person person = new Person(personId, fromFloor, toFloor, direction);
        if (lastId == elevatorId) { //条件睡眠
            try {
                sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (!flag[elevatorId]) { //不是双轿厢电梯
            synchronized (totalRequestTable.get(elevatorId - 1)) {
                totalRequestTable.get(elevatorId - 1).addPerson(fromFloor, person);
                TimableOutput.println("RECEIVE-" + personId + "-" + elevatorId);
            }
        } else { //是双轿厢电梯，两个表都锁上
            synchronized (DCRequestTable.get(elevatorId).get(0)) {
                synchronized (DCRequestTable.get(elevatorId).get(1)) {
                    RequestTable reqA = DCRequestTable.get(elevatorId).get(0);
                    RequestTable reqB = DCRequestTable.get(elevatorId).get(1);
                    int t = DCRequestTable.get(elevatorId).get(0).getTransFloor();
                    if (direction) { //上行请求
                        if (fromFloor < t) {
                            reqA.addPerson(fromFloor, person);
                            TimableOutput.println("RECEIVE-" + personId + "-" + elevatorId + "-A");
                        } else {
                            reqB.addPerson(fromFloor, person);
                            TimableOutput.println("RECEIVE-" + personId + "-" + elevatorId + "-B");
                        }
                    } else { //下行请求
                        if (fromFloor <= t) {
                            reqA.addPerson(fromFloor, person);
                            TimableOutput.println("RECEIVE-" + personId + "-" + elevatorId + "-A");
                        } else {
                            reqB.addPerson(fromFloor, person);
                            TimableOutput.println("RECEIVE-" + personId + "-" + elevatorId + "-B");
                        }
                    }
                }
            } //加入请求即打印，开始运行
        }
        count = (count + 1) % 6;
        lastId = -1;//用过一次就行了
    }
    
    public void dealNormalResetRequest(Request request) {
        int elevatorId = ((NormalResetRequest) request).getElevatorId();
        totalRequestTable.get(elevatorId - 1).addNormalReset((NormalResetRequest) request);
    }
    
    public void dealDoubleCarResetRequest(Request request) {
        int elevatorId = ((DoubleCarResetRequest) request).getElevatorId();
        totalRequestTable.get(elevatorId - 1).addDoubleCarReset((DoubleCarResetRequest) request);
        lastId = elevatorId;
    }
    
    public static void setFlag(int id) {
        flag[id] = true;
    }
    
    public static RequestTable getRequestTable(int id, String type) {
        if (type.equals("A")) {
            return DCRequestTable.get(id).get(0);
        } else {
            return DCRequestTable.get(id).get(1);
        }
    }
}
