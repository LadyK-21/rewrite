package sandbox;

import org.openrewrite.Cursor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.dataflow2.DataFlowGraph;
import org.openrewrite.java.dataflow2.ProgramPoint;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.openrewrite.java.dataflow2.ProgramPoint.ENTRY;

class PrintProgramPointsVisitor extends JavaIsoVisitor {

    private final DataFlowGraph dfg;

    public PrintProgramPointsVisitor(DataFlowGraph dfg) {
        this.dfg = dfg;
    }

    public static String print(Cursor c) {
        if (c.getValue() instanceof ProgramPoint) {
            ProgramPoint p = (ProgramPoint) c.getValue();
            return p.printPP(c).replace("\n", " ").replaceAll("[ ]+", " ").trim();
        } else if (c.getValue() instanceof Collection) {
            return (String) ((Collection) c.getValue()).stream().map(e -> print(new Cursor(c, e))).collect(Collectors.joining("; "));
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public Statement visitStatement(Statement statement, Object o) {
        System.out.println("statement = <" + print(getCursor()) + "> " + statement.getClass().getSimpleName());
        if (print(getCursor()).equals("0")) {
            System.out.println();
        }
        Collection<Cursor> pp = dfg.previousIn(getCursor(), ENTRY);
        if (pp == null) {
            System.out.println("   (prevs = null)");
            //dfg.previous(getCursor());
        } else {
            for (Cursor p : pp) {
                System.out.println("   prev = " + print(p));
            }
        }
        return super.visitStatement(statement, o);
    }

    @Override
    public Expression visitExpression(Expression expr, Object o) {
        if (expr instanceof J.Primitive) {
            System.out.println("skipping J.Primitive");
        } else {
            System.out.println("expression = <" + print(getCursor()) + "> " + expr.getClass().getSimpleName());
            if (expr instanceof J.Binary) {
                J.Binary b = (J.Binary) expr;
                System.out.println("          " +
                        print(new Cursor(getCursor(), b.getLeft())) +
                        "  " + b.getOperator() + "  " +
                        print(new Cursor(getCursor(), b.getRight()))
                );
            }
            if (print(getCursor()).equals("xxx")) {
                Main.debug = true;
            }
            Collection<Cursor> pp = dfg.previousIn(getCursor(), ENTRY);
            if (pp == null) {
                System.out.println("   (prevs = null)");
                //dfg.previous(getCursor());
            } else {
                for (Cursor p : pp) {
                    System.out.println("   prev = " + print(p));
                }
            }
        }
        return super.visitExpression(expr, o);
    }

    @Override
    public J.VariableDeclarations.NamedVariable visitVariable(J.VariableDeclarations.NamedVariable variable, Object o) {
        System.out.println("variable = <" + print(getCursor()) + "> " + variable.getClass().getSimpleName());
        if (print(getCursor()).equals("0")) {
            System.out.println();
        }
        Collection<Cursor> pp = dfg.previousIn(getCursor(), ENTRY);
        if (pp == null) {
            System.out.println("   (null)");
        } else {
            for (Cursor p : pp) {
                System.out.println("   prev = " + print(p));
            }
        }
        return super.visitVariable(variable, o);
        //return variable.withInitializer(visitExpression(variable.getInitializer(), null));
    }
}
