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

    public ProgramState defaultTransfer(Cursor c, ProgramState state) {
        return inputState(c, state);
    }

    @Override
    public ProgramState transferBinary(Cursor programPoint, ProgramState state) {
        return state.push(DefinitelyNo);
    }

}
