import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;

import java.io.IOException;
import java.util.ArrayList;

public class Dispatcher extends Thread {
    private final ArrayList<RequestTable> totalRequestTable;
    
    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            PersonRequest request = elevatorInput.nextPersonRequest(); //乘客信息
            if (request == null) { //输入结束
                for (RequestTable requestTable : totalRequestTable) {
                    requestTable.setEndFlag(true);
                }
                break; //结束输入流线程
            } else {
                int elevatorId = request.getElevatorId();
                int personId = request.getPersonId();
                int fromFloor = request.getFromFloor();
                int toFloor = request.getToFloor();
                boolean direction = fromFloor < toFloor;
                Person person = new Person(personId, fromFloor, toFloor, direction);
                totalRequestTable.get(elevatorId - 1).addPerson(fromFloor, person);//新增生产，唤醒可能沉睡的电梯
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
    }
}
