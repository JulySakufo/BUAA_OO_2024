import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

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
    
    @Override
    public Poly toPoly(BigInteger exponent) {
        Poly poly = this.toPoly();//将Expr转为多项式
        Poly resultPoly = new Poly();
        Mono mono = new Mono(new BigInteger("1"), new BigInteger("0"));
        HashMap<Poly, BigInteger> hashMap = new HashMap<>();
        hashMap.put(poly, exponent);
        mono.setExpMap(hashMap);
        resultPoly.addMono(mono);
        return resultPoly;//Poly类实现方法即可
    }
}

