public class Monomial {
    private Constant coefficient;
    private int index;

    public Monomial(Constant coefficient, int index) {
        this.index = index;
        this.coefficient = coefficient;
    }

    public int getIndex() {
        return index;
    }

    public Constant getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(Constant coefficient) {
        this.coefficient = coefficient;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (coefficient.equalZero()) {
            sb.append("");
        } else if (index == 0) {
            sb.append(coefficient.toString());
        } else if (index == 1 && !coefficient.equalOne()) {
            sb.append(coefficient.toString());
            sb.append("*x");
        } else if (index == 1 && coefficient.equalOne()) {
            sb.append(coefficient.getSign() == 1 ? "+" : "-");
            sb.append("x");
        }
        else if (coefficient.equalOne()) {
            sb.append(coefficient.getSign() == 1 ? "+" : "-");
            sb.append("x^");
            sb.append(index);
        }
        else {
            sb.append(coefficient.toString());
            sb.append("*x^");
            sb.append(index);
        }
        return sb.toString();
    }
}
