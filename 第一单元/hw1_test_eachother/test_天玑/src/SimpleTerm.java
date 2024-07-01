import java.math.BigInteger;

public class SimpleTerm {
    private Variable variable;
    private int flag;
    private BigInteger xi;
    private int ci;
    private boolean sub;
    
    public SimpleTerm() {
        this.ci = 0;
        this.xi = BigInteger.ONE;
        this.flag = 0;
        this.sub = false;
    }
    
    public void createSimpleTerm(Term term) {
        for (Factor factor : term.getFactors()) {
            if (factor.getClass().getName().equals("Number")) {
                xi = xi.multiply(((Number) factor).getNum());
            }
            else if (factor.getClass().getName().equals("Mi")) {
                Mi mi = (Mi) factor;
                Factor factor1 = mi.getDi();
                if (factor1.getClass().getName().equals("Number")) {
                    for (int i = 1;i <= mi.getCi();i += 1) {
                        xi = xi.multiply(((Number) factor1).getNum());
                    }
                }
                else if (factor1.getClass().getName().equals("Variable")) {
                    if (ci == 0) {
                        variable = (Variable) factor1.clone();
                        ci = mi.getCi();
                    }
                    else if (variable.getName().equals(((Variable) factor1).getName())) {
                        ci += mi.getCi();
                    }
                    flag = 1;
                }
            }
            else if (factor.getClass().getName().equals("Variable")) {
                if (ci == 0) {
                    variable = (Variable) factor.clone();
                    ci = 1;
                }
                else if (variable.getName().equals(((Variable) factor).getName())) {
                    ci += 1;
                }
                flag = 1;
            }
        }
        if (term.isSub()) {
            sub = !sub;
        }
    }
    
    public int getCi() {
        return ci;
    }
    
    public boolean isSub() {
        return sub;
    }
    
    public BigInteger getXi() {
        return xi;
    }
    
    public void add(SimpleTerm simpleTerm) {
        if (ci == simpleTerm.getCi()) {
            xi = xi.add(simpleTerm.getXi());
        }
    }
    
    public void substract(SimpleTerm simpleTerm) {
        if (ci == simpleTerm.getCi()) {
            xi = xi.subtract(simpleTerm.getXi());
        }
    }
    
    public String toString() {
        if (xi.signum() == -1) {
            xi = xi.negate();
            sub = !sub;
        }
        if (flag == 0) {
            return xi.toString();
        }
        else {
            StringBuilder sb = new StringBuilder();
            if (!xi.equals(BigInteger.ONE)) {
                sb.append(xi);
                sb.append('*');
                sb.append(variable.getName());
            }
            else {
                sb.append(variable.getName());
            }
            if (ci != 1) {
                sb.append('^');
                sb.append(ci);
            }
            return sb.toString();
        }
    }
    
}
