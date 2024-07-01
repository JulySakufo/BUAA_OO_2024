import java.util.ArrayList;

public class Lexer {
    private final ArrayList<Token> tokens = new ArrayList<>();
    private int cur;

    public Token now() {
        return tokens.get(cur);
    }

    public Token pre() {
        if (cur - 1 >= 0) {
            return tokens.get(cur - 1);
        }
        return null;
    }

    public Token post() {
        if (cur + 1 < tokens.size()) {
            return tokens.get(cur + 1);
        }
        return null;
    }

    public void move() {
        cur++;
    }

    public boolean notEnd() {
        return cur < tokens.size();
    }

    public Lexer(String expression) {
        int pos = 0;
        while (pos < expression.length()) {
            if (expression.charAt(pos) == '(') {
                tokens.add(new Token(Token.Type.LEFT_P, "("));
                pos++;
            } else if (expression.charAt(pos) == ')') {
                tokens.add(new Token(Token.Type.RIGHT_P, ")"));
                pos++;
            } else if (expression.charAt(pos) == '*') {
                tokens.add(new Token(Token.Type.MUL, "*"));
                pos++;
            } else if (expression.charAt(pos) == '^') {
                tokens.add(new Token(Token.Type.CARET, "^"));
                pos++;
                if (expression.charAt(pos) == '+') {
                    pos++;
                }
            } else if (expression.charAt(pos) == 'x') {
                tokens.add(new Token(Token.Type.VAR_X, "x"));
                pos++;
            } else if (expression.charAt(pos) == '+') {
                tokens.add(new Token(Token.Type.ADD, "+"));
                pos++;
            } else if (expression.charAt(pos) == '-') {
                tokens.add(new Token(Token.Type.SUB, "-"));
                pos++;
            } else {
                char now = expression.charAt(pos);
                StringBuilder numStr = new StringBuilder();
                while ('0' <= now && now <= '9') {
                    numStr.append(now);
                    pos++;
                    if (pos >= expression.length()) {
                        break;
                    }
                    now = expression.charAt(pos);
                }
                tokens.add(new Token(Token.Type.NUM, numStr.toString()));
            }
        }
    }
}
