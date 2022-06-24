package sandbox;

import org.openrewrite.Cursor;
import org.openrewrite.java.dataflow2.DataFlowGraph;
import org.openrewrite.java.dataflow2.ProgramState;
import org.openrewrite.java.dataflow2.Utils;
import org.openrewrite.java.dataflow2.examples.ConstantPropagation;
import org.openrewrite.java.dataflow2.examples.ConstantPropagationValue;
import org.openrewrite.java.dataflow2.examples.IsNullAnalysis;
import org.openrewrite.java.dataflow2.examples.ModalBoolean;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.openrewrite.java.dataflow2.examples.ModalBoolean.*;
import static sandbox.TestUtils.parse;

public class TestConstantPropagation {
    public static void test()
    {
        // Test the value of variable at the end of given code fragment.

        testConstantValue("String s = \"a\";", "s",
                new ConstantPropagationValue(ConstantPropagationValue.Understanding.KNOWN, "a"));
        testConstantValue("String a = \"a\"; String b = \"b\"; String s = a + b;", "s",
                new ConstantPropagationValue(ConstantPropagationValue.Understanding.KNOWN, "ab"));
        testConstantValue("String s = \"a\"; while(x == 0) { s = \"b\"; }", "s",
                ConstantPropagationValue.CONFLICT);

//        testConstantValue("String s; while((s = \"a\") == null) { s = null; }", False);
//        testConstantValue("String s; while((s = \"a\") == null) { s = \"b\"; }", False);
//
//        testConstantValue("String s = null; while(c) { s = \"a\"; }", Conflict);
//        testConstantValue("String s = null; while(c) { s = null; }", True);
//        testConstantValue("String s = \"a\"; while(c) { s = null; }", Conflict);
//        testConstantValue("String s = \"a\"; while(c) { s = \"b\"; }", False);
//        testConstantValue("String s; while((s = null) == null) { s = \"a\"; }", True);
//        testConstantValue("String s; while((s = null) == null) { s = null; }", True);
//
//        testConstantValue("String s = f(); if(s == null) { s = \"a\"; }", False);
////        // Understanding that s is always null below requires constant propagation
////        // and partial evaluation of the condition
////        //testConstantValue("String s = null; if(s == \"b\") { s = \"a\"; }", Conflict);
//
//        testConstantValue("String s, t; t = (s = null);", True);
//        testConstantValue("String s, t; s = (t = null);", True);
//        testConstantValue("String s = \"a\", t, u; t = (u = null);", False);
//
//        testConstantValue("String s = null;", True);
//        testConstantValue("String s = \"abc\";", False);
//        testConstantValue("String s; s = null; s = \"abc\";", False);
//        testConstantValue("String s; s = \"abc\"; s = null;", True);
//        testConstantValue("String q = null; String s = q;", True);
//        testConstantValue("String q = \"abc\"; String s = q;", False);
//        testConstantValue("String s = null + null;", False);
//        testConstantValue("String s = \"a\" + null;", False);
//        testConstantValue("String s = null + \"b\";", False);
//        testConstantValue("String s = \"a\" + \"b\";", False);
//        testConstantValue("String s = u;", null); // Because u is undefined
//        testConstantValue("String u = null; String s = u;", True);
//        testConstantValue("String s = \"a\".toUpperCase();", False);
//        testConstantValue("String s = \"a\".unknownMethod(s, null);", NoIdea);
//        testConstantValue("String s; if(c) { s = null; } else { s = null; }", True);
//        testConstantValue("String s; if(c) { s = null; } else { s = \"b\"; }", Conflict);
//        testConstantValue("String s; if(c) { s = \"a\"; } else { s = null; }", Conflict);
//        testConstantValue("String s; if(c) { s = \"a\"; } else { s = \"b\"; }", False);
//        testConstantValue("String s, q; if((s = null) == null) { q = \"a\"; } else { q = null; }",
//                True);


    }

    /**
     * Test the value of 's' at the end of given code fragment.
     */
    public static void testConstantValue(String fragment, String variable, ConstantPropagationValue expected) {
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

        System.out.println(fragment);

        J.CompilationUnit cu = parse(source);

        //new PrintProgramPointsVisitor().visit(cu, null);

        String pp1 = "b()";
        Cursor c1 = Utils.findProgramPoint(cu, pp1);
        assertThat(c1).withFailMessage("program point <" + pp1 + "> not found").isNotNull();

        //String pp2 = "s";
        JavaType.Variable v = Utils.findVariable(cu, variable);
        assertThat(v).isNotNull();

        DataFlowGraph dfg = new DataFlowGraph(cu);
        ConstantPropagation analysis = new ConstantPropagation(dfg);
        analysis.doAnalysis(c1);
        ProgramState state = analysis.analysis2(c1);

//
//        IsNullAnalysis a = new IsNullAnalysis(dfg);
//        a.analyze(c1, new TraversalControl<>());
//        ProgramState state = a.inputState(c1, new TraversalControl<>());

        System.out.println("    Value of " + variable + " when entering 'b()' = " + state.get(v));

        assertThat(state.get(v)).isEqualTo(expected);
    }

}
