import java.math.BigInteger;
import java.util.ArrayList;

public class Term {
    private final ArrayList<Factor> factors = new ArrayList<>();
    private int sign;

    private final Calculate calculate = new Calculate();

    public Term(int sign, ArrayList<Factor> factors) {
        this.sign = sign;
        this.factors.addAll(factors);
    }

    public int getSign() {
        return sign;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }

    public Polynomial toPolynomial() {
        ArrayList<Monomial> monomials = new ArrayList<>();
        monomials.add(new Monomial(new Constant(sign, BigInteger.valueOf(1)), 0));
        Polynomial polynomial = new Polynomial(monomials);
        for (Factor factor : factors) {
            polynomial = calculate.mulPolynomial(polynomial, factor.toPolynomial());
        }
        return polynomial;
    }
}
