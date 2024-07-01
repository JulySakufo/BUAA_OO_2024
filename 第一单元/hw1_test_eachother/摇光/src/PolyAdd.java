public class PolyAdd {
    private final Polynomial left;
    private final Polynomial right;
    
    public PolyAdd(Polynomial left,Polynomial right) {
        this.left = left;
        this.right = right;
    }
    
    public Polynomial getResult() {
        if (right.getBasicFactorArrayList().size() == 1) {
            return new BasicAdd(left,right.getBasicFactorArrayList().get(0)).getResult();
        } else {
            return new PolyAdd(new BasicAdd(left,right.getBasicFactorArrayList().get(0)).getResult()
                ,right.removeFirstBasicFactor()).getResult();
        }
    }
}
