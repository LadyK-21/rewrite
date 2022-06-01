package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.internal.lang.NonNull;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.tree.*;

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
     * @param programPoint A cursor whose value is a program point.
     * @return The set of primitive program points (i.e., not containing other program points)
     * directly preceding given program point in the dataflow graph.
     */
    public static @NonNull Collection<Cursor> primitiveSources(Cursor programPoint) {
        Collection<Cursor> pp = sources(programPoint);
        return last(pp);
    }

    /**
     * @return If cursor is a compound program point (e.g. a while loop), return the last
     * program point(s) inside that compound. Otherwise return the program point itself.
     */
    public static @NonNull Collection<Cursor> last(Cursor cursor) {
        Collection<Cursor> result = previousSourcesIn(cursor, ProgramPoint.EXIT);
//        System.out.print("    last(" + print(cursor) + ") = {");
//        for(Cursor r : result) {
//            System.out.print(" <" + print(r) + ">");
//        }
//        System.out.println(" }");
        return result;
    }

    public static @NonNull Collection<Cursor> last(Collection<Cursor> cc) {
        Set<Cursor> result = new HashSet<>();
        for (Cursor c : cc) {
            result.addAll(last(c));
        }
        return result;
    }

    /**
     * @param programPoint A cursor whose value is a program point.
     * @return The set of program points, possibly composite (i.e. containing other program points, such as
     * a while loop), preceding given program point in the dataflow graph.
     */
    public static @NonNull Collection<Cursor> sources(Cursor programPoint) {
        ProgramPoint current = programPoint.getValue();

        switch (current.getClass().getName().replaceAll("^org.openrewrite.java.tree.", "")) {
            // either the previous PP is inside current...
            case "J$VariableDeclarations$NamedVariable": {
                J.VariableDeclarations.NamedVariable v = (J.VariableDeclarations.NamedVariable) current;
                Expression init = v.getInitializer();
                if (init != null) {
                    return Collections.singletonList(new Cursor(programPoint, init));
                }
                break;
            }
            case "J$Binary":
                return Collections.singletonList(new Cursor(programPoint, ((J.Binary) current).getRight()));
            case "J$Unary":
                return Collections.singletonList(new Cursor(programPoint, ((J.Unary) current).getExpression()));
            case "J$ControlParentheses":
                return Collections.singletonList(new Cursor(programPoint, ((J.ControlParentheses) current).getTree()));
            case "J$Parentheses":
                return Collections.singletonList(new Cursor(programPoint, ((J.Parentheses) current).getTree()));
            case "J$MethodInvocation": {
                J.MethodInvocation m = (J.MethodInvocation) current;
                List<Expression> args = m.getArguments();
                if (args.size() > 0) {
                    return Collections.singletonList(new Cursor(programPoint, args.get(args.size() - 1)));
                } else {
                    return Collections.singletonList(new Cursor(programPoint, m.getSelect()));
                }
            }
        }
        // ... or it is the previous PP in the parent
        Cursor parentCursor = programPoint.dropParentUntil(t -> t instanceof J);
        return previousSourcesIn(parentCursor, current);
    }

    public static @NonNull Collection<Cursor> previousSourcesIn(Cursor parentCursor, ProgramPoint current) {
        if (parentCursor.getValue() instanceof JLeftPadded) return previousSourcesIn(parentCursor.getParentOrThrow(), current);
        if (parentCursor.getValue() instanceof JRightPadded)
            return previousSourcesIn(parentCursor.getParentOrThrow(), current);
        try {
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
                case "J$VariableDeclarations$NamedVariable":
                    return previousInNamedVariable(parentCursor, current);
                case "J$Literal":
                case "J$Identifier":
                case "J$Empty":
                    return previousInTerminalNode(parentCursor, current);
                case "J$CompilationUnit":
                case "J$ClassDeclaration":
                case "J$MethodDeclaration":
                    return Collections.emptyList();
                default:
                    throw new Error(parent.getClass().getName());
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static @Nullable Collection<Cursor> next(Cursor cursor) {
        // TODO
        return null;
    }


    static @NonNull Collection<Cursor> previousInBlock(Cursor parentCursor, ProgramPoint p) {
        J.Block parent = parentCursor.getValue();
        List<Statement> stmts = parent.getStatements();
        if (p == ProgramPoint.EXIT) {
            if (stmts.size() > 0) {
                return Collections.singletonList(new Cursor(parentCursor, stmts.get(stmts.size() - 1)));
            } else {
                return DataFlowGraph.primitiveSources(parentCursor);
            }
        } else {
            int index = stmts.indexOf(p);
            if (index > 0) {
                return Collections.singletonList(new Cursor(parentCursor, stmts.get(index - 1)));
            } else if (index == 0) {
                return DataFlowGraph.primitiveSources(parentCursor);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public static @NonNull Collection<Cursor> previousInVariableDeclarations(Cursor parentCursor, ProgramPoint p) {
        J.VariableDeclarations parent = parentCursor.getValue();
        List<J.VariableDeclarations.NamedVariable> variables = parent.getVariables();
        if (p == ProgramPoint.EXIT) {
            if (variables.size() > 0) {
                return Collections.singletonList(new Cursor(parentCursor, variables.get(variables.size() - 1)));
            } else {
                return DataFlowGraph.primitiveSources(parentCursor);
            }
        } else if (p == ProgramPoint.ENTRY) {
            return DataFlowGraph.primitiveSources(parentCursor);
        } else if (p == parent.getTypeExpression()) {
            // p is not a program point
            return Collections.emptyList();
        } else {
            int index = parent.getVariables().indexOf(p);
            if (index > 0) {
                return Collections.singletonList(new Cursor(parentCursor, variables.get(index - 1)));
            } else if (index == 0) {
                return DataFlowGraph.primitiveSources(parentCursor);
            } else {
                throw new IllegalStateException();
            }
        }
    }


    /**
     * Identifies one of the statement lists in a for-loop.
     */
    enum ForLoopPosition {
        INIT, CONDITION, UPDATE
    }

    /**
     * @return The last program point(s) in the for-loop at given position, which might be
     * last of the previous position if given position is empty, or even the previous program point
     * of the for-loop if all preceding positions are empty.
     */
    static @NonNull Collection<Cursor> last(Cursor forLoopCursor, ForLoopPosition position) {

        J.ForLoop forLoop = forLoopCursor.getValue();
        J.ForLoop.Control control = forLoop.getControl();

        List<Statement> init = control.getInit();
        List<Statement> update = control.getUpdate();

        if (position == ForLoopPosition.UPDATE) {
            if (update.size() > 0) {
                return Collections.singletonList(new Cursor(forLoopCursor, update.get(update.size() - 1)));
            } else {
                return last(forLoopCursor, ForLoopPosition.INIT);
            }
        }
        if (position == ForLoopPosition.INIT) {
            if (init.size() > 0) {
                return Collections.singletonList(new Cursor(forLoopCursor, init.get(init.size() - 1)));
            } else {
                return DataFlowGraph.primitiveSources(forLoopCursor);
            }
        }
        throw new IllegalStateException();
    }

    static @NonNull Collection<Cursor> previousInMethodInvocation(Cursor parentCursor, ProgramPoint p) {

        J.MethodInvocation parent = parentCursor.getValue();

        if (p == ProgramPoint.EXIT) {
            return Collections.singletonList(parentCursor);
        } else if (p == ProgramPoint.ENTRY) {
            return previousSourcesIn(parentCursor.getParent(), parentCursor.getValue());
        } else if (p == parent.getSelect()) {
            return DataFlowGraph.primitiveSources(parentCursor);
        } else {
            List<Expression> args = parent.getArguments();
            int index = args.indexOf(p);
            if (index > 0) {
                return Collections.singletonList(new Cursor(parentCursor, args.get(index - 1)));
            } else if (index == 0) {
                if (parent.getSelect() != null) {
                    return Collections.singletonList(new Cursor(parentCursor, parent.getSelect()));
                } else {
                    // implicit this
                    return DataFlowGraph.previousSourcesIn(parentCursor, ProgramPoint.ENTRY);
                }
            } else {
                throw new IllegalStateException();
            }
        }
    }

    static @NonNull Collection<Cursor> previousInIf(Cursor ifCursor, ProgramPoint p) {

        J.If ifThenElse = ifCursor.getValue();
        J.ControlParentheses<Expression> cond = ifThenElse.getIfCondition();
        Statement thenPart = ifThenElse.getThenPart();
        J.If.@Nullable Else elsePart = ifThenElse.getElsePart();

        if (p == ProgramPoint.EXIT) {
            Set<Cursor> result = new HashSet<>();
            result.add(new Cursor(ifCursor, thenPart));
            if (elsePart != null) {
                result.add(new Cursor(ifCursor, elsePart));
            }
            return result;
        } else if (p == thenPart) {
            return Collections.singletonList(new Cursor(ifCursor, cond));
        } else if (p == elsePart) {
            return Collections.singletonList(new Cursor(ifCursor, cond));
        } else if (p == cond) {
            return DataFlowGraph.primitiveSources(ifCursor);
        }
        throw new IllegalStateException();
    }

    static @NonNull Collection<Cursor> previousInIfElse(Cursor ifElseCursor, ProgramPoint p) {

        J.If.Else ifElse = ifElseCursor.getValue();
        Statement body = ifElse.getBody();

        if (p == ProgramPoint.EXIT) {
            return Collections.singletonList(new Cursor(ifElseCursor, body));
        } else if (p == body) {
            return DataFlowGraph.primitiveSources(ifElseCursor);
        }
        throw new IllegalStateException();
    }

    static @NonNull Collection<Cursor> previousInWhileLoop(Cursor whileCursor, ProgramPoint p) {

        J.WhileLoop _while = whileCursor.getValue();
        J.ControlParentheses<Expression> cond = _while.getCondition();
        Statement body = _while.getBody();

        // while(cond: Expression) {
        //   body: Statement
        // }

        if (p == ProgramPoint.EXIT) {
            Set<Cursor> result = new HashSet<>();
            result.add(new Cursor(whileCursor, body));
            result.add(new Cursor(whileCursor, cond));
            return result;
        } else if (p == body) {
            return Collections.singletonList(new Cursor(whileCursor, cond));
        } else if (p == cond) {
            return DataFlowGraph.primitiveSources(whileCursor);
        }

        throw new UnsupportedOperationException("TODO");
    }

    static @NonNull Collection<Cursor> previousInForLoop(Cursor forLoopCursor, ProgramPoint p) {

        // init: List<Statement>
        // while(cond: Expression) {
        //   body: Statement
        //   update: List<Statement>
        // }

        J.ForLoop forLoop = forLoopCursor.getValue();
        List<Statement> init = forLoop.getControl().getInit();
        Expression cond = forLoop.getControl().getCondition();
        Statement body = forLoop.getBody();
        List<Statement> update = forLoop.getControl().getUpdate();


        if (p == ProgramPoint.EXIT) {
            Set<Cursor> result = new HashSet<>();
            result.add(new Cursor(forLoopCursor, update));
            result.add(new Cursor(forLoopCursor, cond));
            return result;

        } else if (p == ProgramPoint.ENTRY) {
            return Collections.singletonList(new Cursor(forLoopCursor, cond));

        } else if (p == body) {
            return Collections.singletonList(new Cursor(forLoopCursor, cond));

        } else if (p == cond) {
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
                return DataFlowGraph.primitiveSources(forLoopCursor);
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

        J.ForLoop.Control forLoopControl = forLoopControlCursor.getValue();
        return previousInForLoop(forLoopControlCursor.getParentOrThrow(), p);
    }

    public static Collection<Cursor> previousInParentheses(Cursor parenthesesCursor, ProgramPoint p) {
        J.Parentheses<?> parentheses = parenthesesCursor.getValue();
        J tree = parentheses.getTree();

        if (p == ProgramPoint.EXIT) {
            return Collections.singletonList(new Cursor(parenthesesCursor, tree));
        } else if (p == tree) {
            return DataFlowGraph.primitiveSources(parenthesesCursor);
        }
        throw new IllegalStateException();
    }

    public static Collection<Cursor> previousInControlParentheses(Cursor parenthesesCursor, ProgramPoint p) {
        J.ControlParentheses<?> parentheses = parenthesesCursor.getValue();
        J tree = parentheses.getTree();

        if (p == ProgramPoint.EXIT) {
            return Collections.singletonList(new Cursor(parenthesesCursor, tree));
        } else if (p == tree) {
            return DataFlowGraph.primitiveSources(parenthesesCursor);
        }
        throw new IllegalStateException();
    }

    public static Collection<Cursor> previousInNamedVariable(Cursor namedVariableCursor, ProgramPoint p) {
        J.VariableDeclarations.NamedVariable namedVariable = namedVariableCursor.getValue();
        J.Identifier name = namedVariable.getName();
        Expression initializer = namedVariable.getInitializer();

        if (p == ProgramPoint.EXIT) {
            return Collections.singletonList(namedVariableCursor);
        } else if (p == ProgramPoint.ENTRY) {
            return DataFlowGraph.previousSourcesIn(namedVariableCursor.getParentOrThrow(), namedVariableCursor.getValue());
        } else if (p == name) {
            // it is an accident that 'name' is an expression, asking for its previous program point doesn't really make sense
            return Collections.emptyList();
        } else if (p == initializer) {
            return previousSourcesIn(namedVariableCursor.getParentOrThrow(), namedVariableCursor.getValue());
            //return Collections.singletonList(new Cursor(namedVariableCursor, namedVariable.getInitializer()));
        }
        throw new IllegalStateException();
    }

    public static Collection<Cursor> previousInUnary(Cursor unaryCursor, ProgramPoint p) {
        J.Unary unary = unaryCursor.getValue();
        Expression expr = unary.getExpression();

        if (p == ProgramPoint.ENTRY) {
            return previousSourcesIn(unaryCursor.getParentOrThrow(), unaryCursor.getValue());
        } else if (p == ProgramPoint.EXIT) {
            return Collections.singletonList(unaryCursor);
        } else if (p == unary.getExpression()) {
            return previousSourcesIn(unaryCursor, ProgramPoint.ENTRY);
        }
        throw new IllegalStateException();
    }

    public static Collection<Cursor> previousInBinary(Cursor binaryCursor, ProgramPoint p) {
        J.Binary binary = binaryCursor.getValue();

        Expression left = binary.getLeft();
        Expression right = binary.getRight();
        J.Binary.Type op = binary.getOperator();

        if (p == ProgramPoint.ENTRY) {
            return previousSourcesIn(binaryCursor.getParentOrThrow(), binaryCursor.getValue());
        } else if (p == ProgramPoint.EXIT) {
            return Collections.singletonList(binaryCursor);
            /*
            if(op == J.Binary.Type.And || op == J.Binary.Type.Or) {
                // short-circuit operators
                Set<Cursor> result = new HashSet<>();
                result.add(new Cursor(binaryCursor, right));
                result.add(new Cursor(binaryCursor, left));
                return result;
            } else {
                return Collections.singletonList(new Cursor(binaryCursor, right));
            }
             */
        } else if (p == right) {
            return Collections.singletonList(new Cursor(binaryCursor, left));
        } else if (p == left) {
            //return DataFlowGraph.previous(binaryCursor);
            return previousSourcesIn(binaryCursor, ProgramPoint.ENTRY);
        }
        throw new IllegalStateException();
    }

    public static Collection<Cursor> previousInAssignment(Cursor assignmentCursor, ProgramPoint p) {
        J.Assignment assignment = assignmentCursor.getValue();
        Expression a = assignment.getAssignment();

        if (p == ProgramPoint.EXIT) {
            return Collections.singletonList(new Cursor(assignmentCursor, a));
        } else if (p == a) {
            return DataFlowGraph.primitiveSources(assignmentCursor);
        }
        throw new IllegalStateException();
    }

    public static Collection<Cursor> previousInTerminalNode(Cursor parentCursor, ProgramPoint p) {
        if (p == ProgramPoint.EXIT) {
            return Collections.singletonList(parentCursor);
        }
        throw new IllegalStateException();
    }

    public static String print(Cursor c) {
        if (c.getValue() instanceof ProgramPoint) {
            ProgramPoint p = c.getValue();
            return p.printPP(c).replace("\n", " ").replaceAll("[ ]+", " ").trim();
        } else if (c.getValue() instanceof Collection) {
            return ((Collection<?>) c.getValue()).stream().map(e -> print(new Cursor(c, e))).collect(Collectors.joining("; "));
        } else {
            throw new IllegalStateException();
        }
    }
}
