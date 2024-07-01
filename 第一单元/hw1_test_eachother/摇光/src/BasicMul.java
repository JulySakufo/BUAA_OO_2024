public class BasicMul implements Mul {
    private final BasicFactor left;
    private final BasicFactor right;

    public BasicMul(BasicFactor left,BasicFactor right) {
        this.left = left;
        this.right = right;
    }
    
    @Override
    public BasicFactor getResult() {
        //in homework 1, there is only one variable <x>
        return new BasicFactor(left.getVariable(),
            left.getCoefficient().multiply(right.getCoefficient()),
            left.getIndex() + right.getIndex());
    }
    
}
