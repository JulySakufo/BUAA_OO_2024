public class Lexer {
    private int pos = 0;
    private String input;
    private String currentToken;
    
    public Lexer(String input) {
        this.input = input;
        this.next();
    }

    private String getNumber() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            ++pos;
        }
        return sb.toString();
    }
    
    private String getEnglish() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isLetter(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            ++pos;
        }
        return sb.toString();
    }
    
    public String getCurrentToken() {
        return currentToken;
    }
    
    public void next() {
        if (pos == input.length()) {
            return;
        }
        char c = input.charAt(pos);
        if (Character.isDigit(c)) {
            currentToken = getNumber();
        } else if (c == '*' || c == '(' || c == ')' || c == '^') {
            pos++;
            currentToken = String.valueOf(c);
        }
        else if (Character.isLetter(c)) {
            currentToken = getEnglish();
        }
        else {
            char b = input.charAt(pos + 1);
            while ((c == '+' | c == '-') && pos + 1 != input.length() && (b == '+' || b == '-')) {
                if (input.charAt(pos + 1) == '+') {
                    pos++;
                    b = input.charAt(pos + 1);
                }
                else if (c == '+') {
                    c = '-';
                    pos++;
                    b = input.charAt(pos + 1);
                }
                else {
                    c = '+';
                    pos++;
                    b = input.charAt(pos + 1);
                }
            }
            currentToken = String.valueOf(c);
            pos++;
        }
    }
    
}
