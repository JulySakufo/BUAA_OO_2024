package expr;

import java.math.BigInteger;
import java.util.HashMap;

public class Method {
    public static HashMap<BigInteger, BigInteger> power(HashMap<BigInteger, BigInteger> temp,
                                                        HashMap<BigInteger, BigInteger> base,
                                                        BigInteger exp) {
        if (exp.equals(BigInteger.ZERO)) {
            HashMap<BigInteger, BigInteger> result = new HashMap<>();
            result.put(BigInteger.ZERO, BigInteger.ONE);
            return result;
        } else if (exp.equals(BigInteger.ONE)) {
            return temp;
        } else {
            HashMap<BigInteger, BigInteger> result = new HashMap<>();
            for (BigInteger e1 : temp.keySet()) {
                for (BigInteger e2 : base.keySet()) {
                    if (result.containsKey(e1.add(e2))) {
                        BigInteger addNum = temp.get(e1).multiply(base.get(e2));
                        result.put(e1.add(e2), result.get(e1.add(e2)).add(addNum));
                    } else {
                        result.put(e1.add(e2), temp.get(e1).multiply(base.get(e2)));
                    }
                }
            }
            return power(result, base, exp.subtract(BigInteger.ONE));
        }
    }

    public static HashMap<BigInteger, BigInteger> multiply(HashMap<BigInteger, BigInteger> temp,
                                                           HashMap<BigInteger, BigInteger> base) {
        HashMap<BigInteger, BigInteger> result = new HashMap<>();
        for (BigInteger e1 : temp.keySet()) {
            for (BigInteger e2 : base.keySet()) {
                if (result.containsKey(e1.add(e2))) {
                    BigInteger addNum = temp.get(e1).multiply(base.get(e2));
                    result.put(e1.add(e2), result.get(e1.add(e2)).add(addNum));
                } else {
                    result.put(e1.add(e2), temp.get(e1).multiply(base.get(e2)));
                }
            }
        }
        return result;
    }

}
