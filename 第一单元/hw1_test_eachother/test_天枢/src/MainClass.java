import java.math.BigInteger;
import java.util.HashMap;
import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        HashMap<BigInteger, BigInteger> expr = parser.parseExpr();
        Printer.printer(expr);
    }
}
