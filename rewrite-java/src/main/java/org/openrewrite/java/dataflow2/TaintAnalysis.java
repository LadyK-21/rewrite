package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.java.tree.JavaType;

import java.util.Collection;

import static org.openrewrite.java.dataflow2.Ternary.DefinitelyNo;

public abstract class TaintAnalysis extends DataFlowAnalysis<ProgramState> {

    @Override
    public ProgramState join(Collection<ProgramState> outs) {
        return ProgramState.join(outs);
    }

    @Override
    public ProgramState defaultTransfer(Cursor c, TraversalControl<ProgramState> t) {
        return inputState(c, t);
    }

    @Override
    public ProgramState transferBinary(Cursor programPoint, TraversalControl<ProgramState> t) {
        return inputState(programPoint, t).push(DefinitelyNo);
    }
}
