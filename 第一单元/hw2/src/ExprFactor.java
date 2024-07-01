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
}
