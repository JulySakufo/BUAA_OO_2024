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
        Poly poly = factor.toPoly(exponent);//将ExprFactor转为多项式
        return poly;
    }
    
    @Override
    public Poly toPoly(BigInteger sum) {
        return null;
    }
}
