package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;

import java.util.Collection;
import java.util.List;

/**
 * Common superclass for all analysis tracking the flow of values. By default, all transfer methods
 * pass the program state unchanged. In the case of assignment or variable initialization, the
 * state is updated accordingly.
 */
public abstract class ValueAnalysis<T> extends DataFlowAnalysis<T> {
    

    public ValueAnalysis(DataFlowGraph dfg, Joiner<T> joiner) {
        super(dfg, joiner);
    }

    @Override
    public ProgramState<T> join(List<ProgramState<T>> outs) {
        return ProgramState.join(joiner, outs);
    }

    @Override
    public ProgramState<T> defaultTransfer(Cursor c, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        throw new UnsupportedOperationException();
        //return inputState(c, t);
    }

    @Override
    public ProgramState<T> transferAssert(Cursor c, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return inputState.push(joiner.lowerBound());
    }

    @Override
    public abstract ProgramState<T> transferToIfThenElseBranches(J.If ifThenElse, ProgramState<T> state, String ifThenElseBranch);

    @Override
    public ProgramState<T> transferUnary(Cursor c, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return inputState.push(joiner.lowerBound());
    }

    @Override
    public ProgramState<T> transferBinary(Cursor c, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return inputState.push(joiner.lowerBound());
    }

    @Override
    public ProgramState<T> transferEmpty(Cursor c, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return inputState;
    }

    @Override
    public ProgramState<T> transferFieldAccess(Cursor c, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return inputState.push(joiner.lowerBound());
    }

    @Override
    public ProgramState<T> transferNamedVariable(Cursor c, ProgramState<T> inputState, TraversalControl<ProgramState<T>> tc) {
        J.VariableDeclarations.NamedVariable v = c.getValue();
        JavaType.Variable t = v.getVariableType();
        if (v.getInitializer() != null) {
            //ProgramState s = analysis(v.getInitializer());
            return inputState.set(t, inputState.expr()).pop();
        } else {
            //ProgramState s = inputState(c, tc);
            assert !inputState.getMap().containsKey(t);
            return inputState.set(t, joiner.defaultInitialization());
        }
    }

    @Override
    public ProgramState<T> transferAssignment(Cursor c, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {

        J.Assignment a = c.getValue();
        if (a.getVariable() instanceof J.Identifier) {
            J.Identifier ident = (J.Identifier) a.getVariable();
            //ProgramState<T> s = analysis(a.getAssignment());
            return inputState.set(ident.getFieldType(), inputState.expr());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public ProgramState<T> transferMethodInvocation(Cursor c, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        J.MethodInvocation m = c.getValue();
        return inputState.push(joiner.lowerBound());
    }

    @Override
    public ProgramState<T> transferArrayAccess(Cursor c, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        J.ArrayAccess ac = c.getValue();
        return inputState.push(joiner.lowerBound());
    }

    @Override
    public  ProgramState<T> transferNewClass(Cursor c, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return inputState.push(joiner.lowerBound());
    }

    @Override
    public ProgramState<T> transferLiteral(Cursor c, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return inputState.push(joiner.lowerBound());
    }

    @Override
    public ProgramState<T> transferIdentifier(Cursor c, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        J.Identifier i = c.getValue();
        ProgramState<T> s = inputState;
        T v = s.get(i.getFieldType());
        return s.push(v);
    }

    @Override
    public ProgramState<T> transferIfElse(Cursor c, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
//        J.If.Else ifElse = c.getValue();
//        ProgramPoint body = ifElse.getBody();
//        return analysis(body);
        return inputState;
    }

    @Override
    public ProgramState<T> transferBlock(Cursor c, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
//        J.Block block = c.getValue();
//        List<Statement> stmts = block.getStatements();
//        if (stmts.size() > 0) {
//            Statement stmt = stmts.get(stmts.size() - 1);
//            return analysis(stmt);
//        } else {
//            throw new UnsupportedOperationException(); // TODO
//        }
        return inputState;
    }

    @Override
    public ProgramState<T> transferParentheses(Cursor c, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
//        J.Parentheses paren = c.getValue();
//        ProgramPoint tree = (ProgramPoint) paren.getTree();
//        return analysis(tree);
        return inputState;
    }

    @Override
    public ProgramState<T> transferControlParentheses(Cursor c, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
//        J.ControlParentheses paren = c.getValue();
//        ProgramPoint tree = (ProgramPoint) paren.getTree();
//        return analysis(tree);
        return inputState;
    }
}

