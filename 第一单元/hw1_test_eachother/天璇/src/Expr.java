import java.util.ArrayList;

public class Expr {
    private final ArrayList<Term> terms = new ArrayList<>();

    private final Calculate calculate = new Calculate();

    public Expr(ArrayList<Term> terms) {
        this.terms.addAll(terms);
    }

    public Polynomial toPolynomial() {
        ArrayList<Monomial> monomials = new ArrayList<>();
        Polynomial polynomial = new Polynomial(monomials);
        for (Term term : terms) {
            polynomial = calculate.addPolynomial(polynomial, term.toPolynomial());
        }
        return polynomial;
    }
}
