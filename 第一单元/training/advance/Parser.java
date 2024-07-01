import expr.Expr;
import expr.Factor;
import expr.Number;
import expr.Term;

import java.math.BigInteger;

public class Parser { //解析，是对一个词进行解析，因此词lexer是它的属性；
    private final Lexer lexer; //final修饰变量，一次赋值后不可再更改；
    
    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }
    //具体的解析过程封装在expr这个package中，这里只负责逻辑上的调用方法；
    public Expr parseExpr() { //表达式的解析
        Expr expr = new Expr();
        expr.addTerm(parseTerm());
        
        while (lexer.peek().equals("+")) { //表达式+项;
            lexer.next();
            expr.addTerm(parseTerm()); //只管调用，不用管项的具体解析过程
        }
        return expr; //把表达式的所有项都解析出来了，返回表达式的解析结果(哪些项)
    }
    
    public Term parseTerm() { //项的解析，返回项的解析结果
        Term term = new Term();
        term.addFactor(parseFactor());
        
        while (lexer.peek().equals("*")) {
            lexer.next();
            term.addFactor(parseFactor()); //只管调用，不用管因子的具体解析过程
        }
        return term; //把项的所有因子都解析出来了，返回项的解析结果(哪些因子)
    }
    
    public Factor parseFactor() { //因子的解析
        if (lexer.peek().equals("(")) { //表达式因子
            lexer.next(); //下一个词
            Factor expr = parseExpr(); //已经知道下一个lexer对应的是一个项，因此采用表达式的解析方法
            lexer.next();//跳过右括号；
            return expr;
        } else { //数字因子;
            BigInteger num = new BigInteger(lexer.peek());
            lexer.next(); //分析完毕，指向下一个lexer
            return new Number(num);
        }
    }
}

