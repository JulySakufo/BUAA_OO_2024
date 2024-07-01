package expr;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class Term {
    private final ArrayList<Factor> factors;//因子之间是相乘关系

    public Term() {
        this.factors = new ArrayList<>();
    }

    public void addFactor(String sign, Factor factor) {
        if (sign.equals("-")) {
            factors.add(new Number(BigInteger.ZERO, new BigInteger("-1")));
        }
        factors.add(factor);
    }

    public HashMap<BigInteger, BigInteger> toPoly() {
        if (factors.size() == 1) {
            return factors.get(0).toPoly();
        } else {
            HashMap<BigInteger, BigInteger> ans =
                    polyMult(factors.get(0).toPoly(), factors.get(1).toPoly());
            for (int i = 2; i < factors.size(); i++) {
                ans = polyMult(ans, factors.get(i).toPoly());
            }
            return ans;
        }
    }

    public HashMap<BigInteger, BigInteger> polyMult(HashMap<BigInteger, BigInteger> a,
                                                    HashMap<BigInteger, BigInteger> b) {
        HashMap<BigInteger, BigInteger> ans = new HashMap<>();
        BigInteger exponent;
        BigInteger parameter;
        for (BigInteger i : a.keySet()) {
            for (BigInteger j : b.keySet()) {
                exponent = i.add(j);
                parameter = a.get(i).multiply(b.get(j));
                if (ans.containsKey(exponent)) {
                    ans.replace(exponent, parameter.add(ans.get(exponent)));
                } else {
                    ans.put(exponent, parameter);
                }
            }
        }
        return ans;
    }
}
