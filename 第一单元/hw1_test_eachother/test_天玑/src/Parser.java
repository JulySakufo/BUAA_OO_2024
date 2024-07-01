import java.math.BigInteger;

public class Parser {
    private Lexer lexer;
    
    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }
    
    public Expr parseExpr() {
        Expr expr = new Expr();
        if (lexer.getCurrentToken().equals("-")) {
            lexer.next();
            expr.addTerm(parseTerm().reverse());
        }
        else if (lexer.getCurrentToken().equals("+")) {
            lexer.next();
            expr.addTerm(parseTerm());
        }
        else {
            expr.addTerm(parseTerm());
        }
        while (lexer.getCurrentToken().equals("+") || lexer.getCurrentToken().equals("-")) {
            if (lexer.getCurrentToken().equals("-")) {
                lexer.next();
                expr.addTerm(parseTerm().reverse());
            }
            else {
                lexer.next();
                expr.addTerm(parseTerm());
            }
        }
        return expr;
    }
    
    public Term parseTerm() {
        Term term = new Term();
        term.addFactor(parseFactor());
        while (lexer.getCurrentToken().equals("*")) {
            lexer.next();
            term.addFactor(parseFactor());
        }
        return term;
    }
    
    public Factor parseFactor() {
        if (lexer.getCurrentToken().equals("(")) {
            lexer.next();
            Factor expr = parseExpr();
            lexer.next();
            if (lexer.getCurrentToken().equals("^")) {
                lexer.next();
                if (lexer.getCurrentToken().equals("+")) {
                    lexer.next();
                }
                Factor mi = new Mi(Integer.parseInt(lexer.getCurrentToken()),expr);
                lexer.next();
                return mi;
            }
            else {
                Factor mi = new Mi(1,expr);
                return mi;
            }
        }
        else if (Character.isDigit(lexer.getCurrentToken().charAt(0))) {
            BigInteger num = new BigInteger(lexer.getCurrentToken());
            lexer.next();
            Factor num1 = new Number(num);
            return num1;
        }
        else if (lexer.getCurrentToken().equals("+")) {
            lexer.next();
            BigInteger num = new BigInteger(lexer.getCurrentToken());
            lexer.next();
            Factor num1 = new Number(num);
            return num1;
        }
        else if (lexer.getCurrentToken().equals("-")) {
            lexer.next();
            BigInteger num = new BigInteger(lexer.getCurrentToken());
            lexer.next();
            Factor num1 = new Number(num.negate());
            return num1;
        }
        else {
            Factor variable = new Variable(lexer.getCurrentToken());
            lexer.next();
            if (lexer.getCurrentToken().equals("^")) {
                lexer.next();
                if (lexer.getCurrentToken().equals("+")) {
                    lexer.next();
                }
                Factor mi = new Mi(Integer.parseInt(lexer.getCurrentToken()),variable);
                lexer.next();
                return mi;
            }
            else {
                return variable;
            }
        }
    }
}
