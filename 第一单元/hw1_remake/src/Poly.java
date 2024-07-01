import java.math.BigInteger;
import java.util.ArrayList;

public class Poly {
    private final ArrayList<Mono> monolist;
    
    public Poly() {
        this.monolist = new ArrayList<>();
    }
    
    public Poly addPoly(Poly poly) {
        ArrayList<Mono> arraylist = poly.getMonolist();
        for (Mono mono : arraylist) {
            BigInteger coefficient = mono.getCoefficient();
            BigInteger exponent = mono.getExponent();
            boolean flag = false;
            for (Mono mono1 : monolist) {
                BigInteger coefficient1 = mono1.getCoefficient();
                BigInteger exponent1 = mono1.getExponent();
                if (exponent.compareTo(exponent1) == 0) { //合并同类项
                    coefficient = coefficient.add(coefficient1);
                    Mono newMono = new Mono(coefficient, exponent);
                    monolist.set(monolist.indexOf(mono1), newMono);
                    flag = true;
                    break;
                }
            }
            if (!flag) { //如果没有同类项，直接加入
                monolist.add(mono);
            }
        }
        return this;
    }
    
    public Poly mulPoly(Poly poly) {
        Poly result = new Poly();
        ArrayList<Mono> arraylist = poly.getMonolist();
        for (Mono mono : arraylist) {
            BigInteger coefficient = mono.getCoefficient();
            BigInteger exponent = mono.getExponent();
            Poly operatePoly = new Poly();
            ArrayList<Mono> operateArraylist = new ArrayList<>();
            for (Mono mono1 : monolist) { //单项式*多项式，monolist已保证不会有同类项
                BigInteger coefficient1 = mono1.getCoefficient();
                BigInteger exponent1 = mono1.getExponent();
                operateArraylist.add(new Mono(coefficient.multiply(coefficient1), exponent.add(exponent1)));
            }
            operatePoly.setMonolist(operateArraylist);
            result.addPoly(operatePoly);
        }
        return result;
    }
    
    public Poly powPoly(int sum) {
        Poly poly = new Poly();
        ArrayList<Mono> arraylist = new ArrayList<>(monolist);
        Poly result = new Poly();
        ArrayList<Mono> resultArraylist = new ArrayList<>(monolist);
        poly.setMonolist(arraylist);
        result.setMonolist(resultArraylist);
        if (sum == 0) {
            monolist.clear();
            monolist.add(new Mono(new BigInteger("1"), new BigInteger("0")));//()^0直接返回1
        } else {
            for (int i = 1; i < sum; i++) {
                result = result.mulPoly(poly);
            }
            return result;
        }
        return this;
    }
    
    public ArrayList<Mono> getMonolist() {
        return monolist;
    }
    
    public void setMonolist(ArrayList<Mono> arraylist) {
        monolist.addAll(arraylist);
    }
    
    public Poly negate() {
        Poly poly = new Poly();
        ArrayList<Mono> arraylist = new ArrayList<>();
        Mono mono = new Mono(new BigInteger("-1"), new BigInteger("0"));
        arraylist.add(mono);
        poly.setMonolist(arraylist);
        return this.mulPoly(poly);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Mono firstMono = monolist.get(0);
        BigInteger coefficient = firstMono.getCoefficient();
        BigInteger exponent = firstMono.getExponent();
        sb.append(coefficient);
        sb.append("*");
        sb.append("x");
        sb.append("^");
        sb.append(exponent);
        for (int i = 1; i < monolist.size(); i++) {
            Mono mono = monolist.get(i);
            BigInteger monoCoefficient = mono.getCoefficient();
            BigInteger monoExponent = mono.getExponent();
            sb.append("+");
            sb.append(monoCoefficient);
            sb.append("*");
            sb.append("x");
            sb.append("^");
            sb.append(monoExponent);
        }
        return sb.toString();
    }
}
