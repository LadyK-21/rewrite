package sandbox;

import org.openrewrite.java.dataflow2.DataFlowGraph;
import org.openrewrite.java.dataflow2.ProgramPoint;
import org.openrewrite.java.dataflow2.Utils;
import org.openrewrite.java.tree.J;

import static org.openrewrite.java.dataflow2.ModalBoolean.False;
import static org.openrewrite.java.dataflow2.ProgramPoint.ENTRY;
import static org.openrewrite.java.dataflow2.ProgramPoint.EXIT;
import static sandbox.TestUtils.parse;

public class TestPrevious {
    public static void test()
    {
        testParentheses();
        testWhileLoop();
        testNonLocalExits();
        testMethodInvocation();
        testIfThen();
        testIfThenElse();
        testVariableDeclarations();
        testForLoop();
    }


    public static void testPrevious(String fragment, String pp, ProgramPoint entryOrExit, String... previous) {
        String source =
                "class C {\n" +
                        "    void a() {} \n" +
                        "    void b() {} \n" +
                        "    void m(String u, String v) { \n" +
                        "        a(); \n" +
                        "        __FRAGMENT__ \n" +
                        "        b(); \n" +
                        "    }\n" +
                        "}\n" +
                        "" ;

        source = source.replace("__FRAGMENT__", fragment);

        J.CompilationUnit cu = parse(source);

        TestUtils.assertPrevious(cu,pp, entryOrExit, previous);
    }

    public static void testParentheses()
    {
        String source =
                "class C {\n" +
                        "    void a() {} \n" +
                        "    void b() {} \n" +
                        "    void m() {\n" +
                        "        a();\n" +
                        "        int i = (u + v);\n" +
                        "        b();\n" +
                        "    }\n" +
                        "}\n" +
                        "";

        J.CompilationUnit cu = parse(source);
        DataFlowGraph dfg = new DataFlowGraph(cu);
        //new PrintProgramPointsVisitor(dfg).visit(cu, null);

        TestUtils.assertPrevious(cu,"b()", ENTRY, "i = (u + v)");
        TestUtils.assertPrevious(cu,"i = (u + v)", EXIT, "i = (u + v)");
        TestUtils.assertPrevious(cu,"i = (u + v)", ENTRY, "(u + v)");
        TestUtils.assertPrevious(cu,"(u + v)", EXIT,"(u + v)");
        TestUtils.assertPrevious(cu,"(u + v)", ENTRY,"u + v");
        TestUtils.assertPrevious(cu,"u + v", EXIT, "u + v");
        TestUtils.assertPrevious(cu,"u + v", ENTRY, "v");
        TestUtils.assertPrevious(cu,"v", EXIT, "v");
        TestUtils.assertPrevious(cu,"v", ENTRY, "u");
        TestUtils.assertPrevious(cu,"u", EXIT, "u");
        TestUtils.assertPrevious(cu,"u", ENTRY, "a()");

    }

    public static void testVariableDeclarations()
    {
        String source =
                "class C {\n" +
                        "    void a() {} \n" +
                        "    void b() {} \n" +
                        "    void m() {\n" +
                        "        a();\n" +
                        "        int i = u + v, j = w;\n" +
                        "        b();\n" +
                        "    }\n" +
                        "}\n" +
                        "";

        J.CompilationUnit cu = parse(source);
        DataFlowGraph dfg = new DataFlowGraph(cu);
        //new PrintProgramPointsVisitor(dfg).visit(cu, null);

        TestUtils.assertPrevious(cu,"b()", ENTRY, "j = w");
        TestUtils.assertPrevious(cu,"j = w", EXIT, "j = w");
        TestUtils.assertPrevious(cu,"j = w", ENTRY, "w");
        TestUtils.assertPrevious(cu,"w", EXIT,"w");
        TestUtils.assertPrevious(cu,"w", ENTRY,"i = u + v");
        TestUtils.assertPrevious(cu,"i = u + v", EXIT, "i = u + v");
        TestUtils.assertPrevious(cu,"i = u + v", ENTRY, "u + v");
        TestUtils.assertPrevious(cu,"u + v", EXIT, "u + v");
        TestUtils.assertPrevious(cu,"u + v", ENTRY, "v");
        TestUtils.assertPrevious(cu,"v", EXIT, "v");
        TestUtils.assertPrevious(cu,"v", ENTRY, "u");
        TestUtils.assertPrevious(cu,"u", EXIT, "u");
        TestUtils.assertPrevious(cu,"u", ENTRY, "a()");

    }

