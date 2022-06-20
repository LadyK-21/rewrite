package org.openrewrite.java.dataflow2.examples;

import org.openrewrite.Cursor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.dataflow2.DataFlowGraph;
import org.openrewrite.java.dataflow2.ProgramState;
import org.openrewrite.java.dataflow2.TraversalControl;
import org.openrewrite.java.dataflow2.ValueAnalysis;
import org.openrewrite.java.tree.J;

public class HttpAnalysis extends ValueAnalysis<HttpAnalysisValue> {
    public static MethodMatcher URI_CREATE_MATCHER = new MethodMatcher("java.net.URI create(String)");
    public static MethodMatcher STRING_REPLACE = new MethodMatcher("java.lang.String replace(..)");
    public HttpAnalysis(DataFlowGraph dfg) {
        super(dfg, HttpAnalysisValue.JOINER);
    }

    @Override
    public ProgramState<HttpAnalysisValue> transferMethodInvocation(Cursor c, TraversalControl<ProgramState<HttpAnalysisValue>> t) {
        J.MethodInvocation mi = c.getValue();
        if (URI_CREATE_MATCHER.matches(mi)) {
            return inputState(c, t).push(HttpAnalysisValue.UNKNOWN);
        } else if (STRING_REPLACE.matches(mi)) {
            ProgramState<HttpAnalysisValue> arg0State = outputState(new Cursor(c, mi.getArguments().get(0)), null);
            ProgramState<HttpAnalysisValue> arg1State = outputState(new Cursor(c, mi.getArguments().get(1)), null);
            if (arg0State.expr().getName() == HttpAnalysisValue.Understanding.NOT_SECURE
                && arg1State.expr().getName() == HttpAnalysisValue.Understanding.SECURE) {
                return inputState(c, t).push(HttpAnalysisValue.SECURE);
            } else if (arg1State.expr().getName() == HttpAnalysisValue.Understanding.NOT_SECURE
                    && arg0State.expr().getName() == HttpAnalysisValue.Understanding.SECURE)
            return inputState(c, t).push(new HttpAnalysisValue(HttpAnalysisValue.Understanding.NOT_SECURE, mi.getArguments().get(1)));
        }
        return super.transferMethodInvocation(c, t);
    }

    @Override
    public ProgramState<HttpAnalysisValue> transferLiteral(Cursor c, TraversalControl<ProgramState<HttpAnalysisValue>> t) {
        J.Literal literal = c.getValue();
        String value = literal.getValueSource();
        if(value != null) {
            if (value.startsWith("https")) {
                return inputState(c, t).push(HttpAnalysisValue.SECURE);
            } else if (value.startsWith("http")) {
                return inputState(c, t).push(new HttpAnalysisValue(HttpAnalysisValue.Understanding.NOT_SECURE, literal));
            }
        }
        return super.transferLiteral(c, t);
    }

    @Override
    public ProgramState<HttpAnalysisValue> transferToIfThenElseBranches(J.If ifThenElse, ProgramState<HttpAnalysisValue> state, String ifThenElseBranch) {
        return null;
    }
}
