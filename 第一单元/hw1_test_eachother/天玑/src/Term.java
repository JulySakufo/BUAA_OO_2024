import java.math.BigInteger;
import java.util.ArrayList;

public class Term {
    private boolean sub;
    private ArrayList<Factor> factors;
    
    public Term() {
        this.factors = new ArrayList<>();
        this.sub = false;
    }
    
    public Term reverse() {
        this.sub = !sub;
        return this;
    }
    
    public ArrayList<Factor> getFactors() {
        return factors;
    }

    public boolean isSub() {
        return sub;
    }

    public void addFactor(Factor factor) {
        this.factors.add(factor);
    }
    
    public boolean isSimple() {
        for (Factor factor : factors) {
            if (factor.getClass().getName().equals("Mi")) {
                return false;
            }
        }
        return true;
    }
    
    public Factor getMi() {
        for (Factor factor : factors) {
            if (factor.getClass().getName().equals("Mi")) {
                return factor;
            }
        }
        return new Mi(1,new Number(BigInteger.ONE));
    }
    
    public Expr simplify() {
        Expr newExpr = new Expr();
        Factor factor = getMi();
        Mi mi = (Mi) factor;
        factors.remove(factor);
        if (mi.getCi() == 0) {
            Number number = new Number(BigInteger.ONE);
            Term newTerm = new Term();
            newTerm.addFactor((Factor) number);
            newExpr.addTerm(newTerm);
            return newExpr.mulTerm(this);
        }
        else {
            switch (mi.getDi().getClass().getName()) {
                case "Expr":
                    newExpr = ((Expr) mi.getDi()).clone().simplify();
                    mi.sub(1);
                    factors.add((Factor) mi);
                    return newExpr.mulTerm(this);
                case "Variable":
                    Term newTerm = new Term();
                    for (int i = 1;i <= mi.getCi();i += 1) {
                        newTerm.addFactor(mi.getDi());
                    }
                    newExpr.addTerm(newTerm);
                    return newExpr.mulTerm(this);
                default:
                    return new Expr();
            }
        }
    }
    
    public Term clone() {
        Term term = new Term();
        for (Factor factor : factors) {
            term.addFactor(factor.clone());
        }
        if (sub) {
            term.reverse();
        }
        return term;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Factor factor : factors) {
            sb.append(factor.toString());
            sb.append('*');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