    public static void testIfThen() {
        String source =
                "class C {\n" +
                "    void m() {\n" +
                "        a();\n" +
                "        if(s == null) f();\n" +
                "        b();\n" +
                "    }\n" +
                "}\n" +
                "";

        J.CompilationUnit cu = parse(source);
        DataFlowGraph dfg = new DataFlowGraph(cu);

        TestUtils.assertPrevious(cu,"b()", ENTRY, "f()", "(s == null)");
        TestUtils.assertPrevious(cu,"f()", ENTRY, "(s == null)");
        TestUtils.assertPrevious(cu,"s", ENTRY, "a()");
    }

    public static void testIfThenElse() {
        String source =
                "class C {\n" +
                        "    void m() {\n" +
                        "        a();\n" +
                        "        if(s == null) f(); else g(); \n" +
                        "        b();\n" +
                        "    }\n" +
                        "}\n" +
                        "";

        J.CompilationUnit cu = parse(source);
        DataFlowGraph dfg = new DataFlowGraph(cu);

        TestUtils.assertPrevious(cu,"b()", EXIT, "b()");
        TestUtils.assertPrevious(cu,"b()", ENTRY, "f()", "else g();");
        TestUtils.assertPrevious(cu,"f()", EXIT, "f()");
        TestUtils.assertPrevious(cu,"f()", ENTRY, "(s == null)");
        TestUtils.assertPrevious(cu,"else g();", EXIT, "g()");
        TestUtils.assertPrevious(cu,"else g();", ENTRY, "(s == null)");
        TestUtils.assertPrevious(cu,"g()", EXIT, "g()");
        TestUtils.assertPrevious(cu,"g()", ENTRY, "(s == null)");
        TestUtils.assertPrevious(cu,"s", EXIT, "s");
        TestUtils.assertPrevious(cu,"s", ENTRY, "a()");
    }

    public static void testMethodInvocation()
    {
        String source =
                "class C {\n" +
                        "    void m() {\n" +
                        "        a();\n" +
                        "        u.method(v,w);\n" +
                        "        b();\n" +
                        "    }\n" +
                        "}\n" +
                        "";

        J.CompilationUnit cu = parse(source);
        DataFlowGraph dfg = new DataFlowGraph(cu);
        //new PrintProgramPointsVisitor(dfg).visit(cu, null);

        TestUtils.assertPrevious(cu,"b()", ENTRY, "u.method(v,w)");
        TestUtils.assertPrevious(cu,"u.method(v,w)", EXIT, "u.method(v,w)");
        TestUtils.assertPrevious(cu,"u.method(v,w)", ENTRY, "w");
        TestUtils.assertPrevious(cu,"w", EXIT,"w");
        TestUtils.assertPrevious(cu,"w", ENTRY,"v");
        TestUtils.assertPrevious(cu,"v", EXIT,"v");
        TestUtils.assertPrevious(cu,"v", ENTRY,"u");
        TestUtils.assertPrevious(cu,"u", EXIT,"u");
        TestUtils.assertPrevious(cu,"u", ENTRY,"a()");
    }

