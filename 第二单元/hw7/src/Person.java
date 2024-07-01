public class Person {
    private final int personId;
    private final int fromFloor;
    private final int toFloor;
    private final boolean direction;
    
    public Person(int personId, int fromFloor, int toFloor, boolean direction) {
        this.personId = personId;
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;
        this.direction = direction;
    }
    
    public int getPersonId() {
        return this.personId;
    }
    
    public int getFromFloor() {
        return this.fromFloor;
    }
    
    public int getToFloor() {
        return this.toFloor;
    }
    
    public boolean getDirection() {
        return this.direction;
    }
}
