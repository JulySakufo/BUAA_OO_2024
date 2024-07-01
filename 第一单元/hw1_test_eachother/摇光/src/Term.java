import java.util.ArrayList;

public class Term {
    private final ArrayList<Factor> factorArrayList;
    private final boolean isPositive;

    public Term(ArrayList<Factor> factorArrayList,boolean isPositive) {
        this.factorArrayList = factorArrayList;
        this.isPositive = isPositive;
    }

    public Polynomial getResult() {
        if (factorArrayList.isEmpty()) {
            return new Polynomial();
        }
        else if (factorArrayList.size() == 1) {
            if (isPositive) {
                return this.factorArrayList.get(0).getResult();
            }
            else {
                Polynomial polynomial = this.factorArrayList
                    .get(0).getResult();
                return new PolyMul(
                    new Polynomial(new BasicFactor()),polynomial).getResult();
            }
        }
        else {
            Polynomial polynomial = this.factorArrayList
                .get(this.factorArrayList.size() - 1).getResult();
            return new PolyMul(removeLastFactor().getResult(), polynomial).getResult();
        }
    }

    public Term removeLastFactor() {
        this.factorArrayList.remove(this.factorArrayList.size() - 1);
        return this;
    }

}
