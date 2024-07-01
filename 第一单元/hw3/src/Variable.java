import java.math.BigInteger;

public class Variable implements Factor { //变量因子
    private final BigInteger coefficient;
    private final String name;
    private final BigInteger exponent;
    
    public Variable(BigInteger coefficient, String name, BigInteger exponent) {
        this.coefficient = coefficient;
        this.name = name;
        this.exponent = exponent;
    }
    
    public Poly toPoly() {
        Poly poly = new Poly();
        poly.addMono(new Mono(coefficient, exponent));
        return poly;
    }
}
