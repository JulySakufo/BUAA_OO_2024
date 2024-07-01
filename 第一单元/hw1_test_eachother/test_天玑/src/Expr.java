import java.util.ArrayList;
import java.util.HashMap;

public class Expr implements Factor {
    private ArrayList<Term> terms;
    private HashMap<Integer,SimpleTerm> simpleTerms;
    
    public Expr() {
        this.terms = new ArrayList<>();
        this.simpleTerms = new HashMap<>();
    }
    
    public void addTerm(Term term) {
        terms.add(term);
    }
    
    public ArrayList<Term> getTerms() {
        return terms;
    }
    
    public void add(Expr expr) {
        for (Term term : expr.getTerms()) {
            this.addTerm(term.clone());
        }
    }
    
    public Expr mulTerm(Term term) { //会更新自己
        for (Factor factor : term.getFactors()) {
            this.terms = this.mulFactor(factor).terms;
        }
        if (term.isSub()) {
            for (Term term1 : terms) {
                term1.reverse();
            }
        }
        
        return this;
    }
    
    public Expr mulFactor(Factor factor) {
        switch (factor.getClass().getName()) {
            case "Variable":
            case "Number":
                for (Term term : this.terms) {
                    term.addFactor(factor);
                }
                return this;
            case "Expr":
                Expr expr = new Expr();
                Expr expr1 = ((Expr) factor).clone().simplify();
                for (Term term : this.terms) {
                    expr.add(expr1.clone().mulTerm(term));
                }
                return expr;
            case "Mi":
                Mi mi = (Mi) factor;
                if (mi.getCi() == 0) {
                    return this;
                }
                else {
                    int i = 1;
                    for (i = 1;i <= mi.getCi();i = i + 1) {
                        this.terms = this.mulFactor(mi.getDi()).getTerms();
                    }
                    return this;
                }
            default:
                return this;
        }
    }
    
    public Expr simplify() {
        Expr expr = new Expr();
        for (Term term : terms) {
            if (!term.isSimple()) {
                expr.add(term.simplify());
            }
            else {
                expr.addTerm(term.clone());
            }
        }
        return expr;
    }
    
    public Expr clone() {
        Expr expr = new Expr();
        for (Term term : terms) {
            expr.addTerm(term.clone());
        }
        return expr;
    }
    
    public void merge() {
        for (Term term : terms) {
            SimpleTerm simpleTerm = new SimpleTerm();
            simpleTerm.createSimpleTerm(term);
            if (simpleTerms.containsKey(simpleTerm.getCi())) {
                if (simpleTerm.isSub() == simpleTerms.get(simpleTerm.getCi()).isSub()) {
                    simpleTerms.get(simpleTerm.getCi()).add(simpleTerm);
                }
                else if (simpleTerm.isSub()) {
                    simpleTerms.get(simpleTerm.getCi()).substract(simpleTerm);
                }
                else {
                    simpleTerm.substract(simpleTerms.get(simpleTerm.getCi()));
                    simpleTerms.remove(simpleTerm.getCi());
                    simpleTerms.put(simpleTerm.getCi(),simpleTerm);
                }
            }
            else {
                simpleTerms.put(simpleTerm.getCi(),simpleTerm);
            }
        }
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (SimpleTerm term : simpleTerms.values()) {
            if (term.getXi().intValue() != 0) {
                String string = term.toString();
                if (term.isSub()) {
                    sb.append('-');
                    sb.append(string);
                }
                else {
                    String str = sb.toString();
                    sb = new StringBuilder().append('+');
                    sb.append(string);
                    sb.append(str);
                }
                
            }
        }
        if (sb.toString().isEmpty()) {
            return "0";
        }
        else {
            if (sb.toString().charAt(0) == '+') {
                return sb.toString().substring(1);
            }
            return sb.toString();
        }
    }
}
