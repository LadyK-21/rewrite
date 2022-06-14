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
    
    protected final Joiner<T> joiner;

    public ValueAnalysis(DataFlowGraph dfg, Joiner<T> joiner) {
        super(dfg);
        this.joiner = joiner;
    }

    @Override
    public ProgramState<T> join(Collection<ProgramState<T>> outs) {
        return ProgramState.join(joiner, outs);
    }

    @Override
    public ProgramState<T> defaultTransfer(Cursor c, TraversalControl<ProgramState<T>> t) {
        throw new UnsupportedOperationException();
        //return inputState(c, t);
    }

    @Override
    public abstract ProgramState<T> transferToIfThenElseBranches(J.If ifThenElse, ProgramState<T> state, String ifThenElseBranch);

    @Override
    public ProgramState<T> transferUnary(Cursor c, TraversalControl<ProgramState<T>> t) {
        return inputState(c, t).push(joiner.lowerBound());
    }

    @Override
    public ProgramState<T> transferBinary(Cursor c, TraversalControl<ProgramState<T>> t) {
        return inputState(c, t).push(joiner.lowerBound());
    }

    @Override
    public ProgramState<T> transferEmpty(Cursor c, TraversalControl<ProgramState<T>> t) {
        return inputState(c, t);
    }

    @Override
    public ProgramState<T> transferNamedVariable(Cursor c, TraversalControl<ProgramState<T>> tc) {
        J.VariableDeclarations.NamedVariable v = c.getValue();
        JavaType.Variable t = v.getVariableType();
        if (v.getInitializer() != null) {
            ProgramState s = outputState(new Cursor(c, v.getInitializer()), tc);
            return s.set(t, s.expr()).pop();
        } else {
            ProgramState s = inputState(c, tc);
            assert !s.getMap().containsKey(t);
            return s.set(t, joiner.defaultInitialization());
        }
    }

    @Override
    public ProgramState<T> transferAssignment(Cursor c, TraversalControl<ProgramState<T>> t) {

        J.Assignment a = c.getValue();
        if (a.getVariable() instanceof J.Identifier) {
            J.Identifier ident = (J.Identifier) a.getVariable();
            ProgramState<T> s = outputState(new Cursor(c, a.getAssignment()), t);
            return s.set(ident.getFieldType(), s.expr());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public ProgramState<T> transferMethodInvocation(Cursor c, TraversalControl<ProgramState<T>> t) {
        J.MethodInvocation m = c.getValue();
        return inputState(c, t).push(joiner.lowerBound());
    }

    @Override
    public abstract ProgramState<T> transferNewClass(Cursor c, TraversalControl<ProgramState<T>> t);

    @Override
    public ProgramState<T> transferLiteral(Cursor c, TraversalControl<ProgramState<T>> t) {
        return inputState(c, t).push(joiner.lowerBound());
    }

    @Override
    public ProgramState<T> transferIdentifier(Cursor c, TraversalControl<ProgramState<T>> t) {
        J.Identifier i = c.getValue();
        ProgramState<T> s = inputState(c, t);
        T v = s.get(i.getFieldType());
        return s.push(v);
    }

    @Override
    public ProgramState<T> transferIfElse(Cursor c, TraversalControl<ProgramState<T>> t) {
        J.If.Else ifElse = c.getValue();
        return outputState(new Cursor(c, ifElse.getBody()), t);
    }

    @Override
    public ProgramState<T> transferBlock(Cursor c, TraversalControl<ProgramState<T>> t) {
        J.Block block = c.getValue();
        List<Statement> stmts = block.getStatements();
        if (stmts.size() > 0) {
            return outputState(new Cursor(c, stmts.get(stmts.size() - 1)), t);
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
    }

    @Override
    public ProgramState<T> transferParentheses(Cursor c, TraversalControl<ProgramState<T>> t) {
        J.Parentheses paren = c.getValue();
        return outputState(new Cursor(c, paren.getTree()), t);
    }

    @Override
    public ProgramState<T> transferControlParentheses(Cursor c, TraversalControl<ProgramState<T>> t) {
        J.ControlParentheses paren = c.getValue();
        return outputState(new Cursor(c, paren.getTree()), t);
    }
}

