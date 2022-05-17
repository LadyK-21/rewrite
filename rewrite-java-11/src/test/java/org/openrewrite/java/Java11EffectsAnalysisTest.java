package org.openrewrite.java;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;
import org.openrewrite.test.RewriteTest;

import java.util.List;

public class Java11EffectsAnalysisTest implements RewriteTest {
    @Test
    void test2() {
        rewriteRun(
                spec -> spec.beforeRecipe(sourceFiles -> {
                    J.CompilationUnit cu = (J.CompilationUnit) sourceFiles.get(0);
                    J.Block classBody = cu.getClasses().get(0).getBody();
                    J.MethodDeclaration methodDecl = (J.MethodDeclaration) classBody.getStatements().get(0);
                    List<Statement> methodStatements = methodDecl.getBody().getStatements();

                    J.VariableDeclarations.NamedVariable xVar = ((J.VariableDeclarations) methodStatements.get(0)).getVariables().get(0);
                    J.VariableDeclarations.NamedVariable yVar = ((J.VariableDeclarations) methodStatements.get(0)).getVariables().get(1);

                    JavaType.Variable x = xVar.getVariableType();
                    JavaType.Variable y = yVar.getVariableType();

                    Statement stmt1 = methodStatements.get(1);
                    System.out.println("stmt1 = " + stmt1);
                    System.out.println("stmt1 reads x = " + stmt1.reads(x));
                    System.out.println("stmt1 writes x = " + stmt1.writes(x));
                    System.out.println("stmt1 reads y = " + stmt1.reads(y));
                    System.out.println("stmt1 writes y = " + stmt1.writes(y));

                    Statement stmt2 = methodStatements.get(2);
                    System.out.println("stmt2 = " + stmt2);
                    System.out.println("stmt2 reads x = " + stmt2.reads(x));
                    System.out.println("stmt2 writes x = " + stmt2.writes(x));
                    System.out.println("stmt2 reads y = " + stmt2.reads(y));
                    System.out.println("stmt2 writes y = " + stmt2.writes(y));
                }),
                java("class C { void m() { " +
                        "int x = 0, y = x;" +
                        " x = 1; x = y;" +
                        " }}"
                )
        );
    }

    @Test
    void test() {
        rewriteRun(
                spec -> spec.recipe(RewriteTest.toRecipe(() -> new JavaIsoVisitor<>() {
                    @Override
                    public J.Assignment visitAssignment(J.Assignment assign, ExecutionContext ctx) {
                        J.Assignment a = super.visitAssignment(assign, ctx);
                        for (JavaType.Variable variable : getCursor().firstEnclosingOrThrow(J.CompilationUnit.class)
                                .getTypesInUse().getVariables()) {
                            if (a.reads(variable)) {
                                a = a.withMarkers(a.getMarkers().searchResult("reads " + variable.getName()));
                            }
                        }
                        return a;
                    }
                })),
                java(
                        "" +
                                "class C { int m(int p) { " +
                                "  int x = 0, y = x;" +
                                "  x = 1;" +
                                "  x = m(y);" +
                                "  x = y;" +
                                "  return p;" +
                                "}}",
                        "" +
                                "class C { int m(int p) { " +
                                "  int x = 0, y = x;" +
                                "  x = 1;" +
                                "  /*~~(reads y)~~>*/x = m(y);" +
                                "  /*~~(reads y)~~>*/x = y;" +
                                "  return p;" +
                                "}}"
                )
        );
    }

    @Test
    public void ForToForeach() {
        @Language("java") String source =
                "import java.util.ArrayList;\n" +
                        "class C { void m() {\n" +
                        "int x = 0, y = 0;\n" +
                        "List<String> list = new ArrayList<>(10);\n" +
                        "for(int i = 0; i < 10; i++) {\n" +
                        "   x = 1;\n" +
                        "   System.out.println(list.get(i));\n" +
                        "   x = y;\n" +
                        " }}}\n";

        Java11Parser parser = new Java11Parser.Builder().build();
        ExecutionContext ctx = new InMemoryExecutionContext();
        List<J.CompilationUnit> cus = parser.parse(ctx, source);
        J.CompilationUnit cu = cus.get(0);
        J.Block classBody = cu.getClasses().get(0).getBody();
        J.MethodDeclaration methodDecl = (J.MethodDeclaration) classBody.getStatements().get(0);
        List<Statement> methodStatements = methodDecl.getBody().getStatements();

        J.VariableDeclarations.NamedVariable xVar = ((J.VariableDeclarations) methodStatements.get(0)).getVariables().get(0);
        J.VariableDeclarations.NamedVariable yVar = ((J.VariableDeclarations) methodStatements.get(0)).getVariables().get(1);

        JavaType.Variable x = xVar.getVariableType();
        JavaType.Variable y = yVar.getVariableType();

        Statement stmt1 = methodStatements.get(1);
        System.out.println("stmt1 = " + stmt1);
        System.out.println("stmt1 reads x = " + stmt1.reads(x));
        System.out.println("stmt1 writes x = " + stmt1.writes(x));
        System.out.println("stmt1 reads y = " + stmt1.reads(y));
        System.out.println("stmt1 writes y = " + stmt1.writes(y));

        Statement stmt2 = methodStatements.get(2);
        System.out.println("stmt2 = " + stmt2);
        System.out.println("stmt2 reads x = " + stmt2.reads(x));
        System.out.println("stmt2 writes x = " + stmt2.writes(x));
        System.out.println("stmt2 reads y = " + stmt2.reads(y));
        System.out.println("stmt2 writes y = " + stmt2.writes(y));

        cu.print();
    }
}
