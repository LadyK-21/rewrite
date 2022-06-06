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
    public ProgramState defaultTransfer(Cursor c) {
        // For development only, to make sure all cases are covered
        throw new UnsupportedOperationException();
    }

    @Override
    public ProgramState transferBinary(Cursor c) {
        return inputState(c).push(DefinitelyNo);
    }

    public ProgramState transferNamedVariable(Cursor c) {
        J.VariableDeclarations.NamedVariable v = c.getValue();
        JavaType.Variable t = v.getVariableType();
        if(v.getInitializer() != null) {
            ProgramState s = outputState(new Cursor(c, v.getInitializer()));
            return s.set(t, s.expr()).pop();
        } else {
            ProgramState s = inputState(c);
            assert !s.getMap().containsKey(t);
            return s.set(t, DefinitelyYes);
        }
    }

    @Override
    public ProgramState transferAssignment(Cursor c) {

        // id = expr

        J.Assignment a = c.getValue();
        if (a.getVariable() instanceof J.Identifier) {
            J.Identifier ident = (J.Identifier) a.getVariable();
//            if (ident.getFieldType() == state) {
//                return outputState(new Cursor(c, a.getAssignment()));
//            } else {
//                return inputState(c);
//            }
            ProgramState s = outputState(new Cursor(c, a.getAssignment()));
            return s.set(ident.getFieldType(), s.expr()).pop();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public ProgramState transferAssignmentOperation(Cursor pp) {
        return defaultTransfer(pp);
    }

    private static final String[] definitelyNonNullReturningMethodSignatures = new String[] {
        "java.lang.String toUpperCase()"
    };

    private static final List<MethodMatcher> definitelyNonNullReturningMethodMatchers =
            Arrays.stream(definitelyNonNullReturningMethodSignatures).map(MethodMatcher::new).collect(Collectors.toList());

    public ProgramState transferMethodInvocation(Cursor c) {
        J.MethodInvocation method = c.getValue();
        for(MethodMatcher matcher : definitelyNonNullReturningMethodMatchers) {
            if (matcher.matches(method)) {
                return inputState(c).push(DefinitelyNo);
            }
        }
        return inputState(c).push(CantTell);
    }

    @Override
    public ProgramState transferLiteral(Cursor c) {
        J.Literal pp = c.getValue();
        ProgramState s = inputState(c);
        if (pp.getValue() == null) {
            return s.push(DefinitelyYes);
        } else {
            return s.push(DefinitelyNo);
        }
    }

    public ProgramState transferIdentifier(Cursor c) {
        J.Identifier i = c.getValue();
        ProgramState s = inputState(c);
        Ternary v = s.get(i.getFieldType());
        return inputState(c).push(v);
    }

    public ProgramState transferEmpty(Cursor c) {
        return inputState(c);
    }

    @Override
    public ProgramState transferIf(Cursor c) {
        return inputState(c);
    }

    @Override
    public ProgramState transferIfElse(Cursor c) {
        J.If.Else ifElse = c.getValue();
        return outputState(new Cursor(c, ifElse.getBody()));
    }

    @Override
    public ProgramState transferBlock(Cursor c) {
        J.Block block = c.getValue();
        List<Statement> stmts = block.getStatements();
        if (stmts.size() > 0) {
            return outputState(new Cursor(c, stmts.get(stmts.size() - 1)));
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
    }

    public ProgramState transferWhileLoop(Cursor c) {
        return inputState(c);
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

    public ProgramState transferParentheses(Cursor c) {
        J.Parentheses paren = c.getValue();
        return outputState(new Cursor(c, paren.getTree()));
        //return inputState(c);
    }

    public ProgramState transferControlParentheses(Cursor c) {
        return inputState(c);
    }
}

