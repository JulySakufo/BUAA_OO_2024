import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

public class Polynomial implements SimpleExpr {
    private final ArrayList<BasicFactor> basicFactorArrayList;

    public Polynomial(ArrayList<BasicFactor> basicFactorArrayList) {
        this.basicFactorArrayList = basicFactorArrayList;
    }

    public Polynomial(BasicFactor basicFactor) {
        /*only <x>*/
        this.basicFactorArrayList = new ArrayList<>();
        this.basicFactorArrayList.add(basicFactor);
    }

    /*default polynomial: a zero polynomial*/
    public Polynomial() {
        /*only <x>*/
        BasicFactor basicFactor = new BasicFactor("x",new BigInteger("0"),0);
        this.basicFactorArrayList = new ArrayList<>();
        this.basicFactorArrayList.add(basicFactor);
    }

    public ArrayList<BasicFactor> getBasicFactorArrayList() {
        return basicFactorArrayList;
    }

    public Polynomial removeFirstBasicFactor() {
        this.basicFactorArrayList.remove(0);
        return this;
    }

    public Polynomial getResult() {
        return this;
    }

    public void printPolynomial() {
        Iterator<BasicFactor> iterator = basicFactorArrayList.iterator();
        boolean flag = false;
        while (iterator.hasNext()) {
            BasicFactor basicFactor = iterator.next();
            if (basicFactor.getCoefficient().compareTo(new BigInteger("0")) > 0) {
                basicFactor.printBasicFactor(true);
                iterator.remove();
                flag = true;
                break;
            }
        }
        if (!flag) {
            basicFactorArrayList.get(0).printBasicFactor(true);
            basicFactorArrayList.remove(0);
        }
        for (BasicFactor basicFactor:basicFactorArrayList) {
            basicFactor.printBasicFactor(false);
        }
    }

}
