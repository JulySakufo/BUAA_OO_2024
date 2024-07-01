import java.util.ArrayList;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expr parseExpr() {
        ArrayList<Term> terms = new ArrayList<>();
        int sign = 1;
        // the first term
        if (lexer.now().getType() == Token.Type.ADD) {
            sign = 1;
            lexer.move();
        } else if (lexer.now().getType() == Token.Type.SUB) {
            sign = -1;
            lexer.move();
        }
        terms.add(parseTerm(sign));
        // the left
        while (lexer.notEnd() && (lexer.now().getType() == Token.Type.ADD
                || lexer.now().getType() == Token.Type.SUB)) {
            sign = lexer.now().getType() == Token.Type.ADD ? 1 : -1;
            lexer.move();
            terms.add(parseTerm(sign));
        }
        return new Expr(terms);
    }

    public Term parseTerm(int sign) {
        ArrayList<Factor> factors = new ArrayList<>();
        factors.add(parseFactor());
        while (lexer.notEnd() && lexer.now().getType() == Token.Type.MUL) {
            lexer.move();
            factors.add(parseFactor());
        }
        return new Term(sign, factors);
    }

    public Factor parseFactor() {
        if (lexer.notEnd() && lexer.now().getType() == Token.Type.NUM) {
            Num num = new Num(lexer.now().getContent(), 1);
            lexer.move();
            return num;
        } else if (lexer.notEnd() && (lexer.now().getType() == Token.Type.ADD
                || lexer.now().getType() == Token.Type.SUB)) {
            Num num;
            if (lexer.now().getType() == Token.Type.ADD) {
                lexer.move();
                num = new Num(lexer.now().getContent(), 1);
                lexer.move();
            } else {
                lexer.move();
                num = new Num(lexer.now().getContent(), -1);
                lexer.move();
            }
            return num;
        }
        else if (lexer.notEnd() && lexer.now().getType() == Token.Type.VAR_X) {
            Power power;
            if (lexer.post() != null && lexer.post().getType() == Token.Type.CARET) {
                lexer.move();
                lexer.move();
                power = new Power(Integer.parseInt(lexer.now().getContent()));
            } else {
                power = new Power(1);
            }
            lexer.move();
            return power;
        } else {
            lexer.move();
            Expr expr = parseExpr();
            lexer.move();
            if (lexer.notEnd() && lexer.now().getType() == Token.Type.CARET) {
                lexer.move();
                lexer.move();
                return new ExprFactor(expr, Integer.parseInt(lexer.pre().getContent()));
            } else {
                return new ExprFactor(expr, 1);
            }
        }
    }
}
