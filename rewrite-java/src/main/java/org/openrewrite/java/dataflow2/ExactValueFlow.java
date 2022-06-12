//package org.openrewrite.java.dataflow2;
//
//import org.openrewrite.Cursor;
//import org.openrewrite.Incubating;
//
//import java.util.Collection;
//
//import static org.openrewrite.java.dataflow2.ModalBoolean.False;
//
//@Incubating(since = "7.24.0")
//public abstract class ExactValueFlow extends DataFlowAnalysis<ProgramState<T>> {
//
//    // Provide transfer methods for all
//
//    private static final Joiner<ModalBoolean> JOINER = ModalBoolean.JOINER;
//
//    public ExactValueFlow(DataFlowGraph dfg) {
//        super(dfg);
//    }
//
//    @Override
//    public ProgramState<T> transferBinary(Cursor programPoint, TraversalControl<ProgramState<T>> t) {
//        return inputState(programPoint, t).push(False);
//    }
//}
