package expr;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class Term {
    private final ArrayList<HashMap<BigInteger, BigInteger>> factors;
    private HashMap<BigInteger, BigInteger> value;

    public Term() {
        this.factors = new ArrayList<>();
        this.value = new HashMap<>();
    }

    public void addFactor(HashMap<BigInteger, BigInteger> factor) {
        this.factors.add(factor);
    }

    public HashMap<BigInteger, BigInteger> getValue() {
        for (BigInteger e1 : factors.get(0).keySet()) {
            value.put(e1, factors.get(0).get(e1));
        }
        for (int i = 1; i < factors.size(); i++) {
            value = Method.multiply(value, factors.get(i));
        }
        return value;
    }
}
