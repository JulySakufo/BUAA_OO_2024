package factor;

import java.util.ArrayList;
import java.util.Iterator;

public class Expression implements Factor {
    private ArrayList<Term> termList;

    public Expression() {
        this.termList = new ArrayList<>();
    }

    public Expression(ArrayList<Term> newTermList) {
        this.termList = newTermList;
    }

    public Expression ExprSimplify() {
        Expression finalExpr = new Expression();
        for (Term term : termList) {
            finalExpr.AddExpression(term.TermSimplify());
        }

        return finalExpr;
    }

    public void AddExpression(Expression newExpr) {
        for (Term newTerm : newExpr.termList) {
            this.AddTerm(newTerm, true);
        }
    }

    public void AddTerm(Term newTerm, boolean isAdd) {
        boolean havaAdded = false;
        if (!isAdd) {
            newTerm.TermInversion();
        }

        for (Term term : termList) {
            if (term.IfSameTerm(newTerm)) {
                term.AddTerm(newTerm);
                havaAdded = true;
                break;
            }
        }

        if (!havaAdded) {
            termList.add(newTerm);
        }
    }

    public Expression MulExpr(Expression otherExpr) {
        Expression finalExpression = new Expression();

        for (Term thisTerm : termList) {
            for (Term otherTerm : otherExpr.termList) {
                finalExpression.AddTerm(thisTerm.CopyTerm().MulTerm(otherTerm), true);
            }
        }

        return finalExpression;
    }

    @Override
    public Factor CopyFactor() {
        ArrayList<Term> newList = new ArrayList<>();
        for (Term term : termList) {
            newList.add(term.CopyTerm());
        }

        return new Expression(newList);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean havePrinted = false;
        int skipPos = -1;

        Iterator<Term> iterator = termList.iterator();
        while (iterator.hasNext()) {
            Term term = iterator.next();
            if ("0".equals(term.toString())) {
                iterator.remove();
            }
        }

        if (!termList.isEmpty() && termList.get(0).toString().charAt(0) == '-') {
            for (int i = 0; i < termList.size(); i++) {
                String termString = termList.get(i).toString();

                if (termString.charAt(0) != '-') {
                    stringBuilder.append(termString);
                    havePrinted = true;
                    skipPos = i;

                    break;
                }
            }
        }

        for (int i = 0; i < termList.size(); i++) {
            if (i == skipPos) {
                continue;
            }

            String termString = termList.get(i).toString();
            if (havePrinted && termString.charAt(0) != '-') {
                stringBuilder.append("+");
            }
            stringBuilder.append(termString);
            havePrinted = true;
        }

        if (!havePrinted || termList.isEmpty()) {
            stringBuilder.append("0");
        }

        return stringBuilder.toString();
    }
}
