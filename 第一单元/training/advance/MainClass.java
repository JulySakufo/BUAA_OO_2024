import expr.Expr;

import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next(); //读入表达式;
        
        Lexer lexer = new Lexer(input); //词法;
        Parser parser = new Parser(lexer); //装入解析器
        //从表达式的最开始依次往后遍历解析整个表达式
        Expr expr = parser.parseExpr(); //解析后的表达式;
        System.out.println(expr); //输出解析完毕的后缀表达式;
    }
}

