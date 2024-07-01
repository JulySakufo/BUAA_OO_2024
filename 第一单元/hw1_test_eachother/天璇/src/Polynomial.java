import java.util.ArrayList;

public class Polynomial {
    private final ArrayList<Monomial> monomials = new ArrayList<>();

    private final Calculate calculate = new Calculate();

    public Polynomial(ArrayList<Monomial> monomials) {
        this.monomials.addAll(monomials);
    }

    public void addMonomial(Monomial monomial) {
        for (int i = 0;i < monomials.size();i++) {
            if (monomials.get(i).getIndex() == monomial.getIndex()) {
                monomials.get(i).setCoefficient(calculate.addConstant(monomial.getCoefficient(),
                        monomials.get(i).getCoefficient()));
                return;
            }
        }
        monomials.add(monomial);
    }

    public Monomial getMonomial(int pos) {
        return monomials.get(pos);
    }

    public int getSize() {
        return monomials.size();
    }

    public String toString() {
        simplifyPolynomial();
        StringBuilder sb = new StringBuilder();
        sb.append(monomials.get(0).toString());
        for (int i = 1;i < monomials.size();i++) {
            sb.append(monomials.get(i).toString());
        }
        return sb.toString();
    }

    public void simplifyPolynomial() {
        for (int i = 0;i < monomials.size();i++) {
            if (monomials.get(i).getCoefficient().equalZero()) {
                monomials.get(i).setIndex(0);
            }
        }
        ArrayList<Monomial> resultMonomials = new ArrayList<>();
        calculate.addMonoToPoly(this, resultMonomials);
        monomials.clear();
        for (Monomial monomial : resultMonomials) {
            if (monomial.getCoefficient().getSign() == 1) {
                monomials.add(monomial);
            }
        }
        for (Monomial monomial : resultMonomials) {
            if (monomial.getCoefficient().getSign() == -1) {
                monomials.add(monomial);
            }
        }
    }
}
