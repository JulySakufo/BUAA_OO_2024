import java.math.BigInteger;

public class Number implements Factor {
    private BigInteger num;
    
    public Number(BigInteger num) {
        this.num = num;
    }
    
    public String toString() {
        return this.num.toString();
    }
    
    public BigInteger getNum() {
        return num;
    }
    
    public Factor clone() {
        return (Factor) new Number(num);
    }
}