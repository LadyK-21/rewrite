package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.Incubating;
import org.openrewrite.Tree;
import org.openrewrite.java.tree.J;

import java.util.Collection;

/**
 * A program point is anything that may change the state of a program.
 * It represents a node in the data-flow graph, embedded in the AST. Namely, program points are represented
 * by cursors and no additional data structures are needed. In the case of compound program points, the constants
 * ENTRY and EXIT are used to refer to the entry and exit program points within the compound.
 * <p>
 * There is an edge (p,q) in the dataflow graph iff. p \in sources(q) iff. q \in sinks(p).
 * A program point has an input state and an output state, they are related by the transfer function.
 * <p>
 * In Java, program points are statements, variable declarations, assignment expressions, increment and decrement
 * expressions, method declarations.
 */
@Incubating(since = "7.25.0")
public interface ProgramPoint {

    default Collection<Cursor> previous(DataFlowGraph dfg, Cursor c) {
        return dfg.previous(c);
    }

    ProgramPoint ENTRY = new ProgramPoint() {
        @Override
        public String toString() {
            return "ENTRY";
        }
    };

    ProgramPoint EXIT = new ProgramPoint() {
        @Override
        public String toString() {
            return "EXIT";
        }
    };
}
