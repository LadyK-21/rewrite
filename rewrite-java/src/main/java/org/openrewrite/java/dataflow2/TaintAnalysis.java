package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.Incubating;

import java.util.Collection;

import static org.openrewrite.java.dataflow2.ModalBoolean.False;

@Incubating(since = "7.24.0")
public abstract class TaintAnalysis extends DataFlowAnalysis<ProgramState<ModalBoolean>> {

    private static final Joiner<ModalBoolean> JOINER = ModalBoolean.JOINER;

    public TaintAnalysis(DataFlowGraph dfg) {
        super(dfg);
    }

    @Override
    public ProgramState<ModalBoolean> join(Collection<ProgramState<ModalBoolean>> outs) {
        return ProgramState.join(JOINER, outs);
    }

    @Override
    public ProgramState<ModalBoolean> defaultTransfer(Cursor c, TraversalControl<ProgramState<ModalBoolean>> t) {
        return inputState(c, t);
    }

    @Override
    public ProgramState<ModalBoolean> transferBinary(Cursor programPoint, TraversalControl<ProgramState<ModalBoolean>> t) {
        return inputState(programPoint, t).push(False);
    }
}
