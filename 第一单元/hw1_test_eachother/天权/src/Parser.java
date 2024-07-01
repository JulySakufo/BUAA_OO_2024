import factor.Expression;
import factor.Factor;
import factor.Polynome;
import factor.Term;
import factor.Unit;

import java.math.BigInteger;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expression parseExpr() {
        Expression expression = new Expression();
        expression.AddTerm(parseTerm(), true);

        while (lexer.peek().equals("+") || lexer.peek().equals("-")) {
            expression = expression.ExprSimplify();
            boolean isAdd = lexer.peek().equals("+");
            lexer.next();
            expression.AddTerm(parseTerm(), isAdd);
        }

        return expression.ExprSimplify();
    }

    private Term parseTerm() {
        Term term = new Term();

        Factor prevFactor = parseFactor();
        HandlePower(term, prevFactor);

        while (lexer.peek().equals("*")) {
            lexer.next();
            prevFactor = parseFactor();
            this.HandlePower(term, prevFactor);
        }

        return term;
    }

    private void HandlePower(Term term, Factor prevFactor) {
        if (lexer.peek().equals("^")) {
            lexer.next();
            int power = Integer.parseInt(parseFactor().toString());

            if (power == 0) {
                term.AddFactor(new Unit(BigInteger.ONE));
            } else {
                for (int i = 0; i < power; i++) {
                    term.AddFactor(prevFactor.CopyFactor());
                }
            }
        } else {
            term.AddFactor(prevFactor);
        }
    }

    private Factor parseFactor() {
        if (lexer.peek().equals("(")) {
            lexer.next();
            Factor expr = parseExpr();
            lexer.next();

            return expr;
        } else {
            String curToken = lexer.peek();

            if (Character.isDigit(curToken.charAt(0))) {
                BigInteger num = new BigInteger(lexer.peek());
                lexer.next();

                return new Unit(num);
            } else {
                Polynome poly = new Polynome(curToken, BigInteger.ONE);
                lexer.next();

                return new Unit(poly);
            }
        }
    }
}