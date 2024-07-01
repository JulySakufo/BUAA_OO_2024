import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

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
        Mono mono = new Mono(coefficient, exponent);
        ArrayList<Mono> monolist = new ArrayList<>();
        monolist.add(mono);
        poly.setMonolist(monolist);
        return poly;
    }
}
