import java.util.ArrayList;
import java.util.HashMap;

public class Definer {
    private static HashMap<String, String> functionMap = new HashMap<>();//函数名获取形式化表达式
    private static HashMap<String, ArrayList<String>> parameterMap = new HashMap<>();//函数名获得形参列表
    
    public static void addFunction(String function) {
        String string = function;
        ArrayList<String> virtualParameters = new ArrayList<>();//形参的读入顺序不一定是xyz
        final int key = string.indexOf("=");
        if (string.contains("exp")) {
            string = string.replaceAll("exp", "eap");//防止参数误加入
        }
        for (int i = 0; i < key; i++) {
            if (string.charAt(i) == 'x') {
                String parameter = "_x";
                virtualParameters.add(parameter);
            } else if (string.charAt(i) == 'y') {
                String parameter = "_y";
                virtualParameters.add(parameter);
            } else if (string.charAt(i) == 'z') {
                String parameter = "_z";
                virtualParameters.add(parameter);
            }
        } //修改了形参读入顺序，防止是f(y,x,z)形式
        int len = string.length();
        String expr = string.substring(key + 1, len);
        expr = expr.replaceAll("x", "_x");
        expr = expr.replaceAll("y", "_y");
        expr = expr.replaceAll("z", "_z");//防止f(y,x)=x+y,f(x,x^2)的异常
        expr = expr.replaceAll("eap", "exp");//防止exp的x被替换掉
        String name = String.valueOf(string.charAt(0));//f,g,h
        functionMap.put(name, expr);//函数名与表达式的对应
        parameterMap.put(name, virtualParameters);
    } //函数表达式的读入与形参化
    
    public static String callFunction(String name, ArrayList<Factor> realParameters) {
        String function = functionMap.get(name);
        ArrayList<String> arraylist = parameterMap.get(name);
        for (int i = 0; i < arraylist.size(); i++) {
            Factor factor = realParameters.get(i);
            final String parameter = arraylist.get(i);
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            sb.append(factor.toPoly().toString());
            sb.append(")");
            function = function.replaceAll(parameter, sb.toString());//实现形参实参替换即可
        } //TODO 感觉应该是用表达式因子的方式存储，否则x^2^2这种替换不符合规则，而已有(x^2)^2的计算方式
        function = Main.preProcess(function);//消除连续的正负号
        return function;//将替换好了的表达式返回回去;
    }
}
