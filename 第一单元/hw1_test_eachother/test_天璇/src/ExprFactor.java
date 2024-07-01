import java.math.BigInteger;
import java.util.ArrayList;

public class ExprFactor implements Factor {
    private final Expr expr;
    private final int index;

    private Polynomial polynomial;

    private final Calculate calculate = new Calculate();

    public ExprFactor(Expr expr, int index) {
        this.expr = expr;
        this.index = index;
    }

    @Override
    public Polynomial toPolynomial() {
        Polynomial expPolynomial = expr.toPolynomial();
        Polynomial tempExpPolynomial;
        if (index == 0) {
            ArrayList<Monomial> monomials = new ArrayList<>();
            monomials.add(new Monomial(new Constant(1, BigInteger.valueOf(1)), 0));
            tempExpPolynomial = new Polynomial(monomials);
        } else if (index == 1) {
            tempExpPolynomial = expPolynomial;
        } else {
            tempExpPolynomial = expPolynomial;
            for (int i = 0;i < index - 1;i++) {
                tempExpPolynomial = calculate.mulPolynomial(tempExpPolynomial, expPolynomial);
            }
        }
        return tempExpPolynomial;
    }
}
