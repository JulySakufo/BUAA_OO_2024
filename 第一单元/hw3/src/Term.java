import java.math.BigInteger;
import java.util.ArrayList;

public class Term {
    private int sign;
    private final ArrayList<Factor> factors;
    
    public Term(int sign) {
        this.sign = sign;
        this.factors = new ArrayList<>();
    } //<exponent,coefficient>
    
    public void addFactor(Factor factor) {
        factors.add(factor);
    }
    
    public Poly toPoly() {
        Poly poly = new Poly();
        poly.addMono(new Mono(new BigInteger("1"), new BigInteger("0")));
        for (Factor factor : factors) {
            poly = poly.mulPoly(factor.toPoly());
        }
        return poly;
    }
    
    public int getSign() {
        return sign;
    }
    
    public void negate() {
        sign = sign * (-1);//两个因子为负，整体因子为正，即保持项内有一个符号即可
    }
}

