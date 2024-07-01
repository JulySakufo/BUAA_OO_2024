public class MainClass {
    public static void main(String[] args) {
        Plate plate = new Plate();
        Producer producer = new Producer(plate);
        Consumer consumer = new Consumer(plate);
        producer.start();
        consumer.start();
    }
}
