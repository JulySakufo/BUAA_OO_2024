import expr.Expr;
import expr.Factor;
import expr.Number;
import expr.Term;

import java.math.BigInteger;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expr parseExpr() {
        Expr expr = new Expr();//表达式
        String sign;
        if (lexer.peek().equals("+") || lexer.peek().equals("-")) {
            sign = lexer.peek();
            lexer.next();
            expr.addTerm(sign, parseTerm());
        } else {
            expr.addTerm("+", parseTerm());
        }

        while (lexer.peek().equals("+") || lexer.peek().equals("-")) {
            sign = lexer.peek();
            lexer.next();
            expr.addTerm(sign, parseTerm());
        }
        return expr;
    }

    public Term parseTerm() {
        Term term = new Term();//项
        String sign;//处理第一个因子之前的符号
        Factor tmp;
        if (lexer.peek().equals("+") || lexer.peek().equals("-")) {
            sign = lexer.peek();
            lexer.next();
        } else {
            sign = "+";
        }
        tmp = parseFactor();
        if (lexer.peek().equals("^")) {
            lexer.next();
            if (lexer.peek().equals("+")) {
                lexer.next();//指数带符号
            }
            int n = Integer.parseInt(lexer.peek());//指数暂时不会太大
            lexer.next();
            if (n > 0) {
                term.addFactor(sign, tmp);
            } else {
                term.addFactor(sign, new Number(BigInteger.ZERO, BigInteger.ONE));
            }
            for (int i = 1; i < n; i++) {
                term.addFactor("+", tmp);
            }
        } else {
            term.addFactor(sign, tmp);
        }

        while (lexer.peek().equals("*")) {
            lexer.next();
            tmp = parseFactor();
            if (lexer.peek().equals("^")) {
                lexer.next();
                if (lexer.peek().equals("+")) {
                    lexer.next();//指数带符号
                }
                int n = Integer.parseInt(lexer.peek());
                lexer.next();
                for (int i = 0; i < n; i++) {
                    term.addFactor("+", tmp);
                }
                if (n == 0) {
                    term.addFactor("+", new Number(BigInteger.ZERO, BigInteger.ONE));
                }
            } else {
                term.addFactor("+", tmp);
            }
        }
        return term;
    }

    public Factor parseFactor() {
        if (lexer.peek().equals("(")) { //表达式因子
            lexer.next();
            Factor expr = parseExpr();
            lexer.next();//跳过括号
            return expr;
        } else if (lexer.peek().equals("x")) {
            lexer.next();
            BigInteger para = new BigInteger("1");
            BigInteger exp;
            if (lexer.peek().equals("^")) {
                lexer.next();
                if (lexer.peek().equals("+")) {
                    lexer.next();
                }
                exp = new BigInteger(lexer.peek());
                lexer.next();
            } else {
                exp = new BigInteger("1");
            }
            return new Number(exp, para);
        } else {
            String sign;
            if (lexer.peek().equals("+") || lexer.peek().equals("-")) {
                sign = lexer.peek();
                lexer.next();
            } else {
                sign = "+";
            }
            BigInteger para = new BigInteger(sign + lexer.peek());//自动转前导0
            lexer.next();
            BigInteger exp = new BigInteger("0");
            return new Number(exp, para);
        }
    }

}
