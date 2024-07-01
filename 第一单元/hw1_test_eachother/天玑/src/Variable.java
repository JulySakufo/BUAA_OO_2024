public class Variable implements Factor {
    private String name;
    
    public Variable(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public Factor clone() {
        return (Factor) new Variable(name);
    }
    
    public String toString() {
        return name;
    }
    
}
