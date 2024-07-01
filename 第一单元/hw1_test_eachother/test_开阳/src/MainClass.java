import expr.Expr;

import java.util.Scanner;

//考虑使用HashMap<String,Unit>来存储表达式，其中Unit为基本单元，String作为key来合并同类项
//考虑正数项先输出
//指数暂时不会太大

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        //预处理，去空白
        input = input.replaceAll("[ \t]", "");

        Lexer lexer = new Lexer(input);//词法分析，获得token
        Parser parser = new Parser(lexer);//解析输入，获得输出
        Expr expr = parser.parseExpr();

        System.out.println(Simplify.simplify(expr.toPoly()));
    }
}
