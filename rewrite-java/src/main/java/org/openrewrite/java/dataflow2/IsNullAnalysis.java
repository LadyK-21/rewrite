package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.openrewrite.java.dataflow2.Ternary.*;

public class IsNullAnalysis extends DataFlowAnalysis<Ternary> {

    @Override
    public Ternary join(Collection<Ternary> outs) {
        return Ternary.join(outs);
    }

    @Override
    public Ternary defaultTransfer(Cursor c, JavaType.Variable v) {
        // For development only, to make sure all cases are covered
        throw new UnsupportedOperationException();
    }

    @Override
    public Ternary transferBinary(Cursor c, JavaType.Variable storeOfInterest) {
        return DefinitelyNo;
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

    private static final String[] definitelyNonNullReturningMethodSignatures = new String[] {
        "java.lang.String toUpperCase()"
    };

    private static final List<MethodMatcher> definitelyNonNullReturningMethodMatchers =
            Arrays.stream(definitelyNonNullReturningMethodSignatures).map(MethodMatcher::new).collect(Collectors.toList());

    public Ternary transferMethodInvocation(Cursor c, JavaType.Variable storeOfInterest) {
        J.MethodInvocation method = c.getValue();
        for(MethodMatcher matcher : definitelyNonNullReturningMethodMatchers) {
            if (matcher.matches(method)) return DefinitelyNo;
        }
        return CantTell;
    }

    @Override
    public Ternary transferLiteral(Cursor c, JavaType.Variable storeOfInterest) {
        J.Literal pp = c.getValue();
        if (pp.getValue() == null) {
            return DefinitelyYes;
        } else {
            return DefinitelyNo;
        }
    }

    public Ternary transferIdentifier(Cursor c, JavaType.Variable storeOfInterest) {
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

