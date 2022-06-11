package org.openrewrite.java.dataflow2.examples;

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

import static org.openrewrite.java.dataflow2.ModalBoolean.*;

@Incubating(since = "7.24.0")
public class IsNullAnalysis extends DataFlowAnalysis<ProgramState> {

    public IsNullAnalysis(DataFlowGraph dfg) {
        super(dfg);
    }

    /**
     * @return Whether the variable v is known to be null before given program point.
     */
    public ModalBoolean isNullBefore(Cursor programPoint, JavaType.Variable v)
    {
        ProgramState state = inputState(programPoint, new TraversalControl<>());
        ModalBoolean result = state.get(v);
        return result;
    }

    @Override
    public ProgramState join(Collection<ProgramState> outs) {
        return ProgramState.join(outs);
    }

    @Override
    public ProgramState transferToIfThenElseBranches(J.If ifThenElse, ProgramState s, String ifThenElseBranch) {
        Expression cond = ifThenElse.getIfCondition().getTree();
        if(cond instanceof J.Binary) {
            J.Binary binary = (J.Binary)cond;
            if(binary.getOperator() == J.Binary.Type.Equal) {
                if(binary.getLeft() instanceof J.Identifier) {
                    J.Identifier left = (J.Identifier) binary.getLeft();
                    if (binary.getRight() instanceof J.Literal && ((J.Literal) binary.getRight()).getValue() == null) {
                        // condition has the form 's == null'
                        if(ifThenElseBranch.equals("then")) {
                            // in the 'then' branch, s is null
                            s = s.set(left.getFieldType(), ModalBoolean.True);
                        } else {
                            // in the 'else' branch or the 'exit' branch, s is not null
                            s = s.set(left.getFieldType(), ModalBoolean.False);
                        }
                    }
                }
            }
        }
        return s;
    }

    @Override
    public ProgramState defaultTransfer(Cursor c, TraversalControl<ProgramState> t) {
        return inputState(c, t);
    }

    @Override
    public ProgramState transferBinary(Cursor c, TraversalControl<ProgramState> t) {
        return inputState(c, t).push(False);
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
            return s.set(t, True);
        }
    }

    @Override
    public ProgramState transferAssignment(Cursor c, TraversalControl<ProgramState> t) {

        J.Assignment a = c.getValue();
        if (a.getVariable() instanceof J.Identifier) {
            J.Identifier ident = (J.Identifier) a.getVariable();
            ProgramState s = outputState(new Cursor(c, a.getAssignment()), t);
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
    public ProgramState transferMethodInvocation(Cursor c, TraversalControl<ProgramState> t) {
        J.MethodInvocation method = c.getValue();
        for(MethodMatcher matcher : definitelyNonNullReturningMethodMatchers) {
            if (matcher.matches(method)) {
                return inputState(c, t).push(False);
            }
        }
        return inputState(c, t).push(Conflict);
    }

    @Override
    public ProgramState transferLiteral(Cursor c, TraversalControl<ProgramState> t) {
        J.Literal pp = c.getValue();
        ProgramState s = inputState(c, t);
        if (pp.getValue() == null) {
            return s.push(True);
        } else {
            return s.push(False);
        }
    }

    @Override
    public ProgramState transferIdentifier(Cursor c, TraversalControl<ProgramState> t) {
        J.Identifier i = c.getValue();
        ProgramState s = inputState(c, t);
        ModalBoolean v = s.get(i.getFieldType());
        return inputState(c, t).push(v);
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
    public ProgramState transferParentheses(Cursor c, TraversalControl<ProgramState> t) {
        J.Parentheses paren = c.getValue();
        return outputState(new Cursor(c, paren.getTree()), t);
    }
}

