import java.math.BigInteger;

public class VariableFactor implements Factor {
    private final String variable;
    private final BigInteger coefficient;
    private final int index;

    public VariableFactor(String variable,int index) {
        this.variable = variable;
        this.coefficient = BigInteger.ONE;
        this.index = index;
    }

    public VariableFactor(BigInteger coefficient) {
        /*default*/
        this.variable = "x";
        this.coefficient = coefficient;
        this.index = 0;
    }

    @Override
    public Polynomial getResult() {
        return new Polynomial(new BasicFactor(variable,coefficient,index));
    }

}
