import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

public class BasicAdd implements Add {
    private final Polynomial left;
    private final BasicFactor right;

    public BasicAdd(Polynomial left,BasicFactor right) {
        this.left = left;
        this.right = right;
    }

    public Polynomial getResult() {
        ArrayList<BasicFactor> basicFactorArrayList = left.getBasicFactorArrayList();
        Iterator<BasicFactor> iterator = basicFactorArrayList.iterator();
        while (iterator.hasNext()) {
            BasicFactor basicFactorInPoly = iterator.next();
            if (BasicFactor.likeTerm(right,basicFactorInPoly)) {
                String variable = right.getVariable();
                BigInteger coefficient = right.getCoefficient().add(
                    basicFactorInPoly.getCoefficient());
                int index = right.getIndex();
                BasicFactor newBasicFactor = new BasicFactor(variable,coefficient,index);
                iterator.remove();
                basicFactorArrayList.add(newBasicFactor);
                return new Polynomial(basicFactorArrayList);
            }
            else if (basicFactorInPoly.zeroTerm()) {
                iterator.remove();
            }
        }
        basicFactorArrayList.add(right);
        return new Polynomial(basicFactorArrayList);
    }

}
