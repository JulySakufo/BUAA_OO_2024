package factor;

import java.math.BigInteger;

public class Polynome {
    private String polyName;
    private BigInteger polyPower;

    public Polynome(String polyName, BigInteger polyPower) {
        this.polyName = polyName;
        this.polyPower = polyPower;
    }

    public Polynome CopyPoly() {
        return new Polynome(this.polyName, this.polyPower);
    }

    public boolean IfSamePoly(Polynome otherPoly) {
        return this.polyName.equals(otherPoly.polyName)
                && this.polyPower.equals(otherPoly.polyPower);
    }

    public void MulPoly(BigInteger otherPolyPower) {
        this.polyPower = this.polyPower.add(otherPolyPower);
    }

    public BigInteger GetPolyPower() {
        return this.polyPower;
    }

    public String GetPolyName() {
        return this.polyName;
    }

    public String toString() {
        if (polyPower.equals(BigInteger.ZERO)) {
            return "1";
        } else if (polyPower.equals(BigInteger.ONE)) {
            return polyName;
        } else {
            return polyName + "^" + polyPower;
        }
    }
}
