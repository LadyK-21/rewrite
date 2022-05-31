package org.openrewrite.java.dataflow3;

public interface ProgramState<P extends ProgramState<P>> {
    boolean isTerminal();

    P reduce(P p, P acc);
}
