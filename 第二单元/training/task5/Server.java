import java.util.ArrayList;

public class Server implements Observerable {
    private final ArrayList<Observer> arrayList;
    
    public Server() {
        this.arrayList = new ArrayList<>();
    }
    
    @Override
    public void addObserver(Observer observer) {
        arrayList.add(observer);
    }
    
    @Override
    public void removeObserver(Observer observer) {
        arrayList.remove(observer);
    }
    
    @Override
    public void notifyObserver(String msg) {
        new User("server").update(msg);
        for (Observer observer : arrayList) {
            observer.update(msg);
        }
    }
}
