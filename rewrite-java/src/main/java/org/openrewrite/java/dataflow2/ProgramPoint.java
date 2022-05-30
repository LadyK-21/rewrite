package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.java.tree.J;

import java.util.Collection;
import java.util.Collections;

/**
 * A program point is anything that may change the state of a program.
 * It represents a node in the data-flow graph, embedded in the AST (i.e. no additional data structures are created).
 * There is an edge (p,q) in the dataflow graph iff. p \in sources(q) iff. q \in sinks(p).
 * A program point has an input state and an output state, they are related by the transfer function.
 * In Java, program points are statements, variable declarations, assignment expressions, increment and decrement
 * expressions, method declarations.
 */
public interface ProgramPoint {

    default Collection<Cursor> previous(Cursor c) {
        return DataFlowGraph.primitiveSources(c);
    }
    default Collection<Cursor> next(Cursor c) {
        return DataFlowGraph.next(c);
    } // TODO

    // ProgramState in = join(previous)
    // in particular, if previous is empty, join defines the default state

    // ProgramState out = transfer(in)

    default String printPP(Cursor cursor) {
        if(this instanceof J) {
            return ((J)this).print(cursor);
        } else {
            throw new UnsupportedOperationException("printPP(" + this.getClass().getSimpleName() + ")");
        }
    }

    static ProgramPoint ENTRY = new ProgramPoint() { @Override public String toString() { return "ENTRY"; }};
    static ProgramPoint EXIT = new ProgramPoint() { @Override public String toString() { return "EXIT"; }};
}