    public static void testNonLocalExits()
    {
        String source =
                        "import java.lang.Exception; \n" +
                        "import java.io.IOException; \n" +
                        "class C {\n" +
                        "    void a() { \n" +
                        "    void b() { \n" +
                        "    void c() { \n" +
                        "    boolean x() { \n" +
                        "    boolean y() { \n" +
                        "    String m() { \n" +
                        "        a(); \n" +
                        "        if(x()) return \"a\"; \n" +
                        "        b(); \n" +
                        "        if(y()) throw new Exception(\"1\"); \n" +
                        "        try { \n" +
                        "            throw new Exception(\"2\"); \n" +
                        "        } catch(Exception e2) { \n" +
                        "           catch2(); \n" +
                        "        } \n" +
                        "        try { \n" +
                        "            throw new IOException(\"3\"); \n" +
                        "        } catch(Exception e3) { \n" +
                        "           catch3(); \n" +
                        "        } \n" +
                        "        try { \n" +
                        "            throw new Exception(\"4\"); \n" +
                        "        } catch(IOException e4) { \n" +
                        "           catch4(); \n" +
                        "        } \n" +
                        // also add loops with break and continue
                        "         \n" +
                        "         \n" +
                        "         \n" +
                        "         \n" +
                        "         \n" +
                        "         \n" +
                        "        c(); \n" +
                        // implicit return
                        "    } \n" +
                        "} \n" +
                        "";

        J.CompilationUnit cu = parse(source);
        DataFlowGraph dfg = new DataFlowGraph(cu);
        //new PrintProgramPointsVisitor(dfg).visit(cu, null);

        J.MethodDeclaration m = (J.MethodDeclaration) cu.getClasses().get(0).getBody().getStatements().get(0);
        System.out.println(Utils.print(m));

        // TODO <return "a"> is actually printed as <return"a">
        TestUtils.assertPrevious(cu, Utils.print(m), EXIT,
                "return\"a\"", "throw new Exception(\"1\")", "throw new Exception(\"4\")", "c()");

        TestUtils.assertPrevious(cu, "catch2()", ENTRY, "(Exception e2)");
        TestUtils.assertPrevious(cu, "catch2()", EXIT,"catch2()");
        TestUtils.assertPrevious(cu, "(Exception e2)", ENTRY, "Exception e2");
        TestUtils.assertPrevious(cu, "(Exception e2)", EXIT,"(Exception e2)");
        TestUtils.assertPrevious(cu, "Exception e2", ENTRY, "throw new Exception(\"2\")");
        TestUtils.assertPrevious(cu, "Exception e2", EXIT,"e2");
        TestUtils.assertPrevious(cu, "e2", ENTRY, "throw new Exception(\"2\")");
        TestUtils.assertPrevious(cu, "e2", EXIT,"e2");

        TestUtils.assertPrevious(cu, "catch3()", ENTRY, "(Exception e3)");
        TestUtils.assertPrevious(cu, "catch4()", ENTRY, "(IOException e4)");

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

        // init;
        // if(cond) {
        //     body;
        //     update;
        // }

        J.CompilationUnit cu = parse(source);
        DataFlowGraph dfg = new DataFlowGraph(cu);
        //new PrintProgramPointsVisitor(dfg).visit(cu, null);

        TestUtils.assertPrevious(cu,"b()", ENTRY, "2<3", "n++");

        TestUtils.assertPrevious(cu,"n++", ENTRY,"n");
        TestUtils.assertPrevious(cu,"n++", EXIT,"n++");
        TestUtils.assertPrevious(cu,"n", ENTRY, "m++");
        TestUtils.assertPrevious(cu,"n", EXIT, "n");
        TestUtils.assertPrevious(cu,"m++", ENTRY,"m");
        TestUtils.assertPrevious(cu,"m++", EXIT,"m++");
        TestUtils.assertPrevious(cu,"m", ENTRY,"h()");
        TestUtils.assertPrevious(cu,"m", EXIT,"m");

        TestUtils.assertPrevious(cu,"h()", ENTRY,"g()");
        TestUtils.assertPrevious(cu,"h()", EXIT,"h()");
        TestUtils.assertPrevious(cu,"g()", ENTRY,"f()");
        TestUtils.assertPrevious(cu,"g()", EXIT,"g()");
        TestUtils.assertPrevious(cu,"f()", ENTRY,"2<3");
        TestUtils.assertPrevious(cu,"f()", EXIT,"f()");
        TestUtils.assertPrevious(cu,"{ f(); g(); h(); }", ENTRY,"2<3");
        TestUtils.assertPrevious(cu,"{ f(); g(); h(); }", EXIT,"h()");

        TestUtils.assertPrevious(cu,"2<3", ENTRY,"3");
        TestUtils.assertPrevious(cu,"2<3", EXIT,"2<3");
        TestUtils.assertPrevious(cu,"3", ENTRY, "2");
        TestUtils.assertPrevious(cu,"3", EXIT,"3");
        TestUtils.assertPrevious(cu, "2", ENTRY, "j=1", "n++");
        TestUtils.assertPrevious(cu, "2", EXIT, "2");

        TestUtils.assertPrevious(cu,"int i=0, j=1", ENTRY, "a()");
        TestUtils.assertPrevious(cu,"int i=0, j=1", EXIT, "j=1");
        TestUtils.assertPrevious(cu,"i=0", ENTRY,"0");
        TestUtils.assertPrevious(cu,"i=0", EXIT,"i=0");
        TestUtils.assertPrevious(cu,"0", ENTRY,"a()");
        TestUtils.assertPrevious(cu,"0", EXIT,"0");
        TestUtils.assertPrevious(cu,"j=1", ENTRY,"1");
        TestUtils.assertPrevious(cu,"j=1", EXIT,"j=1");
        TestUtils.assertPrevious(cu,"1", ENTRY, "i=0");
        TestUtils.assertPrevious(cu,"1", EXIT,"1");
    }

