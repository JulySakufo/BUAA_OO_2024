import com.oocourse.elevator1.TimableOutput;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp(); //初始化时间戳
        ArrayList<RequestTable> totalRequestTable = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            RequestTable requestTable = new RequestTable(); //每个电梯有自己的侯乘表
            totalRequestTable.add(requestTable);
            Elevator elevator = new Elevator(requestTable, i); //初始化电梯线程
            elevator.start(); //启动电梯线程
        }
        Dispatcher dispatcher = new Dispatcher(totalRequestTable);
        dispatcher.start();//启动调度线程,模仿生产者-消费者模型
    }
}