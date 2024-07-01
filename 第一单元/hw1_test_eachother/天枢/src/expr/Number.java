package expr;

import java.math.BigInteger;
import java.util.HashMap;

public class Number implements Factor {
    private final HashMap<BigInteger, BigInteger> value = new HashMap<>();

    public Number(BigInteger num) {
        this.value.put(BigInteger.ZERO, num);
    }

    public HashMap<BigInteger, BigInteger> getValue() {
        return value;
    }
}
