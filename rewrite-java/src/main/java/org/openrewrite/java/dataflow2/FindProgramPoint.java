//package org.openrewrite.java.dataflow2;
//
//import org.openrewrite.Cursor;
//import org.openrewrite.java.JavaIsoVisitor;
//import org.openrewrite.java.tree.J;
//import org.openrewrite.java.tree.Statement;
//
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//
///**
// *  A visitor to find a program point within a given compilation unit with the given string representation.
// */
//public class FindProgramPoint extends JavaIsoVisitor {
//
//    final String ppToFind;
//    Cursor result;
//
//    public static Cursor findProgramPoint(J.CompilationUnit cu, String ppToFind) {
//        FindProgramPoint visitor = new FindProgramPoint(ppToFind);
//        visitor.visit(cu, null);
//        return visitor.result;
//    }
//
//    public static void assertPrevious(J.CompilationUnit cu, String pp, String... previous) {
//        List<String> expectedPrevs = Arrays.asList(previous);
//        Cursor c = FindProgramPoint.findProgramPoint(cu, pp);
//        assert(c != null);
//        Collection<Cursor> prevs = DataFlowGraph.previous(c);
//        assert(prevs != null);
//        List<String> actualPrevs = prevs.stream().map(prev -> print(prev)).collect(Collectors.toList());
//
//        expectedPrevs.sort(String.CASE_INSENSITIVE_ORDER);
//        actualPrevs.sort(String.CASE_INSENSITIVE_ORDER);
//        AssertionsForClassTypes.assertThat(actualPrevs).isEqualTo(expectedPrevs);
//    }
//
//    public static String print(Cursor c) {
//        ProgramPoint p = (ProgramPoint)c.getValue();
//        return ((J) p).print(c).replace("\n", " ").replaceAll("[ ]+", " ").trim();
//    }
//
//    public static String print(ProgramPoint p, Cursor c) {
//        return ((J) p).print(c).replace("\n", " ").replaceAll("[ ]+", " ").trim();
//    }
//
//    public static String print(ProgramPoint p) {
//        return ((J) p).print().replace("\n", " ").replaceAll("[ ]+", " ").trim();
//    }
//
//
//    public FindProgramPoint(String ppToFind) {
//        this.ppToFind = ppToFind;
//    }
//
//    @Override
//    public Statement visitStatement(Statement statement, Object o) {
//        super.visitStatement(statement, o);
//        if (result == null && ppToFind.equals(print(statement, getCursor()))) {
//            result = getCursor();
//        }
//        return statement;
//    }
//
//    @Override
//    public J.VariableDeclarations.NamedVariable visitVariable(J.VariableDeclarations.NamedVariable variable, Object o) {
//        super.visitVariable(variable, o);
//        if (result == null && ppToFind.equals(print(variable, getCursor()))) {
//            result = getCursor();
//        }
//        return variable;
//    }
//}
