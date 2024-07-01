import java.math.BigInteger;
import java.util.HashMap;
import java.util.Objects;

public class Mono { //最小单元a*x^b*exp(Factor)^c
    private BigInteger coefficient;//不作为输出，它的真实系数在poly的monomap里面
    private final BigInteger exponent;
    private HashMap<Poly, BigInteger> expMap;
    
    public Mono(BigInteger coefficient, BigInteger exponent) {
        this.coefficient = coefficient;
        this.exponent = exponent;
        this.expMap = new HashMap<>();
    }
    
    public void updateExpMap(Poly poly, BigInteger power) {
        if (expMap.containsKey(poly)) { //有该多项式
            expMap.replace(poly, expMap.get(poly).add(power));//exp的指数相加
        } else { //没有该多项式，存储进exp因子map，表示乘积形式
            expMap.put(poly, power);
        }
    }
    
    public Mono mulMono(Mono mono) { //expMap具有private属性，不放入poly类中
        BigInteger coe = mono.getCoefficient();
        BigInteger power = mono.getExponent();
        Mono newMono = new Mono(coe.multiply(coefficient), power.add(exponent));
        for (Poly poly : expMap.keySet()) {
            newMono.updateExpMap(poly, expMap.get(poly));
        }
        for (Poly poly : mono.expMap.keySet()) {
            newMono.updateExpMap(poly, mono.expMap.get(poly));
        }
        return newMono;
    }
    
    public BigInteger getCoefficient() {
        return coefficient;
    }
    
    public BigInteger getExponent() {
        return exponent;
    }
    
    public boolean equals(Object object) { //比较两个Mono是否相等，用于poly的合并
        if (object == this) {
            return true;
        }
        if (!(object instanceof Mono)) {
            return false;
        }
        Mono mono = (Mono) object;
        return exponent.equals(mono.exponent) && expMap.equals(mono.expMap);
    }
    
    public int hashCode() {
        return Objects.hash(exponent, expMap);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(coefficient);
        sb.append("*");
        sb.append("x");
        sb.append("^");
        sb.append(exponent);
        for (Poly poly : expMap.keySet()) {
            sb.append("*");
            sb.append("exp((");
            sb.append(poly.toString());
            sb.append("))");
            sb.append("^");
            sb.append(expMap.get(poly));
        }
        return sb.toString();
        //TODO poly调用mono的toString方法，mono作为基本项，coe*x^power*exp(poly)^power
    }
    
    public void setCoefficient(BigInteger coe) {
        coefficient = coe;
    }
    
    public void setExpMap(HashMap<Poly, BigInteger> hashMap) {
        expMap = hashMap;
    }
}
