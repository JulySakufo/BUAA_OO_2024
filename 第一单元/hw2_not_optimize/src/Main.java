import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        scanner.nextLine();//吃掉换行符
        for (int i = 0; i < n; i++) {
            String function = scanner.nextLine();
            function = Main.preProcess(function);
            Definer.addFunction(function);//函数的声明
        }
        String input = scanner.nextLine(); //读入表达式;
        input = Main.preProcess(input);
        //输入预处理，先去空白符，再去使得每一个数只带一个符号sign
        Lexer lexer = new Lexer(input); //词法;
        Parser parser = new Parser(lexer); //装入解析器
        //从表达式的最开始依次往后遍历解析整个表达式
        Expr expr = parser.parseExpr(); //解析后的表达式;
        System.out.println(expr.toPoly().toString()); //输出解析完毕的后缀表达式;
    }
    
    public static String preProcess(String s) {
        String string = s;
        string = string.replaceAll("[ \t]", "");
        string = string.replaceAll("(\\+\\+)|(--)", "+");
        string = string.replaceAll("(\\+-)|(-\\+)", "-");
        string = string.replaceAll("(\\+\\+)|(--)", "+");
        string = string.replaceAll("(\\+-)|(-\\+)", "-");
        return string;
    }
}