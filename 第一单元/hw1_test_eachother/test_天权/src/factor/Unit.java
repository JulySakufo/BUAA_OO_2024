package factor;

import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;

public class Unit implements Factor {
    private BigInteger cofficient;
    private TreeMap<String, Polynome> polyList;

    public Unit(BigInteger cofficient) {
        this.cofficient = cofficient;
        this.polyList = new TreeMap<>();
    }

    public Unit(Polynome polynome) {
        this.cofficient = BigInteger.ONE;
        this.polyList = new TreeMap<>();
        this.polyList.put(polynome.GetPolyName(), polynome);
    }

    public Unit(BigInteger cofficient, TreeMap<String, Polynome> polyList) {
        this.cofficient = cofficient;
        this.polyList = polyList;
    }

    public Unit CopyUnit() {
        TreeMap<String, Polynome> newPolyList = new TreeMap<>();
        for (Map.Entry<String, Polynome> entry : this.polyList.entrySet()) {
            newPolyList.put(entry.getKey(), entry.getValue().CopyPoly());
        }
        return new Unit(this.cofficient, newPolyList);
    }

    public void UnitInversion() {
        this.cofficient = this.cofficient.negate();
    }

    public boolean IfSameUnit(Unit otherUnit) {
        if (this.polyList.size() != otherUnit.polyList.size()) {
            return false;
        }

        TreeMap<String, Polynome> otherList = otherUnit.polyList;
        for (Map.Entry<String, Polynome> entry : polyList.entrySet()) {
            if (!otherList.containsKey(entry.getKey()) ||
                    !entry.getValue().IfSamePoly(otherList.get(entry.getKey()))) {
                return false;
            }
        }

        return true;
    }

    public void AddUnit(Unit otherUnit) {
        this.cofficient = this.cofficient.add(otherUnit.cofficient);

        if (this.cofficient.equals(BigInteger.ZERO)) {
            this.polyList.clear();
        }
    }

    public void MulUnit(Unit otherUnit) {
        this.cofficient = this.cofficient.multiply(otherUnit.cofficient);

        TreeMap<String, Polynome> otherList = otherUnit.polyList;
        for (Map.Entry<String, Polynome> entry : otherList.entrySet()) {
            Polynome newPoly = entry.getValue();
            if (polyList.containsKey(newPoly.GetPolyName())) {
                polyList.get(newPoly.GetPolyName()).MulPoly(newPoly.GetPolyPower());
            } else {
                polyList.put(newPoly.GetPolyName(), newPoly);
            }
        }

        if (this.cofficient.equals(BigInteger.ZERO)) {
            this.polyList.clear();
        }
    }

    @Override
    public Factor CopyFactor() {
        return this.CopyUnit();
    }

    @Override
    public String toString() {
        if (cofficient.equals(BigInteger.ZERO)) {
            return "0";
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (polyList.isEmpty()) {
            stringBuilder.append(cofficient);
        } else {
            boolean isFirst = true;
            if (cofficient.equals(BigInteger.ONE) ||
                    cofficient.equals(BigInteger.valueOf(-1))) {

                stringBuilder.append(cofficient.equals(BigInteger.valueOf(-1)) ? "-" : "");
            } else {
                stringBuilder.append(cofficient);
                isFirst = false;
            }

            for (Map.Entry<String, Polynome> entry : polyList.entrySet()) {
                if (!entry.getValue().GetPolyPower().equals(BigInteger.ZERO)) {
                    stringBuilder.append(isFirst ? "" : "*");
                    stringBuilder.append(entry.getValue().toString());
                    isFirst = false;
                }
            }
        }
        return stringBuilder.toString();
    }
}
