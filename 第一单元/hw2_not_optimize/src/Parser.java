import java.math.BigInteger;
import java.util.ArrayList;

public class Parser { //解析，是对一个词进行解析，因此词lexer是它的属性；
    private final Lexer lexer; //final修饰变量，一次赋值后不可再更改；
    
    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }
    
    public Expr parseExpr() { //表达式的解析
        Expr expr = new Expr();
        int sign = 1;
        if (lexer.peek().equals("-")) {
            sign = -1;
            lexer.next();
            expr.addTerm(parseTerm(sign));
        } else if (lexer.peek().equals("+")) {
            lexer.next();//读下一个
            expr.addTerm(parseTerm(sign));
        } else {
            expr.addTerm(parseTerm(sign));
        }
        while (lexer.peek().equals("+") || lexer.peek().equals("-")) { //提取项的概念，使得在外部处理只有+
            if (lexer.peek().equals("-")) { //先识别符号
                lexer.next();
                expr.addTerm(parseTerm(-1)); //只管调用，不用管项的具体解析过程
            } else {
                lexer.next();
                expr.addTerm(parseTerm(1)); //只管调用，不用管项的具体解析过程
            }
            
        }
        return expr; //项的合并，成为最简式
    }
    
    public Term parseTerm(int sign) { //项的解析，返回项的解析结果  一个项！！！
        Term term = new Term(sign);
        Factor factor = parseFactor();
        term.addFactor(factor);
        
        while (lexer.peek().equals("*")) {
            lexer.next();
            if (lexer.peek().equals("-")) { //不作为运算符，因子的符号作为调度项的整体符号
                lexer.next();
                term.negate();
            } else if (lexer.peek().equals("+")) { //不作为运算符
                lexer.next();
            }
            term.addFactor(parseFactor()); //只管调用，不用管因子的具体解析过程
        }
        return term; //把一个项的所有因子都解析出来了，进行项的合并,成为最简项
    }
    
    public Factor parseFactor() { //因子的解析
        if (lexer.peek().equals("f") || lexer.peek().equals("g") || lexer.peek().equals("h")) {
            return parseFuncFactor();
        } else if (lexer.peek().matches("\\d+")) { //数字因子;
            return parseNumberFactor();
        } else if (lexer.peek().equals("exp")) { //指数函数因子;
            return parseExpFactor();
        } else if (lexer.peek().equals("(")) { //表达式因子
            return parseExprFactor();
        } else { //是幂函数因子,此时lexer.peek()得到的是变量名，使用lexer.next()可能得到是^，有可能没有^
            return parseVariableFactor();
        }
    }
    
    public Factor parseExpFactor() { //对指数函数的解析   exp(Factor)^?
        lexer.next();//跳过固有的左括号
        lexer.next();
        //lexer.next();//exp(Factor),跳到因子处
        Factor factor = parseExpr();//TODO：我已经将parseFactor换成了parseExprFactor
        lexer.next();//跳过右括号；
        //lexer.next();//指向下一个符号
        if (lexer.peek().equals("^")) {
            lexer.next(); //指向exponent部分
            if (lexer.peek().equals("+")) {
                lexer.next();//跳过+
            }
            BigInteger exponent = new BigInteger(lexer.peek());
            lexer.next();//指向下一个符号
            return new Exp(factor, exponent);
        } else { //省略形式
            BigInteger exponent = new BigInteger("1");
            return new Exp(factor, exponent);
        }
    }
    
    public Factor parseExprFactor() {
        lexer.next(); //下一个词
        Expr expr = parseExpr(); //已经知道下一个lexer对应的是一个项，因此采用表达式的解析方法
        int sum = 1;
        /*if (lexer.peek().equals(",")) { //TODO:修改逻辑使得多元函数成立
            return new ExprFactor(expr, sum);
        }*/
        lexer.next();//跳过右括号；
        if (lexer.peek().equals("^")) {
            lexer.next();
            if (lexer.peek().equals("+")) {
                lexer.next();//跳过符号
            }
            sum = Integer.parseInt(lexer.peek());
            lexer.next();//指向下一个符号
        }
        return new ExprFactor(expr, sum);
    }
    
    public Factor parseNumberFactor() {
        BigInteger coefficient = new BigInteger(lexer.peek());
        lexer.next(); //分析完毕，指向下一个lexer
        BigInteger exponent = new BigInteger("0");
        return new Variable(coefficient, "x", exponent);
    }
    
    public Factor parseVariableFactor() {
        String name = lexer.peek();
        lexer.next();//变量名的下一个未分析的符号
        if (lexer.peek().equals("^")) { //幂函数因子的一般形式
            lexer.next();
            BigInteger coefficient = new BigInteger("1");
            if (lexer.peek().equals("+")) { //x^+5
                lexer.next();//跳过+
            }
            BigInteger exponent = new BigInteger(lexer.peek());
            lexer.next();//这一个指数因子分析完毕，指向下一个未分析的成分
            return new Variable(coefficient, name, exponent);
            //先使变量的系数全为1
        } else { //幂函数因子的省略形式
            BigInteger coefficient = new BigInteger("1");//下一个未分析的符号是+-号，跳过
            BigInteger exponent = new BigInteger("1");
            return new Variable(coefficient, name, exponent); //先使变量的系数全为1
        }
    }
    
    public Factor parseFuncFactor() { // f(因子,因子,因子)//TODO:BUG
        final String name = lexer.peek(); //f|g|h
        lexer.next();//(
        lexer.next();
        //lexer.next();//因子
        ArrayList<Factor> realParameters = new ArrayList<>();
        //Factor factor = parseFactor();
        Factor factor = parseExpr();
        realParameters.add(factor);
        while (lexer.peek().equals(",")) { //函数的调用未结束
            //lexer.next();//跳过逗号;
            //realParameters.add(parseFactor());
            lexer.next();
            realParameters.add(parseExpr());
        }
        lexer.next();
        return new FuncFactor(name, realParameters);//函数因子返回
    }
}

