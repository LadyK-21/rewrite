package org.openrewrite.java.dataflow2;

import lombok.AllArgsConstructor;
import org.openrewrite.Cursor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.openrewrite.java.dataflow2.ProgramPoint.ENTRY;
import static org.openrewrite.java.dataflow2.ProgramPoint.EXIT;

@AllArgsConstructor
public class Edge {

    ProgramPoint from;
    ProgramPoint to;

    public static Collection<Edge> edges(Cursor programPoint) {
        switch (programPoint.getClass().getName().replaceAll("^org.openrewrite.java.tree.", "")) {
            case "J$Binary":
                J.Binary binary = programPoint.getValue();

                Expression left = binary.getLeft();
                Expression right = binary.getRight();
                J.Binary.Type op = binary.getOperator();

                return edges(
                    edge(ENTRY, left),
                    edge(left, right),
                    edge(right, binary),
                    edge(binary, EXIT)
                );
                
            default:
                return null;
        }
    }

    private static Collection<Edge> edges(Edge... edges) {
        return Arrays.asList(edges);
    }

    private static Edge edge(ProgramPoint from, ProgramPoint to) {
        return new Edge(from, to);
    }

}
