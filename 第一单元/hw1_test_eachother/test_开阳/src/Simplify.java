import java.math.BigInteger;
import java.util.HashMap;

public class Simplify {
    public static String simplify(HashMap<BigInteger, BigInteger> poly) {
        StringBuilder ans = new StringBuilder();
        if (poly.size() == 1 && poly.containsValue(BigInteger.ZERO)) {
            ans.append("0");//特判0
        } else {
            BigInteger flag = new BigInteger("-1");//目前指数不为负数
            for (BigInteger i : poly.keySet()) {
                if (poly.get(i).compareTo(BigInteger.ZERO) > 0) {
                    ans.append(print_unit(i, poly.get(i)).substring(1));
                    flag = i;//优先输出正数
                    break;
                }
            }
            for (BigInteger i : poly.keySet()) {
                if (i.equals(flag)) {
                    continue;
                }
                ans.append(print_unit(i, poly.get(i)));
            }
        }
        return ans.toString();
    }

    public static String print_unit(BigInteger exponent, BigInteger parameter) {
        StringBuilder string = new StringBuilder();

        if (parameter.compareTo(BigInteger.ZERO) > 0) {
            string.append("+");//先判定符号
        } else {
            string.append("-");
        }

        if (parameter.equals(BigInteger.ONE) ||
                parameter.multiply(new BigInteger("-1")).equals(BigInteger.ONE)) {
            if (exponent.equals(BigInteger.ONE)) {
                string.append("x");
            } else if (exponent.compareTo(BigInteger.ONE) > 0) {
                string.append("x^");
                string.append(exponent);
            } else {
                string.append("1");
            }
            return string.toString();
        }

        string.append(parameter.abs());
        if (exponent.equals(BigInteger.ONE)) {
            string.append("*x");
        } else if (exponent.compareTo(BigInteger.ONE) > 0) {
            string.append("*x^");
            string.append(exponent);
        }
        return string.toString();
    }
}
