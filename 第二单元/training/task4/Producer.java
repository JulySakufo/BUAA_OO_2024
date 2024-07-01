public class Producer extends Thread {
    private final Plate plate;
    
    public Producer(Plate plate) {
        this.plate = plate;
    }
    
    @Override
    public void run() {
        int i = 1;
        while (i <= 10) {
            synchronized (plate) {
                if (plate.getPlate() != 0) {
                    try {
                        plate.wait();//等待消费了再唤醒
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                    produce(i);//生产
                    plate.notifyAll();//提醒消费者生产好了
            }
            try { //睡眠
                sleep((int) (Math.random() * 100));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            i++;
        }
    }
    public void produce(int i){
        plate.setPlate(i);
        System.out.println("Producer put:" + i);
    }
}
