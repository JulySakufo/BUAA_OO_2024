public class PolyMul implements Mul {
    private final Polynomial left;
    private final Polynomial right;

    public PolyMul(Polynomial left,Polynomial right) {
        this.left = left;
        this.right = right;
    }

    public Polynomial getResult() {
        Polynomial polynomial = new Polynomial();
        for (BasicFactor basicFactor1:left.getBasicFactorArrayList()) {
            for (BasicFactor basicFactor2:right.getBasicFactorArrayList()) {
                polynomial = new BasicAdd(
                    polynomial,new BasicMul(basicFactor1,basicFactor2).getResult())
                    .getResult();
            }
        }
        return polynomial;
    }

}
