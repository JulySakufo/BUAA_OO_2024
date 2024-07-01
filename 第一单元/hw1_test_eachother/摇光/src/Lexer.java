import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private final String expression;
    private int pos;

    public Lexer(String expression) {
        this.expression = expression;
        this.pos = 0;
    }

    public char peek() {
        return this.expression.charAt(pos);
    }

    public boolean hasNext() {
        return pos != this.expression.length();
    }

    //functions to move pos
    public void next() {
        if (pos != expression.length()) {
            pos++;
        }
    }

    public BigInteger getNumber() {
        String subString = expression.substring(pos);
        Pattern pattern = Pattern.compile("([+-]?[0-9]+)");
        Matcher matcher = pattern.matcher(subString);
        if (matcher.find()) {
            String numString = matcher.group(0);
            pos += numString.length();
            return new BigInteger(numString);
        }
        return BigInteger.ZERO;
    }

    /*no functions*/
    public String getVariable() {
        String subString = expression.substring(pos);
        Pattern pattern = Pattern.compile("[a-zA-Z]+");
        Matcher matcher = pattern.matcher(subString);
        if (matcher.find()) {
            String variableString = matcher.group(0);
            pos += variableString.length();
            return variableString;
        }
        return null;
    }

    public String getExprBracket() {
        int start = pos;
        int stack = 0; //stack < 150
        if (expression.charAt(pos) == '(') {
            stack++;
            while (stack != 0) {
                //assert that stack >= 0
                this.next();
                if (expression.charAt(pos) == '(') {
                    stack++;
                }
                else if (expression.charAt(pos) == ')') {
                    stack--;
                }
            }
        }
        this.next();
        return expression.substring(start,pos);
    }

}
