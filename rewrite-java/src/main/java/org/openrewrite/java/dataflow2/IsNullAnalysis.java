package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.openrewrite.java.dataflow2.ProgramState.*;
import static org.openrewrite.java.dataflow2.Ternary.*;

public class IsNullAnalysis extends DataFlowAnalysis<ProgramState> {

    @Override
    public ProgramState join(Collection<ProgramState> outs) {
        return ProgramState.join(outs);
    }

    @Override
    public ProgramState defaultTransfer(Cursor c, TraversalControl<ProgramState> t) {
        // For development only, to make sure all cases are covered
        throw new UnsupportedOperationException();
    }

    @Override
    public ProgramState transferBinary(Cursor c, TraversalControl<ProgramState> t) {
        J.Binary binary = c.getValue();

        return inputState(c, t).push(DefinitelyNo);
    }

    @Override
    public ProgramState transferNamedVariable(Cursor c, TraversalControl<ProgramState> tc) {
        J.VariableDeclarations.NamedVariable v = c.getValue();
        JavaType.Variable t = v.getVariableType();
        if(v.getInitializer() != null) {
            ProgramState s = outputState(new Cursor(c, v.getInitializer()), tc);
            return s.set(t, s.expr()).pop();
        } else {
            ProgramState s = inputState(c, tc);
            assert !s.getMap().containsKey(t);
            return s.set(t, DefinitelyYes);
        }
    }

    @Override
    public ProgramState transferAssignment(Cursor c, TraversalControl<ProgramState> t) {

        // id = expr

        J.Assignment a = c.getValue();
        if (a.getVariable() instanceof J.Identifier) {
            J.Identifier ident = (J.Identifier) a.getVariable();
//            if (ident.getFieldType() == state) {
//                return outputState(new Cursor(c, a.getAssignment()));
//            } else {
//                return inputState(c);
//            }
            ProgramState s = outputState(new Cursor(c, a.getAssignment()), t);
            return s.set(ident.getFieldType(), s.expr());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public ProgramState transferAssignmentOperation(Cursor pp, TraversalControl<ProgramState> t) {
        return defaultTransfer(pp, t);
    }

    private static final String[] definitelyNonNullReturningMethodSignatures = new String[] {
        "java.lang.String toUpperCase()"
    };

    private static final List<MethodMatcher> definitelyNonNullReturningMethodMatchers =
            Arrays.stream(definitelyNonNullReturningMethodSignatures).map(MethodMatcher::new).collect(Collectors.toList());

    @Override
    public ProgramState transferMethodInvocation(Cursor c, TraversalControl<ProgramState> t) {
        J.MethodInvocation method = c.getValue();
        for(MethodMatcher matcher : definitelyNonNullReturningMethodMatchers) {
            if (matcher.matches(method)) {
                return inputState(c, t).push(DefinitelyNo);
            }
        }
        return inputState(c, t).push(CantTell);
    }

    @Override
    public ProgramState transferLiteral(Cursor c, TraversalControl<ProgramState> t) {
        J.Literal pp = c.getValue();
        ProgramState s = inputState(c, t);
        if (pp.getValue() == null) {
            return s.push(DefinitelyYes);
        } else {
            return s.push(DefinitelyNo);
        }
    }

    @Override
    public ProgramState transferIdentifier(Cursor c, TraversalControl<ProgramState> t) {
        J.Identifier i = c.getValue();
        ProgramState s = inputState(c, t);
        Ternary v = s.get(i.getFieldType());
        return inputState(c, t).push(v);
    }

    @Override
    public ProgramState transferEmpty(Cursor c, TraversalControl<ProgramState> t) {
        return inputState(c, t);
    }

    @Override
    public ProgramState transferIf(Cursor c, TraversalControl<ProgramState> t) {
        return inputState(c, t);
    }

    @Override
    public ProgramState transferIfElse(Cursor c, TraversalControl<ProgramState> t) {
        J.If.Else ifElse = c.getValue();
        return outputState(new Cursor(c, ifElse.getBody()), t);
    }

    @Override
    public ProgramState transferBlock(Cursor c, TraversalControl<ProgramState> t) {
        J.Block block = c.getValue();
        List<Statement> stmts = block.getStatements();
        if (stmts.size() > 0) {
            return outputState(new Cursor(c, stmts.get(stmts.size() - 1)), t);
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
    }

    @Override
    public ProgramState transferWhileLoop(Cursor c, TraversalControl<ProgramState> t) {
        return inputState(c, t);
    }
//
//    public S transferForLoop(Cursor pp) {
//        return defaultTransfer(pp);
//    }
//
//    public S transferForLoopControl(Cursor pp) {
//        return defaultTransfer(pp);
//    }
//
//    public S transferVariableDeclarations(Cursor pp) {
//        return defaultTransfer(pp);
//    }
//
//    public S transferUnary(Cursor pp) {
//        return defaultTransfer(pp);
//    }

    @Override
    public ProgramState transferParentheses(Cursor c, TraversalControl<ProgramState> t) {
        J.Parentheses paren = c.getValue();
        return outputState(new Cursor(c, paren.getTree()), t);
        //return inputState(c);
    }

    @Override
    public ProgramState transferControlParentheses(Cursor c, TraversalControl<ProgramState> t) {
        return inputState(c, t);
    }
}

