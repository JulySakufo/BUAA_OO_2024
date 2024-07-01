import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class Poly {
    private final HashMap<Mono, BigInteger> monoMap; //mono与coe
    
    public Poly() {
        this.monoMap = new HashMap<>();
    }
    
    public Poly(HashMap<Mono, BigInteger> newMonoMap) {
        this.monoMap = new HashMap<>();
        for (Mono mono : newMonoMap.keySet()) {
            Mono newMono = new Mono(mono.getCoefficient(), mono.getExponent());
            Poly newPoly = new Poly(mono.getPoly().getMonoMap());
            newMono.setPoly(newPoly);
            monoMap.put(newMono, newMono.getCoefficient());
        }
    }
    
    public void addMono(Mono mono) { //最小单元的添加方法
        if (monoMap.containsKey(mono)) { //有这个mono
            BigInteger coefficient = monoMap.get(mono);
            BigInteger newCoe = coefficient.add(mono.getCoefficient());//生成新的系数
            Mono newMono = new Mono(newCoe, mono.getExponent());
            newMono.setPoly(new Poly(mono.getPoly().getMonoMap()));
            monoMap.remove(mono);
            monoMap.put(newMono, newCoe);
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
        for (Mono mono : monoMap.keySet()) {
            operatePoly.addMono(mono);
        }
        Poly resultPoly = new Poly();
        for (Mono mono : monoMap.keySet()) {
            resultPoly.addMono(mono);
        }
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
        if (iterator.hasNext()) {
            Mono mono = iterator.next();
            sb.append(mono.toString()); //将poly的toString放成mono的toString实现层次性，不一定有输出
            while (iterator.hasNext()) {
                Mono newMono = iterator.next();
                if (newMono.getCoefficient().compareTo(BigInteger.ZERO) > 0) {
                    if (!sb.toString().equals("")) { //可能最外层没有输出
                        sb.append("+");
                    }
                } //防止出现+-号,系数为0无输出
                sb.append(newMono.toString());
            }
        }
        if (sb.toString().equals("")) { //如果无输出
            return "0";
        }
        return sb.toString();
    }
    
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || (getClass() != object.getClass())) {
            return false;
        }
        Poly poly = (Poly) object;
        return this.monoMap.equals(poly.monoMap);
    }
    
    public int hashCode() {
        return Objects.hash(monoMap);
    }
    
    public boolean isFactor() {
        if (monoMap.size() == 1) { //如果装的mono多于1个，必不是factor
            for (Mono mono : monoMap.keySet()) {
                return mono.isFactor(); //检验mono本身是否是因子
            }
        }
        return false;
    }
    
    public Poly expMulPoly(BigInteger exponent) { //指数项的合并
        Poly operatePoly = new Poly();
        Mono newMono = new Mono(exponent, BigInteger.ZERO);//exp((x^2+1))^2，把2乘进去
        operatePoly.addMono(newMono);
        newMono.updateExpMap(this.mulPoly(operatePoly));
        newMono.setCoefficient(BigInteger.ONE);//用于乘法的系数回退至1
        Poly resultPoly = new Poly();
        Mono newMono1 = new Mono(BigInteger.ONE, BigInteger.ZERO);
        resultPoly.addMono(newMono1);
        return resultPoly.mulPoly(operatePoly);//使用已有的工具，避免重复造轮子
    }
    
    public HashMap<Mono, BigInteger> getMonoMap() {
        return monoMap;
    }
    
    public Poly deriPoly() {
        Poly resultPoly = new Poly();
        for (Mono mono : monoMap.keySet()) {
            resultPoly = resultPoly.addPoly(mono.deriMono());
        }
        return resultPoly;
    }
}
