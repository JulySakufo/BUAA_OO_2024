import java.util.ArrayList;

public class Parser {

    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expr parseExpr() {
        ArrayList<Term> termArrayList = new ArrayList<>();
        if (lexer.peek() != '+' && lexer.peek() != '-') {
            termArrayList.add(parseTerm(true));
        }
        while (lexer.hasNext() &&
            (this.lexer.peek() == '+' || this.lexer.peek() == '-')) {
            if (lexer.peek() == '+') {
                lexer.next();
                termArrayList.add(parseTerm(true));
            }
            else if (lexer.peek() == '-') {
                lexer.next();
                termArrayList.add(parseTerm(false));
            }
        }
        return new Expr(termArrayList);
    }

    public Term parseTerm(boolean isAdd) {
        boolean isPositive;
        ArrayList<Factor> factorArrayList = new ArrayList<>();
        if (lexer.peek() == '+') {
            isPositive = isAdd;
            lexer.next();
        }
        else if (lexer.peek() == '-') {
            isPositive = !isAdd;
            lexer.next();
        }
        else {
            isPositive = isAdd;
        }
        factorArrayList.add(parseFactor());
        while (lexer.hasNext() && lexer.peek() == '*')  {
            lexer.next();
            factorArrayList.add(parseFactor());
        }
        return new Term(factorArrayList,isPositive);
    }

    public Factor parseFactor() {
        char ch = lexer.peek();
        if (Character.isLetter(ch)) {
            String variable = lexer.getVariable();//after the op, pos at next
            int index;
            if (lexer.hasNext() && lexer.peek() == '^') {
                lexer.next();
                index = Integer.parseInt(lexer.getNumber().toString());
            }
            else {
                index = 1;
            }
            return new VariableFactor(variable,index);
        }
        else if (Character.isDigit(ch) || ch == '-' || ch == '+') {
            return new VariableFactor(lexer.getNumber());
        }
        else if (ch == '(') {
            String bracketExpr = lexer.getExprBracket();
            if (lexer.hasNext() && lexer.peek() == '^') {
                lexer.next();
                int index = Integer.parseInt(lexer.getNumber().toString());
                if (index == 0) {
                    Lexer lexer = new Lexer("1");
                    Parser parser = new Parser(lexer);
                    return parser.parseExpr();
                }
                else {
                    StringBuilder exprFactorString = new StringBuilder(bracketExpr);
                    for (int i = 0; i < index - 1; i++) {
                        exprFactorString.append('*');
                        exprFactorString.append(bracketExpr);
                    }
                    Lexer lexer = new Lexer(exprFactorString.toString());
                    Parser parser = new Parser(lexer);
                    return parser.parseExpr();
                }
            }
            else {
                Lexer lexer = new Lexer(bracketExpr.substring(1,bracketExpr.length() - 1));
                Parser parser = new Parser(lexer);
                return parser.parseExpr();
            }
        }
        return null;
    }

}
