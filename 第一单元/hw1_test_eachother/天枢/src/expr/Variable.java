package expr;

import java.math.BigInteger;
import java.util.HashMap;

public class Variable implements Factor {
    private final HashMap<BigInteger, BigInteger> value = new HashMap<>();

    public Variable(BigInteger e) {
        this.value.put(e, BigInteger.ONE);
    }

    public HashMap<BigInteger, BigInteger> getValue() {
        return value;
    }
}
