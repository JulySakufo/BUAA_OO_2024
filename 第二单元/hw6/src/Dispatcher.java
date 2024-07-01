import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.ResetRequest;
import com.oocourse.elevator2.TimableOutput;

import java.io.IOException;
import java.util.ArrayList;

public class Dispatcher extends Thread {
    private final ArrayList<RequestTable> totalRequestTable;
    private final ArrayList<Elevator> elevators;
    private int count;
    
    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest(); //乘客信息
            if (request == null) { //输入结束
                for (RequestTable requestTable : totalRequestTable) {
                    requestTable.setEndFlag(true);
                }
                break; //结束输入流线程
            } else {
                if (request instanceof PersonRequest) {
                    int elevatorId = count % 6 + 1;
                    int personId = ((PersonRequest) request).getPersonId();
                    int fromFloor = ((PersonRequest) request).getFromFloor();
                    int toFloor = ((PersonRequest) request).getToFloor();
                    boolean direction = fromFloor < toFloor;
                    Person person = new Person(personId, fromFloor, toFloor, direction);
                    /*先验证是否处于reset状态，在打印receive的输出!似乎交换顺序就行*/
                    synchronized (totalRequestTable.get(elevatorId - 1)) {
                        totalRequestTable.get(elevatorId - 1).addPerson(fromFloor, person);
                        TimableOutput.println("RECEIVE-" + personId + "-" + elevatorId);
                    }
                    count = (count + 1) % 6;
                } else if (request instanceof ResetRequest) {
                    int elevatorId = ((ResetRequest) request).getElevatorId();
                    totalRequestTable.get(elevatorId - 1).addReset((ResetRequest) request);
                }
                
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Dispatcher(ArrayList<RequestTable> totalRequestTable, ArrayList<Elevator> elevators) {
        this.totalRequestTable = totalRequestTable;
        this.elevators = elevators;
        this.count = 0;
    }
    
    public void selectElevator(Person person) { //requestTable间接反应了elevator的状态，请求优先加入空闲的
        boolean direction = person.getDirection();
        int fromFloor = person.getFromFloor();
        for (int i = 0; i < totalRequestTable.size(); i++) {
            synchronized (elevators) {
                Elevator elevator = elevators.get(i);
                if (elevator.getDirection() && direction) { //单个电梯的捎带策略
                    if (elevator.getCurFloor() <= fromFloor) { //能够捎带
                        totalRequestTable.get(i).addPerson(fromFloor, person);
                    }
                } else if (!elevator.getDirection() && !direction) {
                    if (elevator.getCurFloor() >= fromFloor) {
                        totalRequestTable.get(i).addPerson(fromFloor, person);
                    }
                }
            }
        }
    }
}
