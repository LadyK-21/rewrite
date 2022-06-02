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

import static org.openrewrite.java.dataflow2.ProgramState.*;
import static org.openrewrite.java.dataflow2.Ternary.DefinitelyNo;
import static org.openrewrite.java.dataflow2.Ternary.DefinitelyYes;

public class IsNullAnalysis extends DataFlowAnalysis<ProgramState> {

    @Override
    public ProgramState join(Collection<ProgramState> outs) {
        return ProgramState.join(outs);
    }

    @Override
    public ProgramState defaultTransfer(Cursor c, ProgramState state) {
        // For development only, to make sure all cases are covered
        throw new UnsupportedOperationException();
    }

    @Override
    public ProgramState transferBinary(Cursor c, ProgramState state) {
        return state.push(DefinitelyNo);
    }

    public ProgramState transferNamedVariable(Cursor c, ProgramState state) {
        return inputState(c, state);
    }

    @Override
    public ProgramState transferAssignment(Cursor c, ProgramState state) {
        J.Assignment a = c.getValue();
        if (a.getVariable() instanceof J.Identifier) {
            J.Identifier ident = (J.Identifier) a.getVariable();
//            if (ident.getFieldType() == state) {
//                return outputState(new Cursor(c, a.getAssignment()), state);
//            } else {
//                return inputState(c, state);
//            }
            ProgramState s = outputState(new Cursor(c, a.getAssignment()), state);
            return s.set(ident.getFieldType(), s.expr()).pop();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public ProgramState transferAssignmentOperation(Cursor pp, ProgramState state) {
        return defaultTransfer(pp, state);
    }

    private static final String[] definitelyNonNullReturningMethodSignatures = new String[] {
        "java.lang.String toUpperCase()"
    };

    private static final List<MethodMatcher> definitelyNonNullReturningMethodMatchers =
            Arrays.stream(definitelyNonNullReturningMethodSignatures).map(MethodMatcher::new).collect(Collectors.toList());

    public ProgramState transferMethodInvocation(Cursor c, ProgramState state) {
        J.MethodInvocation method = c.getValue();
        for(MethodMatcher matcher : definitelyNonNullReturningMethodMatchers) {
            if (matcher.matches(method)) {
                return state.push(DefinitelyNo);
            }
        }
        //return CantTell;
        throw new UnsupportedOperationException();
    }

    @Override
    public ProgramState transferLiteral(Cursor c, ProgramState state) {
        J.Literal pp = c.getValue();
        if (pp.getValue() == null) {
            return state.push(DefinitelyYes);
        } else {
            return state.push(DefinitelyNo);
        }
    }

    public ProgramState transferIdentifier(Cursor c, ProgramState state) {
        return inputState(c, state);
    }

    public ProgramState transferEmpty(Cursor c, ProgramState state) {
        return inputState(c, state);
    }

    @Override
    public ProgramState transferIf(Cursor c, ProgramState state) {
        return inputState(c, state);
    }

    @Override
    public ProgramState transferIfElse(Cursor c, ProgramState state) {
        J.If.Else ifElse = c.getValue();
        return outputState(new Cursor(c, ifElse.getBody()), state);
    }

    @Override
    public ProgramState transferBlock(Cursor c, ProgramState state) {
        J.Block block = c.getValue();
        List<Statement> stmts = block.getStatements();
        if (stmts.size() > 0) {
            return outputState(new Cursor(c, stmts.get(stmts.size() - 1)), state);
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
    }

    public ProgramState transferWhileLoop(Cursor c, ProgramState state) {
        return inputState(c, state);
    }
//
//    public S transferForLoop(Cursor pp, ProgramState state) {
//        return defaultTransfer(pp, state);
//    }
//
//    public S transferForLoopControl(Cursor pp, ProgramState state) {
//        return defaultTransfer(pp, state);
//    }
//
//    public S transferVariableDeclarations(Cursor pp, ProgramState state) {
//        return defaultTransfer(pp, state);
//    }
//
//    public S transferUnary(Cursor pp, ProgramState state) {
//        return defaultTransfer(pp, state);
//    }

    public ProgramState transferParentheses(Cursor c, ProgramState state) {
        return inputState(c, state);
    }

    public ProgramState transferControlParentheses(Cursor c, ProgramState state) {
        return inputState(c, state);
    }
}

