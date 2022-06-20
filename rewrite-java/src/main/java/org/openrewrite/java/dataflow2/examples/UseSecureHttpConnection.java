package org.openrewrite.java.dataflow2.examples;

import lombok.AllArgsConstructor;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.dataflow2.DataFlowGraph;
import org.openrewrite.java.dataflow2.ProgramState;
import org.openrewrite.java.tree.J;

import static org.openrewrite.java.dataflow2.examples.HttpAnalysis.URI_CREATE_MATCHER;

public class UseSecureHttpConnection extends Recipe {
    @Override
    public String getDisplayName() {
        return "Use Secure Http Connection";
    }

    @Override
    protected JavaIsoVisitor<ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                J.MethodInvocation mi = super.visitMethodInvocation(method, ctx);
                if (URI_CREATE_MATCHER.matches(mi)) {
                    J.CompilationUnit cu = getCursor().firstEnclosing(J.CompilationUnit.class);
                    HttpAnalysis httpAnalysis = new HttpAnalysis(new DataFlowGraph(cu));
                    ProgramState<HttpAnalysisValue> state = httpAnalysis
                            .outputState(new Cursor(getCursor(), mi.getArguments().get(0)), null);
                    HttpAnalysisValue stateValue = state.expr();
                    if (stateValue.name == HttpAnalysisValue.Understanding.NOT_SECURE) {
                        doAfterVisit(new HttpToHttpsVisitor((J.Literal) stateValue.literal));
                    }
                }
                return mi;
            }
        };
    }

    @AllArgsConstructor
    private static class HttpToHttpsVisitor extends JavaIsoVisitor<ExecutionContext> {
        private final J.Literal insecureHttps;

        @Override
        public J.Literal visitLiteral(J.Literal literal, ExecutionContext executionContext) {

            if (literal == insecureHttps) {
                return literal.withValueSource(literal.getValueSource().replace("http", "https"));
            }
            return literal;
        }
    }

}
