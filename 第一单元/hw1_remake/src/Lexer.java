public class Lexer {
    private final String input;
    private int pos = 0;
    private String curToken; //语法分析的一个最小项，以一个数或者一个符号作为单位返回；
    
    public Lexer(String input) {
        this.input = input;
        this.next();
    }
    
    private String getNumber() { //挖掘连续数字;
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) { //是数字就一直找到不是的那一个;
            sb.append(input.charAt(pos));
            ++pos;
        }
        
        return sb.toString();//整个数字作为一个整体返回，作为得到的结果；
    }
    
    public String getVariable() { //挖掘连续字母
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isLetter(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            ++pos;
        }
        return sb.toString();
    }
    
    public void next() { //
        if (pos == input.length()) {
            return; //什么操作都不做；
        }
        
        char c = input.charAt(pos);
        if (Character.isDigit(c)) { //是数字
            curToken = getNumber();
        } else if (c == '+' || c == '*' || c == '(' || c == ')' || c == '^' || c == '-') { //不是数字;
            pos += 1;
            curToken = String.valueOf(c);
        } else { //是变量
            curToken = getVariable();
        }
    }
    
    public String peek() { //返回当前这个东西是什么
        return this.curToken;
    }
}

