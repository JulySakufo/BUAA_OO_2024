import java.math.BigInteger;
import java.util.HashMap;

public class Printer {
    public static void printer(HashMap<BigInteger, BigInteger> expr) {
        int flag = 0;
        for (BigInteger e : expr.keySet()) {
            if (expr.get(e).compareTo(BigInteger.ZERO) > 0) {
                flag = 1;
                if (e.equals(BigInteger.ZERO)) {
                    System.out.print(expr.get(e));
                } else if (e.equals(BigInteger.ONE)) {
                    if (!expr.get(e).equals(BigInteger.ONE)) {
                        System.out.print(expr.get(e));
                        System.out.print("*");
                    }
                    System.out.print("x");
                } else {
                    if (!expr.get(e).equals(BigInteger.ONE)) {
                        System.out.print(expr.get(e));
                        System.out.print("*");
                    }
                    System.out.print("x^");
                    System.out.print(e);
                }
                expr.remove(e);
                break;
            }
        }
        for (BigInteger e : expr.keySet()) {
            if (!expr.get(e).equals(BigInteger.ZERO)) {
                flag = 1;
                if (e.equals(BigInteger.ZERO)) {
                    System.out.print(expr.get(e));
                } else if (e.equals(BigInteger.ONE)) {
                    if (expr.get(e).compareTo(BigInteger.ZERO) > 0) {
                        System.out.print("+");
                    }
                    if (!expr.get(e).equals(BigInteger.ONE)) {
                        System.out.print(expr.get(e));
                        System.out.print("*");
                    }
                    System.out.print("x");
                } else {
                    if (expr.get(e).compareTo(BigInteger.ZERO) > 0) {
                        System.out.print("+");
                    }
                    if (!expr.get(e).equals(BigInteger.ONE)) {
                        System.out.print(expr.get(e));
                        System.out.print("*");
                    }
                    System.out.print("x^");
                    System.out.print(e);
                }
            }
        }
        if (flag == 0) {
            System.out.print(0);
        }
    }
}
