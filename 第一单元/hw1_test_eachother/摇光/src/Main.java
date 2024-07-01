import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String exprString = scanner.nextLine().replaceAll("[ \t]","");
        Lexer lexer = new Lexer(exprString);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        Polynomial polynomial = expr.getResult();
        polynomial.printPolynomial();
    }
}