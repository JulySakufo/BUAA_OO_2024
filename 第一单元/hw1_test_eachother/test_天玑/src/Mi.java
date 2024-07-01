public class Mi implements Factor {
    private int ci;
    private Factor di;

    public Mi(int ci,Factor di) {
        this.ci = ci;
        this.di = di;
    }
    
    public int getCi() {
        return ci;
    }
    
    public Factor getDi() {
        return di;
    }
    
    public void sub(int i) {
        ci -= i;
    }
    
    public Factor clone() {
        Mi mi = new Mi(ci,di.clone());
        return mi;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(di.toString());
        sb.append('^');
        sb.append(ci);
        return sb.toString();
    }
}
