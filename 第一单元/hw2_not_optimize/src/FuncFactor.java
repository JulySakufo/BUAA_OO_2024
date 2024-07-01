import java.math.BigInteger;
import java.util.ArrayList;

public class FuncFactor implements Factor {
    private String function;//将函数实参带入形参位置后的结果(字符串形式)
    private Expr expr;//将function解析成表达式后的结果
    
    public FuncFactor(String name, ArrayList<Factor> realParameters) {
        this.function = Definer.callFunction(name, realParameters);
        this.expr = setExpr();
    }
    
    public Expr setExpr() {
        Lexer lexer = new Lexer(function);
        Parser parser = new Parser(lexer);
        return parser.parseExpr();
    }
    
    public Expr getExpr() {
        return expr;
    }
    
    @Override
    public Poly toPoly() {
        return expr.toPoly();
    }
    
    @Override
    public Poly toPoly(BigInteger exponent) {
        return null;
    }
}
