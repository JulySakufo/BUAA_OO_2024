import java.math.BigInteger;

public class Exp implements Factor {
    private final Factor factor;
    private final BigInteger exponent;
    
    public Exp(Factor factor, BigInteger exponent) {
        this.factor = factor;
        this.exponent = exponent;
    }
    
    @Override
    public Poly toPoly() {
        return factor.toPoly().expMulPoly(exponent);
    }
}
