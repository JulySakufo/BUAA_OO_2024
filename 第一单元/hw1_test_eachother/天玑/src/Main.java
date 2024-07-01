import java.util.Scanner;

public class Main {
    //处理空格
    //处理数字前符号
    //处理连续+-
    //括号外的+-
    //*或^后跟正负号
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        input = input.replaceAll("\\s*","");
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        Expr simpleExpr = expr.simplify();
        simpleExpr.merge();
        System.out.println(simpleExpr.toString());
    }
}
