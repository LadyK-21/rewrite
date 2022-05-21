package org.openrewrite.java.dataflow2;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.Cursor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static java.util.List.of;

public class PreviousProgramPoint<P> {
    // Find the previous statement when the current statement is part of a sequence of statements

    static Collection<ProgramPoint> previousInBlock(Cursor parentCursor, ProgramPoint p)
    {
        J.Block parent = (J.Block) parentCursor.getValue();
        int index = parent.getStatements().indexOf(p);
        if(index > 0) {
            return Collections.singletonList(parent.getStatements().get(index-1));
        } else {
            return DataFlowGraph.previous(parentCursor);
        }
    }

    public static Collection<ProgramPoint> previousInVariableDeclarations(Cursor parentCursor, ProgramPoint p) {
        J.VariableDeclarations parent = (J.VariableDeclarations) parentCursor.getValue();
        int index = parent.getVariables().indexOf(p);
        if(index > 0) {
            return Collections.singletonList(parent.getVariables().get(index-1));
        } else {
            return DataFlowGraph.previous(parentCursor);
        }
    }


    /** Identifies one of the statement lists in a for-loop. */
    enum ForLoopPosition {
        INIT,
        CONDITION,
        UPDATE
    }

    /**
     * @return The last program point(s) in the for-loop at given position, which might be
     * last of the previous position if given position is empty, or even the previous program point
     * of the for-loop if all preceding positions are empty.
     */
    static Collection<ProgramPoint> last(Cursor parentCursor, ForLoopPosition position) {

        J.ForLoop.Control parent = (J.ForLoop.Control) parentCursor.getValue();

        List<Statement> init = parent.getInit();
        List<Statement> update = parent.getInit();

        if(position == ForLoopPosition.UPDATE) {
            if(update.size() > 0) {
                return Collections.singletonList(update.get(update.size()-1));
            } else {
                return last(parentCursor, ForLoopPosition.INIT);
            }
        }
        if(position == ForLoopPosition.INIT) {
            if(update.size() > 0) {
                return Collections.singletonList(update.get(update.size()-1));
            } else {
                return DataFlowGraph.previous(parentCursor);
            }
        }
        return null;
    }

    static Collection<ProgramPoint> previousInMethodInvocation(Cursor parentCursor, ProgramPoint p) {

        J.MethodInvocation parent = (J.MethodInvocation) parentCursor.getValue();

        // TODO

        return null;
    }
    static Collection<ProgramPoint> previousInIf(Cursor parentCursor, ProgramPoint p) {

        J.If parent = (J.If) parentCursor.getValue();

        // TODO

        return null;
    }

    static Collection<ProgramPoint> previousInIfElse(Cursor parentCursor, ProgramPoint p) {

        J.If.Else parent = (J.If.Else) parentCursor.getValue();

        // TODO

        return null;
    }

    static Collection<ProgramPoint> previousInWhileLoop(Cursor parentCursor, ProgramPoint p) {

        J.WhileLoop parent = (J.WhileLoop) parentCursor.getValue();

        // TODO

        return null;
    }

    static Collection<ProgramPoint> previousInForLoop(Cursor parentCursor, ProgramPoint p) {

        J.ForLoop parent = (J.ForLoop) parentCursor.getValue();

        if(p == parent.getBody()) {
            List<ProgramPoint> result = new ArrayList<>();
            result.add(parent.getBody());
            Cursor controlCursor = new Cursor(parentCursor, parent.getControl());
            result.addAll(last(controlCursor, ForLoopPosition.UPDATE));
            return result;
        }

        return null;
    }

    static Collection<ProgramPoint> previousInForLoopControl(Cursor parentCursor, ProgramPoint p) {

        J.ForLoop.Control parent = (J.ForLoop.Control) parentCursor.getValue();

        List<Statement> init = parent.getInit();
        List<Statement> update = parent.getInit();

        int index;
        ProgramPoint previous;

        index = update.indexOf(p);
        if(index > 0) {
            return Collections.singletonList(update.get(index - 1));
        } else if(index == 0) {
            return last(parentCursor, ForLoopPosition.INIT);
        }

        index = init.indexOf(p);
        if(index > 0) {
            return Collections.singletonList(init.get(index - 1));
        } else if(index == 0) {
            return DataFlowGraph.previous(parentCursor);
        }

        return null;
    }


    static class Visitor<P> extends JavaIsoVisitor<P> {

        final ProgramPoint current;
        Collection<ProgramPoint> previous = null;

        public <T> Visitor(ProgramPoint current) {
            super();
            this.current = current;
        }

        @Override
        public J.Block visitBlock(J.Block block, Object o) {
            previous = PreviousProgramPoint.previousInBlock(getCursor(), current);
            return block;
        }

        @Override
        public J.Case visitCase(J.Case _case, P p) {
            // TODO
            return super.visitCase(_case, p);
        }

        @Override
        public J.@NotNull ForLoop visitForLoop(J.@NotNull ForLoop loop, Object o) {
            previous = PreviousProgramPoint.previousInForLoop(getCursor(), current);
            return loop;
        }

        @Override
        public J.@NotNull MethodDeclaration visitMethodDeclaration(J.@NotNull MethodDeclaration method, P p) {
            // TODO
            return super.visitMethodDeclaration(method, p);
        }

    }
}
