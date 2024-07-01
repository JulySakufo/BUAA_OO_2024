import java.math.BigInteger;

public class Lexer {
    private final String input;
    private int pos = 0;

    public Lexer(String input) {
        String str = input.replaceAll("[ \t]", "");
        for (int i = 0; i < 2; i++) {
            str = str.replaceAll("\\+\\+", "+");
            str = str.replaceAll("\\+-", "-");
            str = str.replaceAll("-\\+", "-");
            str = str.replaceAll("--", "+");
        }
        this.input = str;
    }

    public BigInteger getNumber() {
        StringBuilder sb = new StringBuilder();
        int positive = 1;
        if (input.charAt(pos) == '+' || input.charAt(pos) == '-') {
            if (input.charAt(pos) == '-') {
                positive = -1;
            }
            ++pos;
        }
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            ++pos;
        }
        if (positive == 1) {
            return new BigInteger(sb.toString());
        } else {
            return new BigInteger(sb.toString()).negate();
        }
    }

    public void next() {
        if (pos == input.length() - 1) {
            return;
        }
        pos += 1;
    }

    public String peek() {
        if (pos < input.length()) {
            return String.valueOf(input.charAt(pos));
        } else {
            return " ";
        }
    }
}
