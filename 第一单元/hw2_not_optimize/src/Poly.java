import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class Poly {
    private final HashMap<Mono, BigInteger> monoMap; //mono与coe
    
    public Poly() {
        this.monoMap = new HashMap<>();
    }
    
    public void setMonoMap(HashMap<Mono, BigInteger> hashMap) {
        monoMap.putAll(hashMap);
    }
    
    public void addMono(Mono mono) { //最小单元的添加方法
        if (monoMap.containsKey(mono)) { //有这个mono
            BigInteger coefficient = monoMap.get(mono);
            coefficient = coefficient.add(mono.getCoefficient());//生成新的系数
            mono.setCoefficient(coefficient);//改变最小项系数;使得符合输出逻辑
            monoMap.remove(mono);
            monoMap.put(mono, coefficient);
            //monoMap.replace(mono, coefficient);//将系数合并后的mono放进去;
        } else { //没有这个mono
            monoMap.put(mono, mono.getCoefficient());
        }
    }
    
    public Poly addPoly(Poly poly) { //poly的相加转化成mono的相加,mulPoly的基准
        Poly resultPoly = new Poly();
        for (Mono mono : this.monoMap.keySet()) {
            resultPoly.addMono(mono);
        }
        for (Mono mono : poly.monoMap.keySet()) {
            resultPoly.addMono(mono);
        }
        return resultPoly;
    }
    
    public Poly mulPoly(Poly poly) { //poly的相乘转换为一个一个的单项式相乘
        Poly resultPoly = new Poly();
        for (Mono mono : this.monoMap.keySet()) {
            for (Mono mono1 : poly.monoMap.keySet()) {
                resultPoly.addMono(mono.mulMono(mono1));
            }
        }
        return resultPoly;
    }
    
    public Poly powPoly(int sum) { //以mulPoly作为基础
        Poly operatePoly = new Poly();
        HashMap<Mono, BigInteger> operateHashMap = new HashMap<>(monoMap);
        Poly resultPoly = new Poly();
        HashMap<Mono, BigInteger> resultHashMap = new HashMap<>(monoMap);
        operatePoly.setMonoMap(operateHashMap);
        resultPoly.setMonoMap(resultHashMap);
        if (sum == 0) {
            monoMap.clear();
            monoMap.put(new Mono(new BigInteger("1"), new BigInteger("0")), new BigInteger("1"));
        } else {
            for (int i = 1; i < sum; i++) {
                resultPoly = resultPoly.mulPoly(operatePoly);
            }
            return resultPoly;
        }
        return this;
    }
    
    public Poly negate() {
        Poly poly = new Poly();
        poly.addMono(new Mono(new BigInteger("-1"), new BigInteger("0")));
        return this.mulPoly(poly);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<Mono> iterator = monoMap.keySet().iterator();
        Mono mono = iterator.next();
        sb.append(mono.toString());
        while (iterator.hasNext()) {
            Mono newMono = iterator.next();
            if (newMono.getCoefficient().compareTo(BigInteger.ZERO) > 0) {
                sb.append("+");
            } //防止出现+-号
            sb.append(newMono.toString());
        }
        return sb.toString();
    }
    
    public Poly ExpMulPoly(BigInteger sum) { //exp(x+1)^2->exp(2*x+2)
        Poly operatePoly = new Poly();
        operatePoly.addMono(new Mono(sum, new BigInteger("0")));
        return this.mulPoly(operatePoly);
    }
    
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Poly poly = (Poly) object;
        return this.monoMap.keySet().equals(poly.monoMap.keySet());
    }
    
    public int hashCode() {
        return Objects.hash(monoMap);
    }
}
