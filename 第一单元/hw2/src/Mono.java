import java.math.BigInteger;
import java.util.Objects;

public class Mono { //最小单元a*x^b*exp(Factor)^c
    private BigInteger coefficient;//不作为输出，它的真实系数在poly的monomap里面
    private final BigInteger exponent;
    private Poly poly;
    
    public Mono(BigInteger coefficient, BigInteger exponent) {
        this.coefficient = coefficient;
        this.exponent = exponent;
        this.poly = new Poly();
    }
    
    public void updateExpMap(Poly p) { //直接加就完事
        for (Mono mono : p.getMonoMap().keySet()) {
            poly.addMono(mono);
        }
    }
    
    public Mono mulMono(Mono mono) { //expMap具有private属性，不放入poly类中
        BigInteger coe = mono.getCoefficient();
        BigInteger power = mono.getExponent();
        Mono newMono = new Mono(coe.multiply(coefficient), power.add(exponent));
        newMono.updateExpMap(this.poly);
        newMono.updateExpMap(mono.poly);
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
        if ((object == null) || (getClass() != object.getClass())) {
            return false;
        }
        Mono mono = (Mono) object;
        if ((poly.getMonoMap().isEmpty()) && (mono.poly.getMonoMap().isEmpty())) {
            return true;
        }
        return exponent.equals(mono.exponent) && poly.equals(mono.poly);
    }
    
    public int hashCode() {
        return Objects.hash(exponent, poly);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (coefficient.compareTo(BigInteger.ONE) > 0) { //系数大于1
            coeGtOne(sb);
        } else if (coefficient.compareTo(BigInteger.ONE) == 0) {
            coeEqOne(sb);
        } else if (coefficient.compareTo(new BigInteger("-1")) < 0) { //系数小于-1
            coeLeNeqOne(sb);
        } else if (coefficient.compareTo(new BigInteger("-1")) == 0) { //系数等于-1
            coeEqNegOne(sb);
        } //系数等于0无任何输出，逻辑交予poly判定
        return sb.toString();
        //TODO poly调用mono的toString方法，mono作为基本项，coe*x^power*exp(poly)^power
    }
    
    public void coeGtOne(StringBuilder sb) { //系数大于1
        sb.append(coefficient);
        if (exponent.compareTo(BigInteger.ZERO) == 0) { //2*x^0*exp(())^?
            //Do nothing extra
        } else if (exponent.compareTo(BigInteger.ONE) == 0) { //2*x^1*exp(())^?
            sb.append("*x");
        } else { //2*x^?*exp(())^?
            sb.append("*x^");
            sb.append(exponent);
        }
        if (!poly.getMonoMap().isEmpty()) {
            checkExp(sb);
        }
    }
    
    public void coeEqOne(StringBuilder sb) { //系数等于1
        if (exponent.compareTo(BigInteger.ZERO) == 0) { //1*x^0*exp(())^?
            if (poly.getMonoMap().isEmpty()) { //没有指数项 1*x^0
                sb.append("1");
            } else { //TODO:exp内部的括号的消除才是化简的关键
                if (poly.isFactor()) {
                    sb.append("exp(");
                    sb.append(poly.toString());
                    sb.append(")");
                } else {
                    sb.append("exp((");
                    sb.append(poly.toString());
                    sb.append("))");
                }
                return;
            }
        } else if (exponent.compareTo(BigInteger.ONE) == 0) { //1*x^1*exp(())
            sb.append("x");
        } else { //1*x^?*exp(())^?
            sb.append("x^");
            sb.append(exponent);
        }
        if (!poly.getMonoMap().isEmpty()) {
            checkExp(sb);
        }
    }
    
    public void coeEqNegOne(StringBuilder sb) { //系数等于-1
        if (exponent.compareTo(BigInteger.ZERO) == 0) { //-1*x^0
            if (poly.getMonoMap().isEmpty()) { //没有指数项
                sb.append("-1");
            } else {
                sb.append("-");
                if (poly.isFactor()) {
                    sb.append("exp(");
                    sb.append(poly.toString());
                    sb.append(")");
                } else {
                    sb.append("exp((");
                    sb.append(poly.toString());
                    sb.append("))");
                }
                return;
            }
        } else if (exponent.compareTo(BigInteger.ONE) == 0) { //-1*x^1*exp(())
            sb.append("-x");
        } else { //-1*x^?*exp(())^?
            sb.append("-x^");
            sb.append(exponent);
        }
        if (!poly.getMonoMap().isEmpty()) {
            checkExp(sb);
        }
    }
    
    public void coeLeNeqOne(StringBuilder sb) { //系数小于-1
        sb.append(coefficient);
        if (exponent.compareTo(BigInteger.ZERO) == 0) { //-2*x^0
            //do nothing
        } else if (exponent.compareTo(BigInteger.ONE) == 0) { //-2*x^1
            sb.append("*x");
        } else { //-2*x^?
            sb.append("*x^");
            sb.append(exponent);
        }
        if (!poly.getMonoMap().isEmpty()) {
            checkExp(sb);
        }
    }
    
    public void setCoefficient(BigInteger coe) {
        coefficient = coe;
    }
    
    public boolean isFactor() {
        if (exponent.equals(BigInteger.ZERO) && poly.getMonoMap().isEmpty()) { //只有单纯的系数
            return true;
        } else if ((exponent.equals(BigInteger.ZERO)) || poly.getMonoMap().isEmpty()) { //系数为1的特判
            if (coefficient.equals(BigInteger.ONE)) {
                return true;
            } else if (coefficient.compareTo(new BigInteger("-1")) == 0) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
    
    public void checkExp(StringBuilder sb) {
        if (poly.isFactor()) {
            sb.append("*exp(");
            sb.append(poly.toString());
            sb.append(")");
        } else {
            sb.append("*exp((");
            sb.append(poly.toString());
            sb.append("))");
        }
    }
    
    public Poly getPoly() {
        return poly;
    }
    
    public void setPoly(Poly poly) {
        this.poly = poly;
    }
}
