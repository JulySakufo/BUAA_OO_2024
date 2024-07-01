import java.math.BigInteger;

public class ExprFactor implements Factor {
    private final Expr expr;
    private final int exponent;
    
    public ExprFactor(Expr expr, int exponent) {
        this.expr = expr;
        this.exponent = exponent;
    }
    
    public Poly toPoly() {
        return expr.toPoly().powPoly(exponent);
    }
    
    public Poly toPoly(BigInteger exponent) {
        Poly operatePoly = expr.toPoly();
        Mono mono = new Mono(new BigInteger(String.valueOf(exponent)),new BigInteger("0"));
        Poly operatePoly1 = new Poly();
        operatePoly1.addMono(mono);
        return operatePoly.mulPoly(operatePoly1);
    }
}
