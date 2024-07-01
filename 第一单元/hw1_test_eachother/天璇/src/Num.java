import java.math.BigInteger;
import java.util.ArrayList;

public class Num implements Factor {
    private final Constant coefficient;

    public Num(String num, int sign) {
        this.coefficient = new Constant(sign, new BigInteger(num));
    }

    @Override
    public Polynomial toPolynomial() {
        ArrayList<Monomial> monomials = new ArrayList<>();
        monomials.add(new Monomial(coefficient, 0));
        return new Polynomial(monomials);
    }
}
