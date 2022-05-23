package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.internal.lang.NonNull;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.tree.J;

import java.util.Collection;
import java.util.Collections;

public class DataFlowGraph {

    // edge(p,q) = p in q.previous() = q in p.next()

    // Questions that the API must be able to answer :
    // what are all p, q such that predicate(p) and predicate(q) and there exists a path p.u1...uN.q ?
    // given p, what are all q such that predicate(q) and there exists a path p.u1...uN.q, or vice versa ?
    // ... and the value of q is the same as the value of p ?
    // ... and the path does not taint / untaint the value(s) ending inside q ?
    // what are all paths p.u1...uN.q, starting from p ? ending in q ?

    // reminder : paths may contain loops

    /**
     * @cursor A cursor whose value is a program point.
     * @return The set of program points preceding given program point in the dataflow graph.
     */
    public static @NonNull Collection<ProgramPoint> previous(Cursor cursor) {
        ProgramPoint current = (ProgramPoint) cursor.getValue();
        try {
            Cursor parentCursor = cursor.dropParentUntil(t -> t instanceof J);
            J parent = parentCursor.getValue();
            switch (parent.getClass().getName().replaceAll("^org.openrewrite.java.tree.", "")) {
                case "J$MethodInvocation":
                    return PreviousProgramPoint.previousInMethodInvocation(parentCursor, current);
                case "J$If":
                    return PreviousProgramPoint.previousInIf(parentCursor, current);
                case "J$If$Else":
                    return PreviousProgramPoint.previousInIfElse(parentCursor, current);
                case "J$WhileLoop":
                    return PreviousProgramPoint.previousInWhileLoop(parentCursor, current);
                case "J$ForLoop":
                    return PreviousProgramPoint.previousInForLoop(parentCursor, current);
                case "J$ForLoop$Control":
                    return PreviousProgramPoint.previousInForLoopControl(parentCursor, current);
                case "J$Block":
                    return PreviousProgramPoint.previousInBlock(parentCursor, current);
                case "J$VariableDeclarations":
                    return PreviousProgramPoint.previousInVariableDeclarations(parentCursor, current);
                case "J$CompilationUnit":
                case "J$ClassDeclaration":
                case "J$MethodDeclaration":
                    return Collections.emptyList();
                default:
                    throw new Error(parent.getClass().getSimpleName());
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static @Nullable Collection<ProgramPoint> next(Cursor cursor) {
        // TODO
        return null;
    }
}