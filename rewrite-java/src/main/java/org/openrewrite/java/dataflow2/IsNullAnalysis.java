package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;

import java.util.Collection;
import java.util.List;

import static org.openrewrite.java.dataflow2.Ternary.CantTell;
import static org.openrewrite.java.dataflow2.Ternary.DefinitelyNo;

public class IsNullAnalysis extends DataFlowAnalysis<Ternary> {
    private static final MethodMatcher TO_UPPER_CASE = new MethodMatcher("java.lang.String toUpperCase()");

    public Ternary join(Collection<Ternary> outs) {
        Ternary result = null;
        for (Ternary out : outs) {
            if ((result == Ternary.DefinitelyYes && out != Ternary.DefinitelyYes) ||
                    (result == DefinitelyNo && out != DefinitelyNo)) {
                return CantTell;
            } else if (result == CantTell) {
                return result;
            }
            result = out;
        }
        return result == null ? CantTell : result;
    }

    public Ternary defaultTransfer(Cursor c, JavaType.Variable v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Ternary transferBinary(Cursor c, JavaType.Variable storeOfInterest) {
        J.Binary pp = c.getValue();
        return inputState(c, storeOfInterest);
    }

    public Ternary transferNamedVariable(Cursor c, JavaType.Variable storeOfInterest) {
        return inputState(c, storeOfInterest);
    }

    public Ternary transferAssignment(Cursor c, JavaType.Variable storeOfInterest) {
        J.Assignment a = c.getValue();
        if (a.getVariable() instanceof J.Identifier) {
            J.Identifier ident = (J.Identifier) a.getVariable();
            if (ident.getFieldType() == storeOfInterest) {
                return outputState(new Cursor(c, a.getAssignment()), storeOfInterest);
            } else {
                return inputState(c, storeOfInterest);
            }
        } else {
            return CantTell;
        }
    }

    public Ternary transferMethodInvocation(Cursor c, JavaType.Variable storeOfInterest) {
        return TO_UPPER_CASE.matches((J.MethodInvocation) c.getValue()) ? DefinitelyNo : CantTell;
    }

    @Override
    public Ternary transferLiteral(Cursor c, JavaType.Variable storeOfInterest) {
        J.Literal pp = c.getValue();
        if (pp.getValue() == null) {
            return Ternary.DefinitelyYes;
        } else {
            return DefinitelyNo;
        }
    }

    public Ternary transferIdentifier(Cursor c, JavaType.Variable storeOfInterest) {
        J.Identifier pp = c.getValue();
        return inputState(c, storeOfInterest);
    }

    public Ternary transferEmpty(Cursor c, JavaType.Variable storeOfInterest) {
        return inputState(c, storeOfInterest);
    }

    @Override
    public Ternary transferIf(Cursor c, JavaType.Variable storeOfInterest) {
        return inputState(c, storeOfInterest);
    }

    @Override
    public Ternary transferIfElse(Cursor c, JavaType.Variable storeOfInterest) {
        J.If.Else ifElse = c.getValue();
        return outputState(new Cursor(c, ifElse.getBody()), storeOfInterest);
    }

    @Override
    public Ternary transferBlock(Cursor c, JavaType.Variable storeOfInterest) {
        J.Block block = c.getValue();
        List<Statement> stmts = block.getStatements();
        if (stmts.size() > 0) {
            return outputState(new Cursor(c, stmts.get(stmts.size() - 1)), storeOfInterest);
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
    }
}

