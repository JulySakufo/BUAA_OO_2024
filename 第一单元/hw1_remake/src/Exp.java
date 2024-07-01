import java.math.BigInteger;
import java.util.HashMap;

public class Exp implements Factor {
    private final Factor factor;
    private final BigInteger exponent;
    
    public Exp(Factor factor, BigInteger exponent) {
        this.factor = factor;
        this.exponent = exponent;
    }
    
    @Override
    public Poly toPoly() {
        return null;
    }
}
