package factor;

import java.util.ArrayList;

public class Term {
    private ArrayList<Factor> factorList;

    public Term() {
        this.factorList = new ArrayList<>();
    }

    public Term(ArrayList<Factor> factorList) {
        this.factorList = factorList;
    }

    public Term CopyTerm() {
        Unit thisUnit = (Unit) this.factorList.get(0);
        ArrayList<Factor> newFactorList = new ArrayList<>();
        newFactorList.add(thisUnit.CopyUnit());

        return new Term(newFactorList);
    }

    public void TermInversion() {
        for (Factor factor : factorList) {
            if (factor.getClass().getSimpleName().equals("Unit")) {
                ((Unit) factor).UnitInversion();
            }
        }
    }

    public Expression TermSimplify() {
        Expression expression = new Expression();

        int unitPos = -1;
        for (int i = 0; i < factorList.size(); i++) {
            if (factorList.get(i).getClass().getSimpleName().equals("Unit")) {
                unitPos = i;

                Factor unitFactor = factorList.get(unitPos);
                Term unitTerm = new Term();
                unitTerm.factorList.add(unitFactor);
                expression.AddTerm(unitTerm, true);

                break;
            }
        }

        if (factorList.size() > 1) {
            for (int i = 0; i < factorList.size(); i++) {
                if (i == unitPos) {
                    continue;
                }
                expression = expression.MulExpr((Expression) factorList.get(i));
            }
        }

        return expression;
    }

    public boolean IfSameTerm(Term otherTerm) {
        if (this.factorList.size() > 1 || otherTerm.factorList.size() > 1) {
            return false;
        } else {
            Unit thisUnit = (Unit) this.factorList.get(0);
            Unit otherUnit = (Unit) otherTerm.factorList.get(0);

            return thisUnit.IfSameUnit(otherUnit);
        }
    }

    public void AddTerm(Term otherTerm) {
        Unit thisUnit = (Unit) this.factorList.get(0);
        Unit otherUnit = (Unit) otherTerm.factorList.get(0);

        thisUnit.AddUnit(otherUnit);
    }

    public Term MulTerm(Term otherTerm) {
        Unit thisUnit = (Unit) this.factorList.get(0);
        Unit otherUnit = (Unit) otherTerm.factorList.get(0);

        thisUnit.MulUnit(otherUnit);
        return this;
    }

    public void AddFactor(Factor newFactor) {
        if (newFactor.getClass().getSimpleName().equals("Unit")) {
            Unit newUnit = (Unit) newFactor;
            boolean haveAdded = false;
            for (Factor factor : factorList) {
                if (factor.getClass().getSimpleName().equals("Unit")) {
                    Unit thisUnit = (Unit) factor;
                    thisUnit.MulUnit(newUnit);
                    haveAdded = true;
                    break;
                }
            }
            if (!haveAdded) {
                factorList.add(newFactor);
            }
        } else {
            factorList.add(newFactor);
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        int factorListSize = factorList.size();
        for (int i = 0; i < factorListSize; i++) {
            stringBuilder.append(factorList.get(i).toString());
            if (i < factorListSize - 1) {
                stringBuilder.append("*");
            }
        }

        return stringBuilder.toString();
    }
}
