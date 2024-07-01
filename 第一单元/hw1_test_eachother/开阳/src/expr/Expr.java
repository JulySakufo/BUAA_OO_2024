package expr;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class Expr implements Factor {
    private final ArrayList<Term> terms;//项之间是相加关系

    public Expr() {
        this.terms = new ArrayList<>();
    }

    public void addTerm(String sign, Term term) {
        if (sign.equals("-")) {
            term.addFactor("-", new Number(BigInteger.ZERO, BigInteger.ONE));//指数在前，系数在后
        }
        this.terms.add(term);
    } //还没改，项有正负

    public HashMap<BigInteger, BigInteger> toPoly() {
        HashMap<BigInteger, BigInteger> ans = new HashMap<>();
        HashMap<BigInteger, BigInteger> tmp;
        for (int i = 0; i < terms.size(); i++) {
            tmp = terms.get(i).toPoly();
            for (BigInteger j : tmp.keySet()) {
                if (ans.containsKey(j)) {
                    ans.replace(j, ans.get(j).add(tmp.get(j)));
                } else {
                    ans.put(j, tmp.get(j));
                }
            }
        }
        tmp = new HashMap<>(ans);//深浅拷贝
        for (BigInteger i : tmp.keySet()) {
            if (ans.get(i).equals(BigInteger.ZERO)) {
                ans.remove(i);//去掉为0的项
            }
        }
        if (ans.size() == 0) {
            ans.put(BigInteger.ZERO, BigInteger.ZERO);//全空则输出0
        }
        return ans;
    }

}
