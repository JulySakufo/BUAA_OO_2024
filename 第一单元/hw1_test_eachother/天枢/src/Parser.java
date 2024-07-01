import expr.Expr;
import expr.Variable;
import expr.Term;
import expr.Number;
import expr.Method;
import java.math.BigInteger;
import java.util.HashMap;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public HashMap<BigInteger, BigInteger> parseExpr() {
        Expr expr = new Expr();
        if (lexer.peek().equals("+") || lexer.peek().equals("-")) {
            expr.addOp(lexer.peek());
            lexer.next();
        } else {
            expr.addOp("+");
        }
        expr.addTerm(parseTerm());

        while (lexer.peek().equals("+") || lexer.peek().equals("-")) {
            expr.addOp(lexer.peek());
            lexer.next();
            expr.addTerm(parseTerm());
        }
        return expr.getValue();
    }

    public HashMap<BigInteger, BigInteger> parseTerm() {
        Term term = new Term();
        term.addFactor(parseFactor());

        while (lexer.peek().equals("*")) {
            lexer.next();
            term.addFactor(parseFactor());
        }
        return term.getValue();
    }

    public HashMap<BigInteger, BigInteger> parseFactor() {
        if (lexer.peek().equals("x")) {
            lexer.next();
            if (lexer.peek().equals("^")) {
                lexer.next();
                BigInteger e = lexer.getNumber();
                return new Variable(e).getValue();
            }
            return new Variable(BigInteger.ONE).getValue();
        }
        if (lexer.peek().equals("(")) {
            lexer.next();
            HashMap<BigInteger, BigInteger> expr = parseExpr();
            lexer.next();
            if (lexer.peek().equals("^")) {
                lexer.next();
                BigInteger e = lexer.getNumber();
                expr = Method.power(expr, expr, e);
            }
            return expr;
        } else {
            BigInteger num = lexer.getNumber();
            return new Number(num).getValue();
        }
    }
}
