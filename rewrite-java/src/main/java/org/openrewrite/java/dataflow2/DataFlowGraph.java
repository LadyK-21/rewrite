package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.internal.lang.NonNull;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.*;
import java.util.stream.Collectors;

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
    public static @NonNull Collection<Cursor> previous(Cursor cursor) {
        ProgramPoint pp = (ProgramPoint) cursor.getValue();
        return last(previous2(cursor, pp));
    }

    public static @NonNull Collection<Cursor> last(Cursor cursor) {
        return previous2(cursor, ProgramPoint.EXIT);
    }

    public static @NonNull Collection<Cursor> last(Collection<Cursor> cc) {
        Set<Cursor> result = new HashSet<>();
        for(Cursor c : cc) {
            result.addAll(last(c));
        }
        return result;
    }

    public static @NonNull Collection<Cursor> previous2(Cursor cursor, ProgramPoint current) {
        try {

            Cursor parentCursor = cursor.dropParentUntil(t -> t instanceof J);
            J parent = parentCursor.getValue();
            switch (parent.getClass().getName().replaceAll("^org.openrewrite.java.tree.", "")) {
                case "J$MethodInvocation":
                    return previousInMethodInvocation(parentCursor, current);
                case "J$If":
                    return previousInIf(parentCursor, current);
                case "J$If$Else":
                    return previousInIfElse(parentCursor, current);
                case "J$WhileLoop":
                    return previousInWhileLoop(parentCursor, current);
                case "J$ForLoop":
                    return previousInForLoop(parentCursor, current);
                case "J$ForLoop$Control":
                    return previousInForLoopControl(parentCursor, current);
                case "J$Block":
                    return previousInBlock(parentCursor, current);
                case "J$VariableDeclarations":
                    return previousInVariableDeclarations(parentCursor, current);
//                case "J$NamedVariable":
//                    return PreviousProgramPoint.previousInVariableDeclarations(parentCursor, current);
                case "J$Unary":
                    return previousInUnary(parentCursor, current);
                case "J$Binary":
                    return previousInBinary(parentCursor, current);
                case "J$Assignment":
                    return previousInAssignment(parentCursor, current);
                case "J$Parentheses":
                    return previousInParentheses(parentCursor, current);
                case "J$ControlParentheses":
                    return previousInControlParentheses(parentCursor, current);
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

    public static @Nullable Collection<Cursor> next(Cursor cursor) {
        // TODO
        return null;
    }


    static @NonNull Collection<Cursor> previousInBlock(Cursor parentCursor, ProgramPoint p)
    {
        J.Block parent = (J.Block) parentCursor.getValue();
        List<Statement> stmts = parent.getStatements();
        if(p == ProgramPoint.EXIT) {
            if(stmts.size() > 0) {
                return Collections.singletonList(new Cursor(parentCursor, stmts.get(stmts.size() - 1)));
            } else {
                return DataFlowGraph.previous(parentCursor);
            }
        } else {
            int index = stmts.indexOf(p);
            if (index > 0) {
                return Collections.singletonList(new Cursor(parentCursor, stmts.get(index - 1)));
            } else if (index == 0) {
                return DataFlowGraph.previous(parentCursor);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public static @NonNull Collection<Cursor> previousInVariableDeclarations(Cursor parentCursor, ProgramPoint p) {
        J.VariableDeclarations parent = (J.VariableDeclarations) parentCursor.getValue();
        List<J.VariableDeclarations.NamedVariable> variables = parent.getVariables();
        if(p == ProgramPoint.EXIT) {
            if (variables.size() > 0) {
                return Collections.singletonList(new Cursor(parentCursor, variables.get(variables.size() - 1)));
            } else {
                return DataFlowGraph.previous(parentCursor);
            }
        } else {
            int index = parent.getVariables().indexOf(p);
            if (index > 0) {
                return Collections.singletonList(new Cursor(parentCursor, variables.get(index - 1)));
            } else if (index == 0) {
                return DataFlowGraph.previous(parentCursor);
            } else {
                throw new IllegalStateException();
            }
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
    static @NonNull Collection<Cursor> last(Cursor forLoopCursor, ForLoopPosition position) {

        J.ForLoop forLoop = (J.ForLoop) forLoopCursor.getValue();
        J.ForLoop.Control control = forLoop.getControl();

        List<Statement> init = control.getInit();
        List<Statement> update = control.getUpdate();

        if(position == ForLoopPosition.UPDATE) {
            if(update.size() > 0) {
                return Collections.singletonList(new Cursor(forLoopCursor, update.get(update.size()-1)));
            } else {
                return last(forLoopCursor, ForLoopPosition.INIT);
            }
        }
        if(position == ForLoopPosition.INIT) {
            if(init.size() > 0) {
                return Collections.singletonList(new Cursor(forLoopCursor, init.get(init.size()-1)));
            } else {
                return DataFlowGraph.previous(forLoopCursor);
            }
        }
        throw new IllegalStateException();
    }

    static @NonNull Collection<Cursor> previousInMethodInvocation(Cursor parentCursor, ProgramPoint p) {

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
    static @NonNull Collection<Cursor> previousInIf(Cursor ifCursor, ProgramPoint p) {

        J.If ifThenElse = (J.If) ifCursor.getValue();
        J.ControlParentheses<Expression> cond = ifThenElse.getIfCondition();
        Statement thenPart = ifThenElse.getThenPart();
        J.If.@Nullable Else elsePart = ifThenElse.getElsePart();

        if(p == ProgramPoint.EXIT) {
            Set<Cursor> result = new HashSet<>();
            result.add(new Cursor(ifCursor, thenPart));
            if(elsePart != null) {
                result.add(new Cursor(ifCursor, elsePart));
            }
            return result;
        } else if(p == thenPart) {
            return Collections.singletonList(new Cursor(ifCursor, cond));
        } else if(p == elsePart) {
            return Collections.singletonList(new Cursor(ifCursor, cond));
        } else if(p == cond) {
            return DataFlowGraph.previous(ifCursor);
        }
        throw new IllegalStateException();
    }

    static @NonNull Collection<Cursor> previousInIfElse(Cursor ifElseCursor, ProgramPoint p) {

        J.If.Else ifElse = (J.If.Else) ifElseCursor.getValue();
        Statement body = ifElse.getBody();

        if(p == ProgramPoint.EXIT) {
            return Collections.singletonList(new Cursor(ifElseCursor, body));
        } else if(p == body) {
            return DataFlowGraph.previous(ifElseCursor);
        }
        throw new IllegalStateException();
    }

    static @NonNull Collection<Cursor> previousInWhileLoop(Cursor whileCursor, ProgramPoint p) {

        J.WhileLoop _while = (J.WhileLoop) whileCursor.getValue();
        J.ControlParentheses<Expression> cond = _while.getCondition();
        Statement body = _while.getBody();

        // while(cond: Expression) {
        //   body: Statement
        // }

        if(p == ProgramPoint.EXIT) {
            Set<Cursor> result = new HashSet<>();
            result.add(new Cursor(whileCursor, body));
            result.add(new Cursor(whileCursor, cond));
            return result;
        } else if(p == body) {
            return Collections.singletonList(new Cursor(whileCursor, cond));
        } else if(p == cond) {
            return DataFlowGraph.previous(whileCursor);
        }

        throw new UnsupportedOperationException("TODO");
    }

    static @NonNull Collection<Cursor> previousInForLoop(Cursor forLoopCursor, ProgramPoint p) {

        // init: List<Statement>
        // while(cond: Expression) {
        //   body: Statement
        //   update: List<Statement>
        // }

        J.ForLoop forLoop = (J.ForLoop) forLoopCursor.getValue();
        List<Statement> init = forLoop.getControl().getInit();
        Expression cond = forLoop.getControl().getCondition();
        Statement body = forLoop.getBody();
        List<Statement> update = forLoop.getControl().getUpdate();

        if(p == ProgramPoint.EXIT) {
            Set<Cursor> result = new HashSet<>();
            result.add(new Cursor(forLoopCursor, update));
            result.add(new Cursor(forLoopCursor, cond));
            return result;

        } else if(p == body) {
            return Collections.singletonList(new Cursor(forLoopCursor, cond));

        } else  if (p == cond) {
            Set<Cursor> result = new HashSet<>();
            result.addAll(last(forLoopCursor, ForLoopPosition.INIT));
            result.addAll(last(forLoopCursor, ForLoopPosition.UPDATE));
            return result;

        } else {
            int index;

            index = init.indexOf(p);
            if (index > 0) {
                return Collections.singletonList(new Cursor(forLoopCursor, init.get(index - 1)));
            } else if (index == 0) {
                return DataFlowGraph.previous(forLoopCursor);
            }

            index = update.indexOf(p);
            if (index > 0) {
                return Collections.singletonList(new Cursor(forLoopCursor, update.get(index - 1)));
            } else if (index == 0) {
                return Collections.singletonList(new Cursor(forLoopCursor, body));
            }

            throw new IllegalStateException();
        }
    }

    static @NonNull Collection<Cursor> previousInForLoopControl(Cursor forLoopControlCursor, ProgramPoint p) {

        J.ForLoop.Control forLoopControl = (J.ForLoop.Control) forLoopControlCursor.getValue();
        return previousInForLoop(forLoopControlCursor.getParent(), p);
    }

    public static Collection<Cursor> previousInParentheses(Cursor parenthesesCursor, ProgramPoint p) {
        J.Parentheses parentheses = (J.Parentheses) parenthesesCursor.getValue();
        J tree = parentheses.getTree();

        if(p == ProgramPoint.EXIT) {
            return Collections.singletonList(new Cursor(parenthesesCursor, tree));
        } else if(p == tree) {
            return DataFlowGraph.previous(parenthesesCursor);
        }
        throw new IllegalStateException();
    }

    public static Collection<Cursor> previousInControlParentheses(Cursor parenthesesCursor, ProgramPoint p) {
        J.ControlParentheses parentheses = (J.ControlParentheses) parenthesesCursor.getValue();
        J tree = parentheses.getTree();
        
        if(p == ProgramPoint.EXIT) {
            return Collections.singletonList(new Cursor(parenthesesCursor, tree));
        } else if(p == tree) {
            return DataFlowGraph.previous(parenthesesCursor);
        }
        throw new IllegalStateException();
    }

    public static Collection<Cursor> previousInUnary(Cursor unaryCursor, ProgramPoint p) {
        J.Unary unary = (J.Unary) unaryCursor.getValue();
        Expression expr = unary.getExpression();

        if(p == ProgramPoint.EXIT) {
            return Collections.singletonList(new Cursor(unaryCursor, expr));
        } else if(p == unary.getExpression()) {
            return DataFlowGraph.previous(unaryCursor);
        }
        throw new IllegalStateException();
    }

    public static Collection<Cursor> previousInBinary(Cursor binaryCursor, ProgramPoint p) {
        J.Binary binary = (J.Binary) binaryCursor.getValue();

        Expression left = binary.getLeft();
        Expression right = binary.getRight();
        J.Binary.Type op = binary.getOperator();

        if(p == ProgramPoint.EXIT) {
            if(op == J.Binary.Type.And || op == J.Binary.Type.Or) {
                // short-circuit operators
                Set<Cursor> result = new HashSet<>();
                result.add(new Cursor(binaryCursor, right));
                result.add(new Cursor(binaryCursor, left));
                return result;
            } else {
                return Collections.singletonList(new Cursor(binaryCursor, right));
            }
        } else if(p == right) {
            return Collections.singletonList(new Cursor(binaryCursor, left));
        } else if(p == left) {
            return DataFlowGraph.previous(binaryCursor);
        }
        throw new IllegalStateException();
    }

    public static Collection<Cursor> previousInAssignment(Cursor assignmentCursor, ProgramPoint p) {
        J.Assignment assignment = (J.Assignment) assignmentCursor.getValue();
        Expression a = assignment.getAssignment();

        if(p == ProgramPoint.EXIT) {
            return Collections.singletonList(new Cursor(assignmentCursor, a));
        } else if(p == a) {
            return DataFlowGraph.previous(assignmentCursor);
        }
        throw new IllegalStateException();
    }

}