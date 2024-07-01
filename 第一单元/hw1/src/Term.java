import java.math.BigInteger;
import java.util.HashMap;

public class Term {
    private final HashMap<BigInteger, BigInteger> factors;
    
    public Term() {
        this.factors = new HashMap<>();
        factors.put(new BigInteger("0"), new BigInteger("1"));
    } //<exponent,coefficient>
    
    public void addFactor(Factor factor) {
        HashMap<BigInteger, BigInteger> hashmap = factor.getHashMap();
        HashMap<BigInteger, BigInteger> resultMap = new HashMap<>();
        for (BigInteger exponent : factors.keySet()) {
            BigInteger coefficient = factors.get(exponent);
            for (BigInteger exponent1 : hashmap.keySet()) {
                BigInteger coefficient1 = hashmap.get(exponent1);
                BigInteger exponent2 = exponent.add(exponent1);
                BigInteger coefficient2 = coefficient.multiply(coefficient1);
                if (resultMap.containsKey(exponent2)) {
                    resultMap.replace(exponent2, coefficient2.add(resultMap.get(exponent2)));
                } else {
                    resultMap.put(exponent2, coefficient2);
                }
            }
        }
        factors.clear();
        for (BigInteger exponent : resultMap.keySet()) {
            factors.put(exponent, resultMap.get(exponent));
        }
    }
    
    public HashMap<BigInteger, BigInteger> getHashMap() {
        return factors;
    }
    
    public void polyPow(int sum) {
    
    }
    
    public Term negate() {
        for (BigInteger key : factors.keySet()) {
            factors.replace(key, factors.get(key).multiply(new BigInteger("-1")));
        }
        return this;
    }
}

