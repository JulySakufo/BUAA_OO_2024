import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String preExpression = scanner.nextLine();
        StringProcessing stringProcessing = new StringProcessing();
        String expression = stringProcessing.pretreatment(preExpression);
        //System.out.println(expression);
        Lexer lexer = new Lexer(expression);
        Parser parser = new Parser(lexer);

        Expr expr = parser.parseExpr();
        Polynomial polynomial = expr.toPolynomial();
        String result = polynomial.toString();
        if (result.equals("")) {
            result = "0";
        }
        result = result.charAt(0) == '+' ? result.substring(1) : result;
        System.out.println(result);
    }
}
