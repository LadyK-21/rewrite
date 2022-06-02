package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.java.tree.J;

import java.util.Collection;

/**
 * A program point is anything that may change the state of a program.
 * It represents a node in the data-flow graph, embedded in the AST. Namely, program points are represented
 * by cursors and no additional data structures are needed. In the case of compound program points, the constants
 * ENTRY and EXIT are used to refer to the entry and exit program points within the compound.
 *
 * There is an edge (p,q) in the dataflow graph iff. p \in sources(q) iff. q \in sinks(p).
 * A program point has an input state and an output state, they are related by the transfer function.
 *
 * In Java, program points are statements, variable declarations, assignment expressions, increment and decrement
 * expressions, method declarations.
 */
public interface ProgramPoint {

    default Collection<Cursor> previous(Cursor c) {
        return DataFlowGraph.previous(c);
    }
    default Collection<Cursor> next(Cursor c) {
        return DataFlowGraph.next(c);
    } // TODO

    default String printPP(Cursor cursor) {
        if(this instanceof J) {
            return ((J)this).print(cursor);
        } else {
            throw new UnsupportedOperationException("printPP(" + this.getClass().getSimpleName() + ")");
        }
    }

    ProgramPoint ENTRY = new ProgramPoint() { @Override public String toString() { return "ENTRY"; }};
    ProgramPoint EXIT = new ProgramPoint() { @Override public String toString() { return "EXIT"; }};
}
