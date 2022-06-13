package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.Incubating;
import org.openrewrite.internal.lang.NonNull;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.antlr.v4.analysis.LeftRecursiveRuleAnalyzer.ASSOC.right;
import static org.openrewrite.java.dataflow2.ProgramPoint.ENTRY;
import static org.openrewrite.java.dataflow2.ProgramPoint.EXIT;

@Incubating(since = "7.24.0")
public class DataFlowGraph {

    final J.CompilationUnit cu;

    public DataFlowGraph(J.CompilationUnit cu) {
        this.cu = cu;
    }

    /**
     * @param programPoint A cursor whose value is a program point.
     * @return The set of program points, possibly composite (i.e. containing other program points, such as
     * a while loop), preceding given program point in the dataflow graph.
     */
    public @NonNull Collection<Cursor> previous(Cursor programPoint) {
        return previousIn(programPoint, ENTRY);
    }

    public @NonNull Collection<Cursor> previousIn(Cursor parentCursor, ProgramPoint current) {
        while(!(parentCursor.getValue() instanceof J)) parentCursor = parentCursor.getParentOrThrow();

        J parent = parentCursor.getValue();
        switch (parent.getClass().getName().replaceAll("^org.openrewrite.java.tree.", "")) {
            case "J$MethodInvocation":
                return previousInMethodInvocation(parentCursor, current);
            case "J$NewClass":
                return previousInNewClass(parentCursor, current);
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
            case "J$Return":
                return previousInReturn(parentCursor, current);
            case "J$Literal":
            case "J$Identifier":
            case "J$Empty":
            case "J$Primitive": // not actually a program point
                return previousInTerminalNode(parentCursor, current);
            case "J$MethodDeclaration":
                return previousInMethoddeclaration(parentCursor, current);
            case "J$CompilationUnit":
            case "J$ClassDeclaration":
                return Collections.emptyList();
            default:
                throw new Error(parent.getClass().getName());
        }

    }

