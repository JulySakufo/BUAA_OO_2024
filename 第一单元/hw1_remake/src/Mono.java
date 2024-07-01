import java.math.BigInteger;

public class Mono { //最小单元a*x^b*exp(Factor)^c
    private final BigInteger coefficient;
    private final BigInteger exponent;
    private Factor factor;
    private BigInteger expExponent;
    
    public Mono(BigInteger coefficient, BigInteger exponent) {
        this.coefficient = coefficient;
        this.exponent = exponent;
    }
    
    public BigInteger getCoefficient() {
        return coefficient;
    }
    
    public BigInteger getExponent() {
        return exponent;
    }
}
