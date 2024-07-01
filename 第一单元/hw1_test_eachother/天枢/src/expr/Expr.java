package expr;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

//系数为0不输出
public class Expr implements Factor {
    private final ArrayList<HashMap<BigInteger, BigInteger>> terms;
    private final StringBuilder sb;
    private HashMap<BigInteger, BigInteger> value;

    public Expr() {
        this.terms = new ArrayList<>();
        this.sb = new StringBuilder();
        this.value = new HashMap<>();
    }

    public void addTerm(HashMap<BigInteger, BigInteger> term) {
        this.terms.add(term);
    }

    public void addOp(String op) {
        sb.append(op);
    }

    public HashMap<BigInteger, BigInteger> getValue() {
        String str = sb.toString();
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '+') {
                value = Method2.add(value, terms.get(i));
            } else {
                value = Method2.sub(value, terms.get(i));
            }
        }
        return value;
    }
}
