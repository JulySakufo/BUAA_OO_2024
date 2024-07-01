package expr;

import java.math.BigInteger;
import java.util.HashMap;

public interface Factor {
    public HashMap<BigInteger,BigInteger> toPoly();
}
