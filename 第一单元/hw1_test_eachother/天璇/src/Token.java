public class Token {
    // The types of the tokens.
    public enum Type {
        ADD, SUB, MUL, CARET,
        LEFT_P, RIGHT_P,
        VAR_X, NUM
    }

    private final Type type;
    private final String content;

    public Token(Token.Type type, String content) {
        this.type = type;
        this.content = content;
    }

    public Type getType() {
        return type;
    }

    public String getContent() {
        return content;
    }
}
