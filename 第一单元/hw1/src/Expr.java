import java.math.BigInteger;
import java.util.HashMap;

public class Expr implements Factor {
    
    private final HashMap<BigInteger, BigInteger> variableMap; //存储着每个项的exp和coe值
    
    public Expr() {
        this.variableMap = new HashMap<>();
    }
    
    public void addTerm(Term term) {
        HashMap<BigInteger, BigInteger> hashMap = term.getHashMap();
        for (BigInteger exponent : hashMap.keySet()) {
            BigInteger coefficient = hashMap.get(exponent);
            if (variableMap.containsKey(exponent)) {
                variableMap.replace(exponent, coefficient.add(variableMap.get(exponent)));
            } else {
                variableMap.put(exponent, coefficient);
            }
        }
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (BigInteger exponent : variableMap.keySet()) {
            BigInteger coefficient = variableMap.get(exponent);
            if (coefficient.compareTo(new BigInteger("0")) > 0) { //找到一个比0大的coe;
                if (coefficient.compareTo(new BigInteger("1")) == 0) { //coe = 1
                    if (exponent.compareTo(new BigInteger("1")) == 0) { //1*x^1
                        sb.append("x");
                    } else if (exponent.compareTo(new BigInteger("0")) == 0) { //1*x^0
                        sb.append("1");
                    } else { //1*x^b
                        sb.append("x");
                        sb.append("^");
                        sb.append(exponent);
                    }
                } else { //coe!=1
                    if (exponent.compareTo(new BigInteger("1")) == 0) { //2*x^1
                        sb.append(coefficient);
                        sb.append("*");
                        sb.append("x");
                    } else if (exponent.compareTo(new BigInteger("0")) == 0) { //2*x^0
                        sb.append(coefficient);
                    } else { //2*x^b
                        sb.append(coefficient);
                        sb.append("*");
                        sb.append("x");
                        sb.append("^");
                        sb.append(exponent);
                    }
                }
                variableMap.remove(exponent);//因为打印，所以清除；
                break;
            }
        }
        for (BigInteger exponent : variableMap.keySet()) {
            BigInteger coefficient = variableMap.get(exponent);
            optimize(sb, exponent, coefficient);
        }
        if (sb.toString().equals("")) {
            return "0";
        } else {
            return sb.toString();
        }
    }
    
    @Override
    public HashMap<BigInteger, BigInteger> getHashMap() {
        return this.variableMap;
    }
    
    public void polyPow(int sum) {
        HashMap<BigInteger, BigInteger> operateMap = new HashMap<>();
        HashMap<BigInteger, BigInteger> resultMap = new HashMap<>();
        for (BigInteger exponent : variableMap.keySet()) {
            BigInteger coefficient = variableMap.get(exponent);
            operateMap.put(exponent, coefficient);
            resultMap.put(exponent, coefficient);
        } //准备进行操作
        if (sum == 0) {
            variableMap.clear();
            variableMap.put(new BigInteger("0"), new BigInteger("1"));
            return;
        }
        for (int i = 1; i < sum; i++) {
            resultMap.clear();
            for (BigInteger exponent : variableMap.keySet()) {
                BigInteger coefficient = variableMap.get(exponent);
                for (BigInteger exponent1 : operateMap.keySet()) {
                    BigInteger coefficient1 = operateMap.get(exponent1);
                    BigInteger resExp = exponent.add(exponent1);
                    BigInteger resCoe = coefficient.multiply(coefficient1);
                    if (resultMap.containsKey(resExp)) {
                        resultMap.replace(resExp, resCoe.add(resultMap.get(resExp)));
                    } else {
                        resultMap.put(resExp, resCoe);
                    }
                }
            }
            operateMap.clear();
            for (BigInteger exponent2 : resultMap.keySet()) { //实现操作数的不断更新
                operateMap.put(exponent2, resultMap.get(exponent2));
            }
        }
        variableMap.clear();
        for (BigInteger exponent : resultMap.keySet()) {
            BigInteger coefficient = resultMap.get(exponent);
            variableMap.put(exponent, coefficient);
        }
    }
    
    public void negate() {
        for (BigInteger key : variableMap.keySet()) {
            variableMap.replace(key, variableMap.get(key).multiply(new BigInteger("-1")));
        }
    }
    
    public void optimize(StringBuilder sb, BigInteger exp, BigInteger coe) {
        if (coe.compareTo(new BigInteger("1")) == 0) { //1*x^b
            if (exp.compareTo(new BigInteger("1")) == 0) {  //1*x^1 -> x
                sb.append("+");//放心大胆加，因为要么不会出现该符号，要么在之前已经有了不是+号
                sb.append("x");
            } else if (exp.compareTo(new BigInteger("0")) == 0) { //1*x^0
                sb.append("+");
                sb.append("1");
            } else { //1*x^b
                sb.append("+");
                sb.append("x");
                sb.append("^");
                sb.append(exp);
            }
        } else if (coe.compareTo(new BigInteger("-1")) == 0) { //-1*x^b
            if (exp.compareTo(new BigInteger("1")) == 0) {  //-1*x^1 -> x
                sb.append("-");//放心大胆加，因为要么不会出现该符号，要么在之前已经有了不是+号
                sb.append("x");
            } else if (exp.compareTo(new BigInteger("0")) == 0) { //-1*x^0
                sb.append("-");
                sb.append("1");
            } else { //-1*x^b
                sb.append("-");
                sb.append("x");
                sb.append("^");
                sb.append(exp);
            }
        } else if (coe.compareTo(new BigInteger("1")) > 0) {
            if (exp.compareTo(new BigInteger("1")) == 0) {  //2*x^1 -> x
                sb.append("+");//放心大胆加，因为要么不会出现该符号，要么在之前已经有了不是+号
                sb.append(coe);
                sb.append("*");
                sb.append("x");
            } else if (exp.compareTo(new BigInteger("0")) == 0) { //2*x^0
                sb.append("+");
                sb.append(coe);
            } else { //2*x^b
                sb.append("+");
                sb.append(coe);
                sb.append("*");
                sb.append("x");
                sb.append("^");
                sb.append(exp);
            }
        } else if (coe.compareTo(new BigInteger("-1")) < 0) {
            if (exp.compareTo(new BigInteger("1")) == 0) {  //-2*x^1 -> x
                sb.append(coe);
                sb.append("*");
                sb.append("x");
            } else if (exp.compareTo(new BigInteger("0")) == 0) { //-2*x^0
                sb.append(coe);
            } else { //-2*x^b
                sb.append(coe);
                sb.append("*");
                sb.append("x");
                sb.append("^");
                sb.append(exp);
            }
        }
    }
}

