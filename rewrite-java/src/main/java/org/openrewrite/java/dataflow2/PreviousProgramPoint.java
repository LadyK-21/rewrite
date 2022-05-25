package org.openrewrite.java.dataflow2;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.Cursor;
import org.openrewrite.internal.lang.NonNull;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.*;

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
    static @NonNull Collection<ProgramPoint> previousInIf(Cursor ifCursor, ProgramPoint p) {

        J.If ifThenElse = (J.If) ifCursor.getValue();
        J.ControlParentheses<Expression> cond = ifThenElse.getIfCondition();
        Statement thenPart = ifThenElse.getThenPart();
        J.If.@Nullable Else elsePart = ifThenElse.getElsePart();

        if(p == thenPart) {
            return Collections.singletonList(cond);
        } else if(p == elsePart) {
            return Collections.singletonList(cond);
        } else if(p == cond) {
            return DataFlowGraph.previous(ifCursor);
        }
        throw new IllegalStateException();
    }

    static @NonNull Collection<ProgramPoint> previousInIfElse(Cursor ifElseCursor, ProgramPoint p) {

        J.If.Else ifElse = (J.If.Else) ifElseCursor.getValue();
        Statement elsePart = ifElse.getBody();

        if(p == elsePart) {
            return DataFlowGraph.previous(ifElseCursor);
        }
        throw new IllegalStateException();
    }

    static @NonNull Collection<ProgramPoint> previousInWhileLoop(Cursor whileCursor, ProgramPoint p) {

        J.WhileLoop _while = (J.WhileLoop) whileCursor.getValue();
        J.ControlParentheses<Expression> cond = _while.getCondition();
        Statement body = _while.getBody();
        
        // while(cond: Expression) {
        //   body: Statement
        // }

        if(p == body) {
            return Collections.singletonList(cond);
        } else if(p == cond) {
            return DataFlowGraph.previous(whileCursor);
        }

        throw new UnsupportedOperationException("TODO");
    }

    static @NonNull Collection<ProgramPoint> previousInForLoop(Cursor forLoopCursor, ProgramPoint p) {

        // init: List<Statement>
        // while(cond: Expression) {
        //   body: Statement
        //   update: List<Statement>
        // }

        J.ForLoop forLoop = (J.ForLoop) forLoopCursor.getValue();

        if(p == forLoop.getControl()) {
            return DataFlowGraph.previous(forLoopCursor);
        }
        if(p == forLoop.getBody()) {
            return Collections.singletonList(forLoop.getControl().getCondition());
//            Cursor controlCursor = new Cursor(forLoopCursor, forLoop.getControl());
//            Set<ProgramPoint> result = new HashSet<>();
//            result.addAll(last(controlCursor, ForLoopPosition.INIT));
//            result.addAll(last(controlCursor, ForLoopPosition.UPDATE));
//            return result;
        }

        throw new IllegalStateException();
    }

    static @NonNull Collection<ProgramPoint> previousInForLoopControl(Cursor forLoopControlCursor, ProgramPoint p) {

        J.ForLoop.Control forLoopControl = (J.ForLoop.Control) forLoopControlCursor.getValue();

        List<Statement> init = forLoopControl.getInit();
        Expression cond = forLoopControl.getCondition();
        List<Statement> update = forLoopControl.getUpdate();

        int index;

        index = init.indexOf(p);
        if(index > 0) {
            return Collections.singletonList(init.get(index - 1));
        } else if(index == 0) {
            return DataFlowGraph.previous(forLoopControlCursor);
        }

        if(p == cond) {
            Set<ProgramPoint> result = new HashSet<>();
            result.addAll(last(forLoopControlCursor, ForLoopPosition.INIT));
            result.addAll(last(forLoopControlCursor, ForLoopPosition.UPDATE));
            return result;
        }

        index = update.indexOf(p);
        if(index > 0) {
            return Collections.singletonList(update.get(index - 1));
        } else if(index == 0) {
            Statement body = ((J.ForLoop)forLoopControlCursor.getParent().getValue()).getBody();
            return Collections.singletonList(body);
        }




        throw new IllegalStateException();
    }

    public static Collection<ProgramPoint> previousInParentheses(Cursor parenthesesCursor, ProgramPoint current) {
        J.Parentheses parentheses = (J.Parentheses) parenthesesCursor.getValue();

        if(current == parentheses.getTree()) {
            return DataFlowGraph.previous(parenthesesCursor);
        }
        throw new IllegalStateException();
    }

    public static Collection<ProgramPoint> previousInControlParentheses(Cursor parenthesesCursor, ProgramPoint current) {
        J.ControlParentheses parentheses = (J.ControlParentheses) parenthesesCursor.getValue();

        if(current == parentheses.getTree()) {
            return DataFlowGraph.previous(parenthesesCursor);
        }
        throw new IllegalStateException();
    }

    public static Collection<ProgramPoint> previousInUnary(Cursor unaryCursor, ProgramPoint current) {
        J.Unary unary = (J.Unary) unaryCursor.getValue();

        if (current == unary.getExpression()) {
            return DataFlowGraph.previous(unaryCursor);
        }
        throw new IllegalStateException();
    }

    public static Collection<ProgramPoint> previousInBinary(Cursor binaryCursor, ProgramPoint current) {
        J.Binary binary = (J.Binary) binaryCursor.getValue();

        Expression left = binary.getLeft();
        Expression right = binary.getRight();

        // TODO short-circuit operators &&, ||

        if(current == right) {
            return Collections.singletonList(left);
        } else if(current == left) {
            return DataFlowGraph.previous(binaryCursor);
        }
        throw new IllegalStateException();
    }

    public static Collection<ProgramPoint> previousInAssignment(Cursor assignmentCursor, ProgramPoint current) {
        J.Assignment assignment = (J.Assignment) assignmentCursor.getValue();

        Expression a = assignment.getAssignment();

        if(current == a) {
            return DataFlowGraph.previous(assignmentCursor);
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
