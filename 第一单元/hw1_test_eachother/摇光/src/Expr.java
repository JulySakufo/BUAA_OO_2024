import java.util.ArrayList;

public class Expr implements Factor {
    private final ArrayList<Term> termArrayList;

    public Expr(ArrayList<Term> termArrayList) {
        this.termArrayList = termArrayList;
    }

    @Override
    public Polynomial getResult() {
        if (termArrayList.isEmpty()) {
            return new Polynomial();
        }
        else if (termArrayList.size() == 1) {
            return termArrayList.get(0).getResult();
        }
        else {
            Polynomial polynomial = this.termArrayList
                .get(this.termArrayList.size() - 1).getResult();
            return new PolyAdd(this.removeLastTerm().getResult(),
                polynomial).getResult();
        }
    }

    public Expr removeLastTerm() {
        this.termArrayList.remove(this.termArrayList.size() - 1);
        return this;
    }

}
