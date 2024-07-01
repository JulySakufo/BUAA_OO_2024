import java.math.BigInteger;

public class BasicFactor implements SimpleExpr {
    private final String variable;
    private final BigInteger coefficient;
    private final int index;

    public BasicFactor(String variable,BigInteger coefficient,int index) {
        this.variable = variable;
        this.coefficient = coefficient;
        this.index = index;
    }

    /*negate*/
    public BasicFactor() {
        this.variable = "x";
        this.coefficient = BigInteger.ONE.negate();
        this.index = 0;
    }

    public String getVariable() {
        return variable;
    }

    public BigInteger getCoefficient() {
        return coefficient;
    }

    public int getIndex() {
        return index;
    }

    public boolean zeroTerm() {
        return this.coefficient.equals(new BigInteger("0"));
    }

    public static boolean likeTerm(BasicFactor basicFactor1, BasicFactor basicFactor2) {
        return basicFactor1.getVariable().equals(basicFactor2.getVariable()) &&
            basicFactor1.index == basicFactor2.index;
    }

    public BasicFactor getResult() {
        return this;
    }

    public void printBasicFactor(boolean begin) {
        if (coefficient.equals(new BigInteger("0"))) {
            if (begin) {
                System.out.print(0);
            }
            return;
        }
        if (coefficient.compareTo(new BigInteger("0")) >= 0 && !begin) {
            System.out.print('+');
        }
        if (index == 0) {
            System.out.print(coefficient);
        }
        else {
            if (coefficient.compareTo(new BigInteger("1")) == 0) {
                System.out.print("x");
            }
            else if (coefficient.compareTo(new BigInteger("-1")) == 0) {
                System.out.print("-x");
            }
            else {
                System.out.print(coefficient + "*x");
            }
            if (index > 1) {
                System.out.print("^" + index);
            }
        }
    }

}
