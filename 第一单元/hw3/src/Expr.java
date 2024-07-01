import java.util.ArrayList;

public class Expr implements Factor {
    private final ArrayList<Term> terms;
    
    public Expr() {
        this.terms = new ArrayList<>();
    }
    
    public void addTerm(Term term) {
        terms.add(term);
    }
    
    public Poly toPoly() {
        Poly poly = new Poly();
        for (Term term : terms) {
            int sign = term.getSign();
            if (sign == -1) {
                poly = poly.addPoly(term.toPoly().negate());
            } else {
                poly = poly.addPoly(term.toPoly());
            }
        }
        return poly;
    }
}

