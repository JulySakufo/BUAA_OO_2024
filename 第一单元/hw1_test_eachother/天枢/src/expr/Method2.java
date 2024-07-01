package expr;

import java.math.BigInteger;
import java.util.HashMap;

public class Method2 {
    public static HashMap<BigInteger, BigInteger> add(HashMap<BigInteger, BigInteger> temp,
                                                      HashMap<BigInteger, BigInteger> base) {
        HashMap<BigInteger, BigInteger> result = new HashMap<>();
        for (BigInteger e1 : temp.keySet()) {
            result.put(e1, temp.get(e1));
        }
        for (BigInteger e2 : base.keySet()) {
            if (result.containsKey(e2)) {
                result.put(e2, result.get(e2).add(base.get(e2)));
            } else {
                result.put(e2, base.get(e2));
            }
        }
        return result;
    }

    public static HashMap<BigInteger, BigInteger> sub(HashMap<BigInteger, BigInteger> temp,
                                                      HashMap<BigInteger, BigInteger> base) {
        HashMap<BigInteger, BigInteger> result = new HashMap<>();
        for (BigInteger e1 : temp.keySet()) {
            result.put(e1, temp.get(e1));
        }
        for (BigInteger e2 : base.keySet()) {
            if (result.containsKey(e2)) {
                result.put(e2, result.get(e2).subtract(base.get(e2)));
            } else {
                result.put(e2, base.get(e2).negate());
            }
        }
        return result;
    }
}
