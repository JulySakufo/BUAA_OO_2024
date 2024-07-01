import java.math.BigInteger;
import java.util.ArrayList;

public class Calculate {
    public Constant addConstant(Constant num1, Constant num2) {
        int resultSign = 1;
        BigInteger resultValue;
        if (num1.getSign() == num2.getSign()) {
            resultValue = num1.getValue().add(num2.getValue());
            resultSign = num1.getSign();
        } else if (num1.getValue().compareTo(num2.getValue()) == 0) {
            resultValue = new BigInteger("0");
        } else if (num1.getValue().compareTo(num2.getValue()) > 0) {
            resultValue = num1.getValue().subtract(num2.getValue());
            resultSign = num1.getSign();
        } else {
            resultValue = num2.getValue().subtract(num1.getValue());
            resultSign = num2.getSign();
        }
        return new Constant(resultSign, resultValue);
    }

    public Constant mulConstant(Constant num1, Constant num2) {
        return new Constant(num1.getSign() * num2.getSign(),
                num1.getValue().multiply(num2.getValue()));
    }

    public Polynomial addPolynomial(Polynomial polynomial, Polynomial polynomial1) {
        ArrayList<Monomial> monomials = new ArrayList<>();
        addMonoToPoly(polynomial, monomials);
        addMonoToPoly(polynomial1, monomials);
        return new Polynomial(monomials);
    }

    public void addMonoToPoly(Polynomial polynomial, ArrayList<Monomial> monomials) {
        for (int i = 0;i < polynomial.getSize();i++) {
            addMonoToMonoS(polynomial.getMonomial(i), monomials);
        }
    }

    public void addMonoToMonoS(Monomial monomial, ArrayList<Monomial> monomials) {
        int pos = findMonomialIndex(monomials, monomial.getIndex());
        if (pos == -1) {
            monomials.add(monomial);
        } else {
            Constant newConstant = addConstant(monomials.get(pos).getCoefficient(),
                    monomial.getCoefficient());
            monomials.get(pos).setCoefficient(newConstant);
        }
    }

    public Polynomial mulPolynomial(Polynomial polynomial, Polynomial polynomial1) {
        ArrayList<Monomial> monomials = new ArrayList<>();
        for (int i = 0;i < polynomial.getSize();i++) {
            for (int j = 0;j < polynomial1.getSize();j++) {
                addMonoToMonoS(mulMonomial(polynomial.getMonomial(i),
                        polynomial1.getMonomial(j)), monomials);
            }
        }
        return new Polynomial(monomials);
    }

    public Monomial mulMonomial(Monomial monomial, Monomial monomial1) {
        Monomial monomial2;
        if (monomial.getCoefficient().equalZero() || monomial1.getCoefficient().equalZero()) {
            monomial2 = new Monomial(new Constant(1, BigInteger.ZERO), 0);
        }
        else {
            monomial2 = new Monomial(mulConstant(monomial.getCoefficient(),
                    monomial1.getCoefficient()), monomial.getIndex() + monomial1.getIndex());
        }
        return monomial2;
    }

    public int findMonomialIndex(ArrayList<Monomial> monomials, int index) {
        for (int i = 0;i < monomials.size();i++) {
            if (monomials.get(i).getIndex() == index) {
                return i;
            }
        }
        return -1;
    }
}