    @NonNull Collection<Cursor> previousInBlock(Cursor parentCursor, ProgramPoint p) {
        J.Block parent = parentCursor.getValue();
        List<Statement> stmts = parent.getStatements();
        if (p == EXIT) {
            if (stmts.size() > 0) {
                //return Collections.singletonList(new Cursor(parentCursor, stmts.get(stmts.size() - 1)));
                return previousIn(new Cursor(parentCursor, stmts.get(stmts.size() - 1)), EXIT);
            } else {
                return previous(parentCursor);
            }
        } else if(p == ENTRY) {
            return previousIn(parentCursor.getParent(), parent);
        } else {
            int index = stmts.indexOf(p);
            if (index > 0) {
                return previousIn(new Cursor(parentCursor, stmts.get(index - 1)), EXIT);
            } else if (index == 0) {
                return previous(parentCursor);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public @NonNull Collection<Cursor> previousInVariableDeclarations(Cursor parentCursor, ProgramPoint p) {
        J.VariableDeclarations parent = parentCursor.getValue();
        List<J.VariableDeclarations.NamedVariable> variables = parent.getVariables();
        if (p == EXIT) {
            if (variables.size() > 0) {
                return previousIn(new Cursor(parentCursor, variables.get(variables.size() - 1)), EXIT);
            } else {
                return previous(parentCursor);
            }
        } else if (p == ENTRY) {
            //return DataFlowGraph.previous(parentCursor);
            return previousIn(parentCursor.getParent(), parentCursor.getValue());
        } else if (p == parent.getTypeExpression()) {
            // p is not a program point
            return Collections.emptyList();
        } else {
            int index = parent.getVariables().indexOf(p);
            if (index > 0) {
                //return Collections.singletonList(new Cursor(parentCursor, variables.get(index - 1)));
                return previousIn(new Cursor(parentCursor, variables.get(index - 1)), EXIT);
            } else if (index == 0) {
                //return DataFlowGraph.previous(parentCursor);
                return previous(new Cursor(parentCursor.getParent(), parentCursor.getValue()));
            } else {
                throw new IllegalStateException();
            }
        }
    }

    enum ForLoopPosition {
        INIT, CONDITION, UPDATE
    }

    /**
     * @return The last program point(s) in the for-loop at given position, which might be
     * last of the previous position if given position is empty, or even the previous program point
     * of the for-loop if all preceding positions are empty.
     */
    @NonNull Collection<Cursor> lastInForLoop(Cursor forLoopCursor, ForLoopPosition position) {

        J.ForLoop forLoop = forLoopCursor.getValue();
        J.ForLoop.Control control = forLoop.getControl();

        List<Statement> init = control.getInit();
        List<Statement> update = control.getUpdate();

        if (position == ForLoopPosition.UPDATE) {
            if (update.size() > 0) {
                return Collections.singletonList(new Cursor(forLoopCursor, update.get(update.size() - 1)));
            } else {
                return lastInForLoop(forLoopCursor, ForLoopPosition.INIT);
            }
        }
        if (position == ForLoopPosition.INIT) {
            if (init.size() > 0) {
                return previousIn(new Cursor(forLoopCursor, init.get(init.size() - 1)), EXIT);
            } else {
                return previous(forLoopCursor);
            }
        }
        throw new IllegalStateException();
    }



    @NonNull Collection<Cursor> previousInMethoddeclaration(Cursor parentCursor, ProgramPoint p) {
        J.MethodDeclaration parent = parentCursor.getValue();

        return Collections.emptyList();
    }

    @NonNull Collection<Cursor> previousInMethodInvocation(Cursor parentCursor, ProgramPoint p) {

        J.MethodInvocation parent = parentCursor.getValue();
        Expression select = parent.getSelect();
        List<Expression> args = parent.getArguments();

        if (p == EXIT) {
            return Collections.singletonList(parentCursor);
        } else if (p == ENTRY) {
            if (args.size() > 0 && !(args.get(0) instanceof J.Empty)) {
                return previousIn(new Cursor(parentCursor, right), args.get(args.size() - 1));
            } else if(select != null) {
                return previousIn(new Cursor(parentCursor, select), EXIT);
            } else {
                return previousIn(parentCursor.getParent(), parentCursor.getValue());
            }
        } else if (p == parent.getSelect()) {
            return previousIn(parentCursor.getParent(), parentCursor.getValue());
        } else if(p == parent.getName()) {
            // Not actually a program point
            return previousIn(parentCursor.getParent(), parentCursor.getValue());
        } else {
            int index = args.indexOf(p);
            if (index > 0) {
                return Collections.singletonList(new Cursor(parentCursor, args.get(index - 1)));
            } else if (index == 0) {
                if (parent.getSelect() != null) {
                    return Collections.singletonList(new Cursor(parentCursor, parent.getSelect()));
                } else {
                    // implicit this
                    return previousIn(parentCursor, ENTRY);
                }
            } else {
                throw new IllegalStateException();
            }
        }
    }

    @NonNull Collection<Cursor> previousInNewClass(Cursor parentCursor, ProgramPoint p) {

        J.NewClass parent = parentCursor.getValue();
        List<Expression> args = parent.getArguments();

        if (p == EXIT) {
            return Collections.singletonList(parentCursor);
        } else if (p == ENTRY) {
            if (args.size() > 0 && !(args.get(0) instanceof J.Empty)) {
                return previousIn(new Cursor(parentCursor, right), args.get(args.size() - 1));
            } else {
                return previousIn(parentCursor.getParent(), parentCursor.getValue());
            }
        } else {
            int index = args.indexOf(p);
            if (index > 0) {
                return Collections.singletonList(new Cursor(parentCursor, args.get(index - 1)));
            } else if (index == 0) {
                return previousIn(parentCursor.getParent(), parentCursor.getValue());
            } else {
                throw new IllegalStateException();
            }
        }
    }

    @NonNull Collection<Cursor> previousInIf(Cursor ifCursor, ProgramPoint p) {

        J.If ifThenElse = ifCursor.getValue();
        J.ControlParentheses<Expression> cond = ifThenElse.getIfCondition();
        Statement thenPart = ifThenElse.getThenPart();
        J.If.@Nullable Else elsePart = ifThenElse.getElsePart();

        if (p == EXIT) {
            List<Cursor> result = new ArrayList<>();
            result.add(new Cursor(ifCursor, thenPart));
            if (elsePart == null) {
                Cursor c = new Cursor(ifCursor, cond);
                c.putMessage("ifThenElseBranch", "exit");
                return Collections.singletonList(c);
            } else {
                result.add(new Cursor(ifCursor, elsePart));
            }
            return result;
        } else if (p == ENTRY) {
            return previousIn(ifCursor.getParent(), ifThenElse);
        } else if (p == thenPart) {
            Cursor c = new Cursor(ifCursor, cond);
            c.putMessage("ifThenElseBranch", "then");
            return Collections.singletonList(c);
        } else if (p == elsePart) {
            Cursor c = new Cursor(ifCursor, cond);
            c.putMessage("ifThenElseBranch", "else");
            return Collections.singletonList(c);
        } else if (p == cond) {
            return Collections.singletonList(new Cursor(ifCursor, cond));
        }
        throw new IllegalStateException();
    }

    @NonNull Collection<Cursor> previousInIfElse(Cursor ifElseCursor, ProgramPoint p) {

        J.If.Else ifElse = ifElseCursor.getValue();
        Statement body = ifElse.getBody();

        if (p == EXIT) {
            return Collections.singletonList(new Cursor(ifElseCursor, body));
        } else if (p == ENTRY) {
            return previousIn(ifElseCursor.getParent(), ifElse);
        } else if (p == body) {
            return previous(ifElseCursor);
        }
        throw new IllegalStateException();
    }

    @NonNull Collection<Cursor> previousInWhileLoop(Cursor whileCursor, ProgramPoint p) {

        J.WhileLoop _while = whileCursor.getValue();
        J.ControlParentheses<Expression> cond = _while.getCondition();
        Statement body = _while.getBody();

        // while(cond: Expression) {
        //   body: Statement
        // }

        if (p == EXIT) {
            List<Cursor> result = new ArrayList<>();
            result.add(new Cursor(whileCursor, body));
            result.add(new Cursor(whileCursor, cond));
            return result;
        } else if (p == ENTRY) {
            return previousIn(whileCursor.getParent(), _while);
        } else if (p == body) {
            return Collections.singletonList(new Cursor(whileCursor, cond));
        } else if (p == cond) {
            List<Cursor> result = new ArrayList<>();
            result.add(new Cursor(whileCursor, body));
            result.addAll(previousIn(whileCursor.getParent(), _while));
            return result;
        }

        throw new IllegalStateException();
    }

    @NonNull Collection<Cursor> previousInForLoop(Cursor forLoopCursor, ProgramPoint p) {

        J.ForLoop forLoop = forLoopCursor.getValue();
        List<Statement> init = forLoop.getControl().getInit();
        Expression cond = forLoop.getControl().getCondition();
        Statement body = forLoop.getBody();
        List<Statement> update = forLoop.getControl().getUpdate();

        // init: List<Statement>
        // while(cond: Expression) {
        //   body: Statement
        //   update: List<Statement>
        // }

        if (p == EXIT) {
            List<Cursor> result = new ArrayList<>();
            result.addAll(lastInForLoop(forLoopCursor, ForLoopPosition.UPDATE));
            result.add(new Cursor(forLoopCursor, cond));
            return result;

        } else if (p == ENTRY) {
            return previousIn(forLoopCursor.getParent(), forLoop);

        } else if (p == body) {
            return Collections.singletonList(new Cursor(forLoopCursor, cond));

        } else if (p == cond) {
            List<Cursor> result = new ArrayList<>();
            result.addAll(lastInForLoop(forLoopCursor, ForLoopPosition.INIT));
            result.addAll(lastInForLoop(forLoopCursor, ForLoopPosition.UPDATE));
            return result;

        } else {
            int index;

            index = init.indexOf(p);
            if (index > 0) {
                return Collections.singletonList(new Cursor(forLoopCursor, init.get(index - 1)));
            } else if (index == 0) {
                return previous(forLoopCursor);
            }

            index = update.indexOf(p);
            if (index > 0) {
                return Collections.singletonList(new Cursor(forLoopCursor, update.get(index - 1)));
            } else if (index == 0) {
                return previousIn(new Cursor(forLoopCursor, body), EXIT);
            }

            throw new IllegalStateException();
        }
    }

    @NonNull Collection<Cursor> previousInForLoopControl(Cursor forLoopControlCursor, ProgramPoint p) {

        J.ForLoop.Control forLoopControl = forLoopControlCursor.getValue();
        return previousInForLoop(forLoopControlCursor.getParentOrThrow(), p);
    }

    public Collection<Cursor> previousInParentheses(Cursor parenthesesCursor, ProgramPoint p) {
        J.Parentheses<?> parentheses = parenthesesCursor.getValue();
        J tree = parentheses.getTree();

        if (p == EXIT) {
            return previousIn(new Cursor(parenthesesCursor, tree), EXIT);
        } else if(p == ENTRY) {
            return previousIn(parenthesesCursor.getParent(), parentheses);
        } else if (p == tree) {
            return previous(parenthesesCursor);
        }
        throw new IllegalStateException();
    }

    public Collection<Cursor> previousInControlParentheses(Cursor parenthesesCursor, ProgramPoint p) {
        J.ControlParentheses<?> parentheses = parenthesesCursor.getValue();
        J tree = parentheses.getTree();

        if (p == EXIT) {
            return previousIn(new Cursor(parenthesesCursor, tree), EXIT);
        } else if(p == ENTRY) {
            return previousIn(parenthesesCursor.getParent(), parentheses);
        } else if (p == tree) {
            return previous(parenthesesCursor);
        }
        throw new IllegalStateException();
    }

    public Collection<Cursor> previousInNamedVariable(Cursor namedVariableCursor, ProgramPoint p) {
        J.VariableDeclarations.NamedVariable namedVariable = namedVariableCursor.getValue();
        J.Identifier name = namedVariable.getName();
        Expression initializer = namedVariable.getInitializer();

        if (p == EXIT) {
            return Collections.singletonList(namedVariableCursor);
        } else if (p == ENTRY) {
            //return DataFlowGraph.previousIn(namedVariableCursor.getParentOrThrow(), namedVariableCursor.getValue());
            if(initializer != null) {
                return previousIn(new Cursor(namedVariableCursor, initializer), EXIT);
            } else {
                return previousIn(namedVariableCursor.getParent(), namedVariable);
            }
        } else if (p == name) {
            return Collections.emptyList();
        } else if (p == initializer) {
            return previousIn(namedVariableCursor.getParentOrThrow(), namedVariableCursor.getValue());
        }
        throw new IllegalStateException();
    }

    public Collection<Cursor> previousInUnary(Cursor unaryCursor, ProgramPoint p) {
        J.Unary unary = unaryCursor.getValue();
        Expression expr = unary.getExpression();

        if (p == ENTRY) {
            return Collections.singletonList(new Cursor(unaryCursor, expr));
        } else if (p == EXIT) {
            //return previousIn(new Cursor(unaryCursor, expr), EXIT);
            return Collections.singletonList(unaryCursor);
        } else if (p == unary.getExpression()) {
            return previousIn(unaryCursor.getParent(), unary);
        }
        throw new IllegalStateException();
    }

    public Collection<Cursor> previousInBinary(Cursor binaryCursor, ProgramPoint p) {
        J.Binary binary = binaryCursor.getValue();

        Expression left = binary.getLeft();
        Expression right = binary.getRight();
        J.Binary.Type op = binary.getOperator();

        // ENTRY -> left
        // left -> right
        // right -> binary
        // binary -> EXIT

        if (p == ENTRY) {
            return Collections.singletonList(new Cursor(binaryCursor, right));
        } else if (p == EXIT) {
            return Collections.singletonList(binaryCursor);
        } else if (p == right) {
            return Collections.singletonList(new Cursor(binaryCursor, left));
        } else if (p == left) {
            return previousIn(binaryCursor.getParent(), binaryCursor.getValue());
        }
        throw new IllegalStateException();
    }

    public Collection<Cursor> previousInAssignment(Cursor assignmentCursor, ProgramPoint p) {
        J.Assignment assignment = assignmentCursor.getValue();
        Expression a = assignment.getAssignment();
        Expression v = assignment.getVariable();

        if (p == EXIT) {
            return Collections.singletonList(assignmentCursor);
        } else if (p == ENTRY) {
            return Collections.singletonList(new Cursor(assignmentCursor, a));
        } else if (p == a) {
            return previousIn(assignmentCursor.getParent(), assignmentCursor.getValue());
        } else if (p == v) {
            // Not actually an expression
            return Collections.singletonList(new Cursor(assignmentCursor, a));
        }
        throw new IllegalStateException();
    }

    public Collection<Cursor> previousInReturn(Cursor parentCursor, ProgramPoint p) {
        J.Return _return = parentCursor.getValue();
        @Nullable Expression expr = _return.getExpression();
        if (p == EXIT) {
            if(expr == null) {
                return Collections.singletonList(parentCursor);
            } else {
                return Collections.singletonList(new Cursor(parentCursor, expr));
            }
        } else if(p == ENTRY) {
            return previousIn(parentCursor.getParent(), parentCursor.getValue());
        } else if(p == expr) {
            return previousIn(parentCursor.getParent(), parentCursor.getValue());
        }
        throw new IllegalStateException();
    }

    public Collection<Cursor> previousInTerminalNode(Cursor parentCursor, ProgramPoint p) {
        if (p == EXIT) {
            return Collections.singletonList(parentCursor);
        } else if(p == ENTRY) {
            return previousIn(parentCursor.getParent(), parentCursor.getValue());
        }
        throw new IllegalStateException();
    }

    public String print(Cursor c) {
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
