import factor.Expression;

import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String expressionIn = scanner.nextLine();

        Lexer lexer = new Lexer(expressionIn);
        Parser parser = new Parser(lexer);

        Expression expression = parser.parseExpr();

        System.out.println(expression);
    }
}
