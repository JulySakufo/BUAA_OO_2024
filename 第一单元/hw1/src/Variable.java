import java.math.BigInteger;
import java.util.HashMap;

public class Variable implements Factor { //变量因子
    private final BigInteger coefficient;
    private final String name;
    private final BigInteger exponent;
    private final HashMap<BigInteger, BigInteger> hashmap = new HashMap<>();
    
    public Variable(BigInteger coefficient, String name, BigInteger exponent) {
        this.coefficient = coefficient;
        this.name = name;
        this.exponent = exponent;
        hashmap.put(exponent, coefficient);
    }
    
    public String getName() {
        return this.name;
    }
    
    public BigInteger getExponent() {
        return this.exponent;
    }
    
    public BigInteger getCoefficient() {
        return this.coefficient;
    }
    
    public HashMap<BigInteger, BigInteger> getHashMap() {
        return this.hashmap;
    }
    
    public void polyPow(int sum) {
    
    }
    
    public void negate() {
    
    }
}
