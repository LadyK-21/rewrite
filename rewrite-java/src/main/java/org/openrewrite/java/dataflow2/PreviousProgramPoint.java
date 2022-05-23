package org.openrewrite.java.dataflow2;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.Cursor;
import org.openrewrite.internal.lang.NonNull;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.*;
import java.util.function.Supplier;

import static java.util.List.of;

public class PreviousProgramPoint<P> {
    // Find the previous statement when the current statement is part of a sequence of statements

    static @NonNull Collection<ProgramPoint> previousInBlock(Cursor parentCursor, ProgramPoint p)
    {
        J.Block parent = (J.Block) parentCursor.getValue();
        int index = parent.getStatements().indexOf(p);
        if(index > 0) {
            return Collections.singletonList(parent.getStatements().get(index-1));
        } else if(index == 0) {
            return DataFlowGraph.previous(parentCursor);
        }
        throw new IllegalStateException();
    }

    public static @NonNull Collection<ProgramPoint> previousInVariableDeclarations(Cursor parentCursor, ProgramPoint p) {
        J.VariableDeclarations parent = (J.VariableDeclarations) parentCursor.getValue();
        int index = parent.getVariables().indexOf(p);
        if(index > 0) {
            return Collections.singletonList(parent.getVariables().get(index-1));
        } else if(index == 0) {
            return DataFlowGraph.previous(parentCursor);
        }
        throw new IllegalStateException();
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
    static @NonNull Collection<ProgramPoint> last(Cursor parentCursor, ForLoopPosition position) {

        J.ForLoop.Control parent = (J.ForLoop.Control) parentCursor.getValue();

        List<Statement> init = parent.getInit();
        List<Statement> update = parent.getUpdate();

        if(position == ForLoopPosition.UPDATE) {
            if(update.size() > 0) {
                return Collections.singletonList(update.get(update.size()-1));
            } else {
                return last(parentCursor, ForLoopPosition.INIT);
            }
        }
        if(position == ForLoopPosition.INIT) {
            if(init.size() > 0) {
                return Collections.singletonList(init.get(init.size()-1));
            } else {
                return DataFlowGraph.previous(parentCursor);
            }
        }
        throw new IllegalStateException();
    }

    static @NonNull Collection<ProgramPoint> previousInMethodInvocation(Cursor parentCursor, ProgramPoint p) {

        J.MethodInvocation parent = (J.MethodInvocation) parentCursor.getValue();

//        int index = parent.getArguments().indexOf(p);
//        if(index > 0) {
                // an argument is an expression
//            return Collections.singletonList(parent.getArguments().get(index-1));
//        } else if(index == 0) {
//            return DataFlowGraph.previous(parentCursor);
//        }
        return Collections.emptyList();
    }
    static @NonNull Collection<ProgramPoint> previousInIf(Cursor parentCursor, ProgramPoint p) {

        J.If parent = (J.If) parentCursor.getValue();

        throw new UnsupportedOperationException("TODO");
    }

    static @NonNull Collection<ProgramPoint> previousInIfElse(Cursor parentCursor, ProgramPoint p) {

        J.If.Else parent = (J.If.Else) parentCursor.getValue();

        throw new UnsupportedOperationException("TODO");
    }

    static @NonNull Collection<ProgramPoint> previousInWhileLoop(Cursor parentCursor, ProgramPoint p) {

        J.WhileLoop parent = (J.WhileLoop) parentCursor.getValue();

        throw new UnsupportedOperationException("TODO");
    }

    static @NonNull Collection<ProgramPoint> previousInForLoop(Cursor forLoopCursor, ProgramPoint p) {

        // init: List<Statement>
        // while(cond: Expression) {
        //   body: Statement
        //   update: List<Statement>
        // }

        // TODO: expression

        J.ForLoop forLoop = (J.ForLoop) forLoopCursor.getValue();

        if(p == forLoop.getControl()) {
            return DataFlowGraph.previous(forLoopCursor);
        }
        if(p == forLoop.getBody()) {
            Cursor controlCursor = new Cursor(forLoopCursor, forLoop.getControl());
            Set<ProgramPoint> result = new HashSet<>();
            result.addAll(last(controlCursor, ForLoopPosition.INIT));
            result.addAll(last(controlCursor, ForLoopPosition.UPDATE));
            return result;
        }

        throw new IllegalStateException();
    }

    static @NonNull Collection<ProgramPoint> previousInForLoopControl(Cursor forLoopControlCursor, ProgramPoint p) {

        J.ForLoop.Control forLoopControl = (J.ForLoop.Control) forLoopControlCursor.getValue();

        List<Statement> init = forLoopControl.getInit();
        List<Statement> update = forLoopControl.getUpdate();

        int index;

        index = update.indexOf(p);
        if(index > 0) {
            return Collections.singletonList(update.get(index - 1));
        } else if(index == 0) {
            return last(forLoopControlCursor, ForLoopPosition.INIT);
        }

        index = init.indexOf(p);
        if(index > 0) {
            return Collections.singletonList(init.get(index - 1));
        } else if(index == 0) {
            return DataFlowGraph.previous(forLoopControlCursor);
        }

        throw new IllegalStateException();
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
            throw new UnsupportedOperationException("TODO");
        }

        @Override
        public J.@NotNull ForLoop visitForLoop(J.@NotNull ForLoop loop, Object o) {
            previous = PreviousProgramPoint.previousInForLoop(getCursor(), current);
            return loop;
        }

        @Override
        public J.@NotNull MethodDeclaration visitMethodDeclaration(J.@NotNull MethodDeclaration method, P p) {
            throw new UnsupportedOperationException("TODO");
        }

    }
}
