package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;

import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    /**
     * @return The first program point in given compilation unit whose print representation is equal to `ppToFind`.
     */
    public static Cursor findProgramPoint(J.CompilationUnit cu, String ppToFind) {
        Visitor visitor = new Visitor(ppToFind);
        visitor.visit(cu, null);
        return visitor.result;
    }

    public static JavaType.Variable findVariable(J.CompilationUnit cu, String variableToFind) {
        FindVariableVisitor visitor = new FindVariableVisitor(variableToFind);
        visitor.visit(cu, null);
        return visitor.result;
    }

    static class Visitor extends JavaIsoVisitor {

        final String ppToFind;
        Cursor result;


        public Visitor(String ppToFind) {
            this.ppToFind = ppToFind;
        }

        @Override
        public Statement visitStatement(Statement statement, Object o) {
            super.visitStatement(statement, o);
            if (result == null && ppToFind.equals(print(statement, getCursor()))) {
                result = getCursor();
            }
            return statement;
        }

        @Override
        public J.VariableDeclarations.NamedVariable visitVariable(J.VariableDeclarations.NamedVariable variable, Object o) {
            super.visitVariable(variable, o);
            if (result == null && ppToFind.equals(print(variable, getCursor()))) {
                result = getCursor();
            }
            return variable;
        }

        @Override
        public Expression visitExpression(Expression expression, Object o) {
            super.visitExpression(expression, o);
            if (result == null && ppToFind.equals(print(expression, getCursor()))) {
                result = getCursor();
            }
            return expression;
        }
    }

    static class FindVariableVisitor extends JavaIsoVisitor {

        final String variableToFind;
        JavaType.Variable result;


        public FindVariableVisitor(String variableToFind) {
            this.variableToFind = variableToFind;
        }

        @Override
        public J.VariableDeclarations.NamedVariable visitVariable(J.VariableDeclarations.NamedVariable variable, Object o) {
            super.visitVariable(variable, o);
            if (result == null && variableToFind.equals(variable.getName().getSimpleName())) {
                result = variable.getVariableType();
            }
            return variable;
        }
    }

    public static String print(Cursor c) {
        if(c.getValue() instanceof ProgramPoint) {
            ProgramPoint p = c.getValue();
            return print(p,c);
        } else if(c.getValue() instanceof List) {
            List<ProgramPoint> l = c.getValue();
            return "[" + l.stream().map(e -> print(e, c)).collect(Collectors.joining(", ")) + "]";
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public static String print(ProgramPoint p, Cursor c) {
        return ((J) p).print(c).replace("\n", " ").replaceAll("[ ]+", " ").trim();
    }

    public static String print(ProgramPoint p) {
        return ((J) p).print().replace("\n", " ").replaceAll("[ ]+", " ").trim();
    }
}
