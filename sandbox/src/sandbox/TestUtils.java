package sandbox;

import org.openrewrite.Cursor;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.dataflow2.DataFlowGraph;
import org.openrewrite.java.dataflow2.ProgramPoint;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 *  A visitor to find a program point within a given compilation unit with the given string representation.
 *  Used for specifying unit tests.
 */
public class TestUtils {

    /**
     * @return The first program point in given compilation unit whose print representation is equal to `ppToFind`.
     */
    public static Cursor findProgramPoint(J.CompilationUnit cu, String ppToFind) {
        TestUtils.Visitor visitor = new TestUtils.Visitor(ppToFind);
        visitor.visit(cu, null);
        return visitor.result;
    }

    public static JavaType.Variable findVariable(J.CompilationUnit cu, String variableToFind) {
        TestUtils.FindVariableVisitor visitor = new TestUtils.FindVariableVisitor(variableToFind);
        visitor.visit(cu, null);
        return visitor.result;
    }

    public static void assertPrevious(J.CompilationUnit cu, String pp, String... previous) {
        List<String> expected = Arrays.asList(previous);
        Cursor c = TestUtils.findProgramPoint(cu, pp);
        assertThat(c).withFailMessage("program point <" + pp + "> not found").isNotNull();
        Collection<Cursor> prevs = DataFlowGraph.primitiveSources(c);
        assertThat(c).withFailMessage("previous() returned null").isNotNull();
        List<String> actual = prevs.stream().map(prev -> print(prev)).collect(Collectors.toList());

        expected.sort(String.CASE_INSENSITIVE_ORDER);
        actual.sort(String.CASE_INSENSITIVE_ORDER);
        assertThat(actual)
                .withFailMessage("previous(" + pp + ")\nexpected: " + expected + "\n but was: " + actual)
                .isEqualTo(expected);
    }

    public static void assertLast(J.CompilationUnit cu, String pp, String... last) {
        List<String> expected = Arrays.asList(last);
        Cursor c = TestUtils.findProgramPoint(cu, pp);
        assertThat(c).withFailMessage("program point <" + pp + "> not found").isNotNull();
        Collection<Cursor> lasts = DataFlowGraph.last(c);
        assertThat(c).withFailMessage("last() returned null").isNotNull();
        List<String> actual = lasts.stream().map(l -> print(l)).collect(Collectors.toList());

        expected.sort(String.CASE_INSENSITIVE_ORDER);
        actual.sort(String.CASE_INSENSITIVE_ORDER);
        assertThat(actual)
                .withFailMessage("last(" + pp + ")\nexpected: " + expected + "\n but was: " + actual)
                .isEqualTo(expected);
    }

    public static String print(Cursor c) {
        ProgramPoint p = (ProgramPoint)c.getValue();
        return ((J) p).print(c).replace("\n", " ").replaceAll("[ ]+", " ").trim();
    }

    public static String print(ProgramPoint p, Cursor c) {
        return ((J) p).print(c).replace("\n", " ").replaceAll("[ ]+", " ").trim();
    }

    public static String print(ProgramPoint p) {
        return ((J) p).print().replace("\n", " ").replaceAll("[ ]+", " ").trim();
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

}
