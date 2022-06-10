package org.openrewrite.java.dataflow3;

import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.dataflow2.DataFlowGraph;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

import static org.openrewrite.java.dataflow3.Ternary.No;

public class IsNullAnalysis extends DataFlowAnalysisVisitor<Ternary> {
    private static final MethodMatcher TO_UPPER_CASE = new MethodMatcher("java.lang.String toUpperCase()");

    public IsNullAnalysis(DataFlowGraph dfg) {
        super(dfg);
    }

    @Override
    public J visitExpression(Expression expression, DataFlowExecutionContextView<Ternary> ctx) {
        if (TO_UPPER_CASE.matches(expression)) {
            ctx.reduceProgramState(No);
        }
        return super.visitExpression(expression, ctx);
    }

    @Override
    public J visitLiteral(J.Literal literal, DataFlowExecutionContextView<Ternary> ctx) {
        if (literal.getValue() == null) {
            ctx.reduceProgramState(No);
        }
        return super.visitLiteral(literal, ctx);
    }
}
