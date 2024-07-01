import java.math.BigInteger;
import java.util.HashMap;

public class Number implements Factor {
    private HashMap<BigInteger, BigInteger> hashmap;
    private final BigInteger num;
    
    public Number(BigInteger num) {
        this.num = num;
        this.hashmap = new HashMap<>();
    }
    
    public String toString() {
        return this.num.toString();
    }
    
    public BigInteger getNum() {
        return this.num;
    }
    
    @Override
    public HashMap<BigInteger, BigInteger> getHashMap() {
        return hashmap;
    }
    
    public void polyPow(int sum) {
    
    }
    
    public void negate() {
    
    }
}

