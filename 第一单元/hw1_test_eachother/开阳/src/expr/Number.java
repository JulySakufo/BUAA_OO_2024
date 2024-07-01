package expr;

import java.math.BigInteger;
import java.util.HashMap;

public class Number implements Factor {
    //private final BigInteger num;
    private final BigInteger parameter;
    private final BigInteger exponent;

    public Number(BigInteger exponent, BigInteger parameter) {
        this.parameter = parameter;
        this.exponent = exponent;
    }

    public HashMap<BigInteger, BigInteger> toPoly() {
        HashMap<BigInteger, BigInteger> poly = new HashMap<>();
        poly.put(exponent, parameter);
        return poly;
    }

    public BigInteger get_para() {
        return parameter;
    }

    public BigInteger get_exp() {
        return exponent;
    }

}
