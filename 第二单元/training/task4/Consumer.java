public class Consumer extends Thread {
    private final Plate plate;
    
    public Consumer(Plate plate) {
        this.plate = plate;
    }
    
    @Override
    public void run() {
        int count = 1;
        while (count <= 10) {
            synchronized (plate) {
                if (plate.getPlate() == 0) {
                    try {
                        plate.wait();//等待生产
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                consume();//生产完了就消费
                plate.notifyAll();//消费完了提醒生产者
            }
            count++;
        }
    }
    
    public void consume() { //取走货物
        System.out.println("Consumer get:" + plate.getPlate());
        plate.setPlate(0);
    }
}
