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
public class IsNullAnalysis extends DataFlowAnalysis<ProgramState<ModalBoolean>> {

    public IsNullAnalysis(DataFlowGraph dfg) {
        super(dfg);
    }

    private static final Joiner<ModalBoolean> JOINER = ModalBoolean.JOINER;

    /**
     * @return Whether the variable v is known to be null before given program point.
     */
    public ModalBoolean isNullBefore(Cursor programPoint, JavaType.Variable v)
    {
        ProgramState<ModalBoolean> state = inputState(programPoint, new TraversalControl<>());
        ModalBoolean result = state.get(v);
        return result;
    }

    @Override
    public ProgramState join(Collection<ProgramState<ModalBoolean>> outs) {
        return ProgramState.join(JOINER, outs);
    }

    @Override
    public ProgramState transferToIfThenElseBranches(J.If ifThenElse, ProgramState s, String ifThenElseBranch) {
        Expression cond = ifThenElse.getIfCondition().getTree();
        if(cond instanceof J.Binary) {
            J.Binary binary = (J.Binary)cond;
            if(binary.getOperator() == J.Binary.Type.Equal) {
                if(binary.getLeft() instanceof J.Identifier) {
                    J.Identifier left = (J.Identifier) binary.getLeft();
                    if (binary.getRight() instanceof J.Literal) {
                        // condition has the form 's == literal'
                        boolean isNull = ((J.Literal) binary.getRight()).getValue() == null;
                        if(ifThenElseBranch.equals("then")) {
                            // in the 'then' branch
                            s = s.set(left.getFieldType(), isNull ? True : False);
                        } else {
                            // in the 'else' branch or the 'exit' branch
                            s = s.set(left.getFieldType(), isNull ? False : True);
                        }
                    }
                }
            }
        }
        return s;
    }

    @Override
    public ProgramState<ModalBoolean> defaultTransfer(Cursor c, TraversalControl<ProgramState<ModalBoolean>> t) {
        return inputState(c, t);
    }

    @Override
    public ProgramState<ModalBoolean> transferBinary(Cursor c, TraversalControl<ProgramState<ModalBoolean>> t) {
        return inputState(c, t).push(False);
    }

    @Override
    public ProgramState<ModalBoolean> transferNamedVariable(Cursor c, TraversalControl<ProgramState<ModalBoolean>> tc) {
        J.VariableDeclarations.NamedVariable v = c.getValue();
        JavaType.Variable t = v.getVariableType();
        if(v.getInitializer() != null) {
            ProgramState<ModalBoolean> s = outputState(new Cursor(c, v.getInitializer()), tc);
            ModalBoolean e = s.expr();
            return s.set(t, s.expr()).pop();
        } else {
            ProgramState s = inputState(c, tc);
            assert !s.getMap().containsKey(t);
            return s.set(t, True);
        }
    }

    @Override
    public ProgramState<ModalBoolean> transferAssignment(Cursor c, TraversalControl<ProgramState<ModalBoolean>> t) {

        J.Assignment a = c.getValue();
        if (a.getVariable() instanceof J.Identifier) {
            J.Identifier ident = (J.Identifier) a.getVariable();
            ProgramState<ModalBoolean> s = outputState(new Cursor(c, a.getAssignment()), t);
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
    public ProgramState<ModalBoolean> transferMethodInvocation(Cursor c, TraversalControl<ProgramState<ModalBoolean>> t) {
        J.MethodInvocation method = c.getValue();
        for(MethodMatcher matcher : definitelyNonNullReturningMethodMatchers) {
            if (matcher.matches(method)) {
                return inputState(c, t).push(False);
            }
        }
        return inputState(c, t).push(Conflict);
    }

    @Override
    public ProgramState<ModalBoolean> transferLiteral(Cursor c, TraversalControl<ProgramState<ModalBoolean>> t) {
        J.Literal pp = c.getValue();
        ProgramState s = inputState(c, t);
        if (pp.getValue() == null) {
            return s.push(True);
        } else {
            return s.push(False);
        }
    }

    @Override
    public ProgramState<ModalBoolean> transferIdentifier(Cursor c, TraversalControl<ProgramState<ModalBoolean>> t) {
        J.Identifier i = c.getValue();
        ProgramState<ModalBoolean> s = inputState(c, t);
        ModalBoolean v = s.get(i.getFieldType());
        return inputState(c, t).push(v);
    }

    @Override
    public ProgramState<ModalBoolean> transferIfElse(Cursor c, TraversalControl<ProgramState<ModalBoolean>> t) {
        J.If.Else ifElse = c.getValue();
        return outputState(new Cursor(c, ifElse.getBody()), t);
    }

    @Override
    public ProgramState<ModalBoolean> transferBlock(Cursor c, TraversalControl<ProgramState<ModalBoolean>> t) {
        J.Block block = c.getValue();
        List<Statement> stmts = block.getStatements();
        if (stmts.size() > 0) {
            return outputState(new Cursor(c, stmts.get(stmts.size() - 1)), t);
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
    }

    @Override
    public ProgramState<ModalBoolean> transferParentheses(Cursor c, TraversalControl<ProgramState<ModalBoolean>> t) {
        J.Parentheses paren = c.getValue();
        return outputState(new Cursor(c, paren.getTree()), t);
    }
}

