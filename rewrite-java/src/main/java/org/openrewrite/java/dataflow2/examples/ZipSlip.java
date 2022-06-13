package org.openrewrite.java.dataflow2.examples;

import lombok.AllArgsConstructor;
import org.openrewrite.Cursor;
import org.openrewrite.Incubating;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.dataflow2.*;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Incubating(since = "7.24.0")
public class ZipSlip extends DataFlowAnalysis<ProgramState<ZipSlipValue>> {

    public ZipSlip(DataFlowGraph dfg) {
        super(dfg);
    }

    @Override
    public ProgramState<ZipSlipValue> join(Collection<ProgramState<ZipSlipValue>> outs) {
        return ProgramState.join(ZipSlipValue.JOINER, outs);
    }

    @Override
    public ProgramState<ZipSlipValue> defaultTransfer(Cursor c, TraversalControl<ProgramState<ZipSlipValue>> t) {
        throw new UnsupportedOperationException();
        //return inputState(c, t);
    }

    @Override
    public ProgramState<ZipSlipValue> transferToIfThenElseBranches(J.If ifThenElse, ProgramState<ZipSlipValue> s, String ifThenElseBranch) {
        Expression cond = ifThenElse.getIfCondition().getTree();
        // look for file.toPath().startsWith(dir.toPath()) or its negation

        boolean negate = false;
        if(cond instanceof J.Unary) {
            J.Unary unary = (J.Unary)cond;
            if(unary.getOperator() == J.Unary.Type.Not) {
                cond = unary.getExpression();
                negate = true;
            }
        }
        if(cond instanceof J.MethodInvocation) {
            J.MethodInvocation startsWithInvocation = (J.MethodInvocation)cond;
            // ...
            // s = s.set(left.getFieldType(), ZipSlipValue.True / False);
        }

        return s;
    }

    @Override
    public ProgramState<ZipSlipValue> transferUnary(Cursor c, TraversalControl<ProgramState<ZipSlipValue>> t) {
        return inputState(c, t).push(ZipSlipValue.LOWER);
    }

    @Override
    public ProgramState<ZipSlipValue> transferBinary(Cursor c, TraversalControl<ProgramState<ZipSlipValue>> t) {
        return inputState(c, t).push(ZipSlipValue.LOWER);
    }

    @Override
    public ProgramState<ZipSlipValue> transferEmpty(Cursor c, TraversalControl<ProgramState<ZipSlipValue>> t) {
        return inputState(c, t);
    }

    @Override
    public ProgramState<ZipSlipValue> transferNamedVariable(Cursor c, TraversalControl<ProgramState<ZipSlipValue>> tc) {
        J.VariableDeclarations.NamedVariable v = c.getValue();
        JavaType.Variable t = v.getVariableType();
        if(v.getInitializer() != null) {
            ProgramState s = outputState(new Cursor(c, v.getInitializer()), tc);
            return s.set(t, s.expr()).pop();
        } else {
            ProgramState s = inputState(c, tc);
            assert !s.getMap().containsKey(t);
            return s.set(t, ZipSlipValue.JOINER.defaultInitialization());
        }
    }

    @Override
    public ProgramState<ZipSlipValue> transferAssignment(Cursor c, TraversalControl<ProgramState<ZipSlipValue>> t) {

        J.Assignment a = c.getValue();
        if (a.getVariable() instanceof J.Identifier) {
            J.Identifier ident = (J.Identifier) a.getVariable();
            ProgramState<ZipSlipValue> s = outputState(new Cursor(c, a.getAssignment()), t);
            return s.set(ident.getFieldType(), s.expr());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private static final String[] definitelyNonNullReturningMethodSignatures = new String[] {
            "java.lang.String toUpperCase()"
    };

    private static final List<MethodMatcher> definitelyNonNullReturningMethodMatchers =
            Arrays.stream(definitelyNonNullReturningMethodSignatures).map(MethodMatcher::new).collect(Collectors.toList());

    @Override
    public ProgramState<ZipSlipValue> transferMethodInvocation(Cursor c, TraversalControl<ProgramState<ZipSlipValue>> t) {
        J.MethodInvocation m = c.getValue();
        return inputState(c, t).push(ZipSlipValue.JOINER.lowerBound());
    }

    @Override
    public ProgramState<ZipSlipValue> transferNewClass(Cursor c, TraversalControl<ProgramState<ZipSlipValue>> t) {
        J.NewClass newClass = c.getValue();
        // new File(dir, name)
        MethodMatcher m = new MethodMatcher("java.io.File <constructor>(java.io.File, java.lang.String)");
        if(m.matches(newClass)) {
            Expression dir = newClass.getArguments().get(0);
            return inputState(c, t).push(new ZipSlipValue(null, dir));
        } else {
            return inputState(c, t).push(ZipSlipValue.JOINER.lowerBound());
        }
    }

    @Override
    public ProgramState<ZipSlipValue> transferLiteral(Cursor c, TraversalControl<ProgramState<ZipSlipValue>> t) {
        return inputState(c, t).push(ZipSlipValue.JOINER.lowerBound());
    }

    @Override
    public ProgramState<ZipSlipValue> transferIdentifier(Cursor c, TraversalControl<ProgramState<ZipSlipValue>> t) {
        J.Identifier i = c.getValue();
        ProgramState<ZipSlipValue> s = inputState(c, t);
        ZipSlipValue v = s.get(i.getFieldType());
        return s.push(v);
    }

    @Override
    public ProgramState<ZipSlipValue> transferIfElse(Cursor c, TraversalControl<ProgramState<ZipSlipValue>> t) {
        J.If.Else ifElse = c.getValue();
        return outputState(new Cursor(c, ifElse.getBody()), t);
    }

    @Override
    public ProgramState<ZipSlipValue> transferBlock(Cursor c, TraversalControl<ProgramState<ZipSlipValue>> t) {
        J.Block block = c.getValue();
        List<Statement> stmts = block.getStatements();
        if (stmts.size() > 0) {
            return outputState(new Cursor(c, stmts.get(stmts.size() - 1)), t);
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
    }

    @Override
    public ProgramState<ZipSlipValue> transferParentheses(Cursor c, TraversalControl<ProgramState<ZipSlipValue>> t) {
        J.Parentheses paren = c.getValue();
        return outputState(new Cursor(c, paren.getTree()), t);
    }

    @Override
    public ProgramState<ZipSlipValue> transferControlParentheses(Cursor c, TraversalControl<ProgramState<ZipSlipValue>> t) {
        J.ControlParentheses paren = c.getValue();
        return outputState(new Cursor(c, paren.getTree()), t);
    }
}