    public static void testWhileLoop() {
        String source =
                "class C { \n" +
                        "    void a() {} \n" +
                        "    void b() {} \n" +
                        "    void method() { \n" +
                        "       a(); \n" +
                        "       String s; while((s = \"a\") == null) { s = null; } \n" +
                        "       b(); \n" +
                        "    } \n" +
                        "} \n" +
                        "";

        J.CompilationUnit cu = parse(source);
        DataFlowGraph dfg = new DataFlowGraph(cu);

        TestUtils.assertPrevious(cu,"while((s = \"a\") == null) { s = null; }", ENTRY,"s");
        TestUtils.assertPrevious(cu,"while((s = \"a\") == null) { s = null; }", EXIT,"((s = \"a\") == null)");
        TestUtils.assertPrevious(cu,"((s = \"a\") == null)", ENTRY, "(s = \"a\") == null");
        TestUtils.assertPrevious(cu,"((s = \"a\") == null)", EXIT, "((s = \"a\") == null)");
        TestUtils.assertPrevious(cu,"(s = \"a\") == null", ENTRY, "null");
        TestUtils.assertPrevious(cu,"(s = \"a\") == null", EXIT, "(s = \"a\") == null");
        TestUtils.assertPrevious(cu,"null", ENTRY,"(s = \"a\")");
        TestUtils.assertPrevious(cu,"null", EXIT,"null");
        TestUtils.assertPrevious(cu,"(s = \"a\")", ENTRY,"s = \"a\"");
        TestUtils.assertPrevious(cu,"(s = \"a\")", EXIT,"(s = \"a\")");
        TestUtils.assertPrevious(cu,"s = \"a\"", ENTRY,"\"a\"");
        TestUtils.assertPrevious(cu,"s = \"a\"", EXIT,"s = \"a\"");
        TestUtils.assertPrevious(cu,"{ s = null; }", ENTRY,"((s = \"a\") == null)");
        TestUtils.assertPrevious(cu,"{ s = null; }", EXIT,"s = null");
        TestUtils.assertPrevious(cu,"s = null", ENTRY,"null");
        TestUtils.assertPrevious(cu,"s = null", EXIT,"s = null");
    }

}
