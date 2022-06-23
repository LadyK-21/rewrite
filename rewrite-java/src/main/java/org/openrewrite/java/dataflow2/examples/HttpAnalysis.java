package org.openrewrite.java.dataflow2.examples;

import org.openrewrite.Cursor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.dataflow2.*;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class HttpAnalysis extends ValueAnalysis<HttpAnalysisValue> {
    public static MethodMatcher URI_CREATE_MATCHER = new MethodMatcher("java.net.URI create(String)");
    public static MethodMatcher STRING_REPLACE = new MethodMatcher("java.lang.String replace(..)");
    public HttpAnalysis(DataFlowGraph dfg) {
        super(dfg, HttpAnalysisValue.JOINER);
    }

    @Override
    public ProgramState<HttpAnalysisValue> transferMethodInvocation(Cursor c, ProgramState<HttpAnalysisValue> inputState, TraversalControl<ProgramState<HttpAnalysisValue>> t) {
        J.MethodInvocation mi = c.getValue();
        if (URI_CREATE_MATCHER.matches(mi)) {
            return inputState.push(HttpAnalysisValue.UNKNOWN);
        } else if (STRING_REPLACE.matches(mi)) {
            //ProgramPoint arg0 = mi.getArguments().get(0);
            //ProgramPoint arg1 = mi.getArguments().get(1);
            HttpAnalysisValue arg0Value = inputState.expr(1); // analysis(arg0);
            HttpAnalysisValue arg1Value = inputState.expr(0); // analysis(arg1);
            if (arg0Value.getName() == HttpAnalysisValue.Understanding.NOT_SECURE
                && arg1Value.getName() == HttpAnalysisValue.Understanding.SECURE) {
                return inputState.push(HttpAnalysisValue.SECURE);
            } else if (arg1Value.getName() == HttpAnalysisValue.Understanding.NOT_SECURE
                    && arg0Value.getName() == HttpAnalysisValue.Understanding.SECURE)
            return inputState.push(new HttpAnalysisValue(HttpAnalysisValue.Understanding.NOT_SECURE, mi.getArguments().get(1)));
        }
        return super.transferMethodInvocation(c, inputState, t);
    }

    @Override
    public ProgramState<HttpAnalysisValue> transferLiteral(Cursor c, ProgramState<HttpAnalysisValue> inputState, TraversalControl<ProgramState<HttpAnalysisValue>> t) {
        J.Literal literal = c.getValue();
        String value = literal.getValueSource();
        if(value != null) {
            if (value.startsWith("https")) {
                return inputState.push(HttpAnalysisValue.SECURE);
            } else if (value.startsWith("http")) {
                return inputState.push(new HttpAnalysisValue(HttpAnalysisValue.Understanding.NOT_SECURE, literal));
            }
        }
        return super.transferLiteral(c, inputState, t);
    }

    @Override
    public ProgramState<HttpAnalysisValue> transferToIfThenElseBranches(J.If ifThenElse, ProgramState<HttpAnalysisValue> state, String ifThenElseBranch) {
        return null;
    }
}
