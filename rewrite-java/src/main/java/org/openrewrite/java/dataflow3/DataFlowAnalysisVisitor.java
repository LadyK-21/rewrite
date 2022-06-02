package org.openrewrite.java.dataflow3;

import org.openrewrite.Cursor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.dataflow2.DataFlowGraph;
import org.openrewrite.java.tree.J;

import java.util.Collection;

public class DataFlowAnalysisVisitor<P extends ProgramState<P>> extends JavaVisitor<DataFlowExecutionContextView<P>> {

    /**
     * @return The *input* program state at given program point.
     */
    public P inputState(Cursor pp, DataFlowExecutionContextView<P> ctx) {
        Collection<Cursor> sources = DataFlowGraph.previous(pp);
        for (Cursor source : sources) {
            DataFlowExecutionContextView<P> sourceCtx = ctx.fork();
            visit(source.getValue(), sourceCtx, source.getParentOrThrow());
            P r = ctx.reduceProgramState(sourceCtx.joined());
            if (r.isTerminal()) {
                return r;
            }
        }
        return ctx.joined();
    }

    @Override
    public J visitIdentifier(J.Identifier ident, DataFlowExecutionContextView<P> ctx) {
        inputState(getCursor(), ctx);
        return super.visitIdentifier(ident, ctx);
    }

    @Override
    public J visitEmpty(J.Empty empty, DataFlowExecutionContextView<P> ctx) {
        inputState(getCursor(), ctx);
        return super.visitEmpty(empty, ctx);
    }

    @Override
    public J visitIf(J.If iff, DataFlowExecutionContextView<P> ctx) {
        inputState(getCursor(), ctx);
        return super.visitIf(iff, ctx);
    }

    @Override
    public J visitElse(J.If.Else elze, DataFlowExecutionContextView<P> ctx) {
        inputState(getCursor(), ctx);
        return super.visitElse(elze, ctx);
    }

    @Override
    public J visitVariable(J.VariableDeclarations.NamedVariable variable, DataFlowExecutionContextView<P> ctx) {
        inputState(getCursor(), ctx);
        return super.visitVariable(variable, ctx);
    }

    @Override
    public J visitBinary(J.Binary binary, DataFlowExecutionContextView<P> ctx) {
        inputState(getCursor(), ctx);
        return super.visitBinary(binary, ctx);
    }
}
