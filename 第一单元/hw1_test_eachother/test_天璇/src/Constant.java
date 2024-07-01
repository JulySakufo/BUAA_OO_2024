import java.math.BigInteger;

public class Constant {
    // the sign and the absolute value of the coefficient
    private final int sign;
    private final BigInteger value;

    public Constant(int sign, BigInteger value) {
        this.sign = sign;
        this.value = value;
    }

    public int getSign() {
        return sign;
    }

    public BigInteger getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (sign == 1) {
            sb.append("+");
        } else {
            sb.append("-");
        }
        sb.append(value.toString());
        return sb.toString();
    }

    public boolean equalZero() {
        return (value.compareTo(BigInteger.ZERO) == 0);
    }

    public boolean equalOne() {
        return (value.compareTo(BigInteger.ONE) == 0);
    }
}
