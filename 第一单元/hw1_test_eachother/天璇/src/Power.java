import java.math.BigInteger;
import java.util.ArrayList;

public class Power implements Factor {
    private final String var = "x";
    private final int index;

    private Polynomial polynomial;

    public Power(int index) {
        this.index = index;
    }

    @Override
    public Polynomial toPolynomial() {
        ArrayList<Monomial> monomials = new ArrayList<>();
        monomials.add(new Monomial(new Constant(1, BigInteger.valueOf(1)), index));
        this.polynomial = new Polynomial(monomials);
        return this.polynomial;
    }
}
