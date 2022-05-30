package sandbox;

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.Java11Parser;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.dataflow2.DataFlowGraph;
import org.openrewrite.java.dataflow2.IsNullAnalysis;
import org.openrewrite.java.dataflow2.ProgramPoint;
import org.openrewrite.java.dataflow2.Ternary;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class Main {

    public static boolean debug = false;

    private static J.CompilationUnit parse(String src) {
        Java11Parser parser = new Java11Parser.Builder().build();
        ExecutionContext ctx = new InMemoryExecutionContext();
        List<J.CompilationUnit> cus = parser.parse(ctx, src);
        J.CompilationUnit cu = cus.get(0);
        return cu;
    }

    public static void main(String[] args)
    {
        testAPI();
        // testVariableDeclarations();
        // testForLoop();
    }

    public static void testAPI() {
        testIsSNull("String s = null;");
        testIsSNull("String s = \"abc\";");
        testIsSNull("String q = null; String s = q;");
        testIsSNull("String q = \"abc\"; String s = q;");
        testIsSNull("String s = \"a\" + \"b\";");
        testIsSNull("String s = u;");
        testIsSNull("String s = \"a\".toUpperCase();");
        testIsSNull("String s = \"a\".unknownMethod();");
        testIsSNull("String s; if(c) { s = null; } else { s = null; }");
        testIsSNull("String s; if(c) { s = null; } else { s = \"b\"; }");
        testIsSNull("String s; if(c) { s = \"a\"; } else { s = null; }");
        testIsSNull("String s; if(c) { s = \"a\"; } else { s = \"b\"; }");
    }

    public static void testIsSNull(String fragment) {
        String source =
                "class C {\n" +
                        "    void a() {} \n" +
                        "    void b() {} \n" +
                        "    void m(String u, String v) { \n" +
                        "        a(); \n" +
                        "        __FRAGMENT__ \n" +
                        "        b(u,v); \n" +
                        "    }\n" +
                        "}\n" +
                        "" ;

        source = source.replace("__FRAGMENT__", fragment);

        J.CompilationUnit cu = parse(source);

        //new PrintProgramPointsVisitor().visit(cu, null);

        String pp1 = "b(u,v)";
        Cursor c1 = TestUtils.findProgramPoint(cu, pp1);
        assertThat(c1).withFailMessage("program point <" + pp1 + "> not found").isNotNull();

        String pp2 = "s";
        JavaType.Variable v = TestUtils.findVariable(cu, pp2);
        assertThat(v).isNotNull();

        Ternary state = new IsNullAnalysis().inputState(c1, v);
        System.out.println(fragment + "\n    Is s null when entering point 'b()' ? " + state);
    }

    public static void testVariableDeclarations()
    {
        String source =
                    "class C {\n" +
                    "    void a() {} \n" +
                    "    void b() {} \n" +
                    "    int w;\n" +
                    "    void m() {\n" +
                    "        a();\n" +
                    "        int i = u + v, j = w;\n" +
                    "        b();\n" +
                    "    }\n" +
                    "}\n" +
                    "";

        J.CompilationUnit cu = parse(source);

        // new MyVisitor().visit(cu, null);

        TestUtils.assertLast(cu,"a()","a()");
        TestUtils.assertLast(cu,"int i = u + v, j = w","j = w");
        TestUtils.assertLast(cu,"i = u + v","i = u + v");
        TestUtils.assertLast(cu,"u + v","u + v");
        TestUtils.assertLast(cu,"u","u");
        TestUtils.assertLast(cu,"v","v");

        TestUtils.assertPrevious(cu,"b()","j = w");
        //FindProgramPoint.assertPrevious(cu,"j = w", "w");
        //FindProgramPoint.assertPrevious(cu,"w", "i = u + v");
        TestUtils.assertPrevious(cu,"i = u + v", "u + v");
        TestUtils.assertPrevious(cu,"u + v", "v");
        TestUtils.assertPrevious(cu,"v", "u");
        TestUtils.assertPrevious(cu,"u", "a()");
        TestUtils.assertPrevious(cu,"a()");

    }


    public static void testForLoop()
    {
        String source =
            "class C { \n" +
            "    void a() {} \n" +
            "    void b() {} \n" +
            "    void f() {} \n" +
            "    void g() {} \n" +
            "    void h() {} \n" +
            "    void method() { \n" +
            "       a(); \n" +
            "       for(int i=0, j=1; 2<3; m++, n++) { \n" +
            "           f(); g(); h(); \n" +
            "       } \n" +
            "       b(); \n" +
            "    } \n" +
            "} \n" +
            "" ;

        // m(x,y)[i] = 1
        // m(x,y).f = 1
        // z = m(x,y).f

        J.CompilationUnit cu = parse(source);

        new PrintProgramPointsVisitor().visit(cu, null);

        TestUtils.assertPrevious(cu,"a()");
        TestUtils.assertPrevious(cu,"int i=0, j=1", "a()");
        TestUtils.assertPrevious(cu,"i=0", "0");
        TestUtils.assertPrevious(cu,"0", "a()");
        TestUtils.assertPrevious(cu,"j=1", "1");
        TestUtils.assertPrevious(cu,"1", "i=0");
        TestUtils.assertPrevious(cu,"n++", "n");
        TestUtils.assertPrevious(cu,"n", "m++");
        TestUtils.assertPrevious(cu,"m++", "m");
        TestUtils.assertPrevious(cu,"m", "h()");
        TestUtils.assertPrevious(cu,"h()", "g()");
        TestUtils.assertPrevious(cu,"g()", "f()");
        TestUtils.assertPrevious(cu,"f()", "2<3");
        TestUtils.assertPrevious(cu,"{ f(); g(); h(); }", "2<3");
        TestUtils.assertPrevious(cu,"2<3", "3");
        TestUtils.assertPrevious(cu,"3", "2");
        TestUtils.assertPrevious(cu,"2", "j=1", "n++");
//        FindProgramPoint.assertPrevious(cu,"b()",
//                "for(int i=0, j=1; i<10 && j<10; i++, j++) { f(); g(); h(); }");

    }

    public static void main2()
    {
        Java11Parser parser = new Java11Parser.Builder().build();
        String source =
            "class C {\n" +
            "   void m() {\n" +
            "       a(); int x = 1 + 2; b();" +
//            "       int x = 0, y = 1;\n" +
//            "       for(int i=0, j=2*i+1; i<x; i++, j++) {\n" +
//            "           f(x); g(x); h(x);\n" +
//            "       }\n" +
//            "       a();" +
//            "       if(x == y+1) { u(x); } else { v(x); }\n" +
//            "       while(x == 0) { w(x); }\n" +
//            "       o.m(p,q,r);\n" +
//            "       b();\n" +
//            "       o.m(1+x++, 2+(y++ *3));\n" +
//            "       x = 1+(2*y);\n" +
            "   }\n" +
            "}\n"
            ;

        J.CompilationUnit cu = parse(source);

        // new MyVisitor().visit(cu, null);

        /*
        J.Block classBody = cu.getClasses().get(0).getBody();
        J.MethodDeclaration methodDecl = (J.MethodDeclaration) classBody.getStatements().get(0);
        List<Statement> methodStatements = methodDecl.getBody().getStatements();
        Statement stmt1 = methodStatements.get(1);
        Statement stmt2 = methodStatements.get(2);

        J.VariableDeclarations.NamedVariable xVar = ((J.VariableDeclarations)methodStatements.get(0)).getVariables().get(0);
        J.VariableDeclarations.NamedVariable yVar = ((J.VariableDeclarations)methodStatements.get(0)).getVariables().get(1);

        JavaType.Variable x = xVar.getVariableType();
        JavaType.Variable y = yVar.getVariableType();

        System.out.println("stmt1 = " + stmt1);
        System.out.println("stmt1 reads x = " + stmt1.reads(x));
        System.out.println("stmt1 writes x = " + stmt1.writes(x));
        System.out.println("stmt1 reads y = " + stmt1.reads(y));
        System.out.println("stmt1 writes y = " + stmt1.writes(y));

        System.out.println("stmt2 = " + stmt2);
        System.out.println("stmt2 reads x = " + stmt2.reads(x));
        System.out.println("stmt2 writes x = " + stmt2.writes(x));
        System.out.println("stmt2 reads y = " + stmt2.reads(y));
        System.out.println("stmt2 writes y = " + stmt2.writes(y));

        cu.print();
         */
    }

}

class PrintProgramPointsVisitor extends JavaIsoVisitor {
    public static String print(Cursor c)
    {
        if(c.getValue() instanceof ProgramPoint) {
            ProgramPoint p = (ProgramPoint) c.getValue();
            return p.printPP(c).replace("\n", " ").replaceAll("[ ]+", " ").trim();
        } else if(c.getValue() instanceof Collection) {
            return (String) ((Collection)c.getValue()).stream().map(e -> print(new Cursor(c, e))).collect(Collectors.joining("; "));
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
        Collection<Cursor> pp = DataFlowGraph.primitiveSources(getCursor());
        if (pp == null) {
            System.out.println("   (prevs = null)");
            DataFlowGraph.primitiveSources(getCursor());
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
            Collection<Cursor> pp = DataFlowGraph.primitiveSources(getCursor());
            if (pp == null) {
                System.out.println("   (prevs = null)");
                DataFlowGraph.primitiveSources(getCursor());
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
        Collection<Cursor> pp = DataFlowGraph.primitiveSources(getCursor());
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

