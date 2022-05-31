package org.openrewrite.java.dataflow3;

import org.openrewrite.DelegatingExecutionContext;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.tree.JavaType;

import static java.util.Objects.requireNonNull;

public class DataFlowExecutionContextView<P extends ProgramState<P>> extends DelegatingExecutionContext {
    private static final String STORE_OF_INTEREST = "org.openrewrite.java.dataflow.storeOfInterest";
    private static final String DEFAULT_PROGRAM_STATE = "org.openrewrite.java.dataflow.defaultProgramState";
    private static final String PROGRAM_STATE = "org.openrewrite.java.dataflow.programState";

    public DataFlowExecutionContextView(ExecutionContext delegate, JavaType.Variable storeOfInterest,
                                        P defaultProgramState) {
        super(delegate);
        putMessage(STORE_OF_INTEREST, storeOfInterest);
        putMessage(DEFAULT_PROGRAM_STATE, defaultProgramState);
    }

    public static <P extends ProgramState<P>> DataFlowExecutionContextView<P> view(ExecutionContext ctx, JavaType.Variable storeOfInterest,
                                                           P defaultProgramState) {
        if (ctx instanceof DataFlowExecutionContextView) {
            //noinspection unchecked
            return (DataFlowExecutionContextView<P>) ctx;
        }
        return new DataFlowExecutionContextView<>(ctx, storeOfInterest, defaultProgramState);
    }

    public JavaType.Variable getStoreOfInterest() {
        return requireNonNull(getMessage(STORE_OF_INTEREST));
    }

    public P reduceProgramState(P p) {
        P r = p.reduce(getMessage(PROGRAM_STATE), p);
        putMessage(PROGRAM_STATE, r);
        return r;
    }

    public P joined() {
        return getMessage(PROGRAM_STATE, requireNonNull(getMessage(DEFAULT_PROGRAM_STATE)));
    }

    public DataFlowExecutionContextView<P> fork() {
        return new DataFlowExecutionContextView<>(new InMemoryExecutionContext(),
                getStoreOfInterest(), getMessage(DEFAULT_PROGRAM_STATE));
    }
}
