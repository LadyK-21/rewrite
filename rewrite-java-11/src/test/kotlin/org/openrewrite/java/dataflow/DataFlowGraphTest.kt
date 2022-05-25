package org.openrewrite.java.dataflow

import org.junit.jupiter.api.Test;
import org.assertj.core.api.Assertions.*;
import org.openrewrite.Cursor
import org.openrewrite.java.JavaParser
import org.openrewrite.java.dataflow2.DataFlowGraph
import org.openrewrite.java.tree.DefaultJavaTypeSignatureBuilderTest
import org.openrewrite.java.tree.J

class DataFlowGraphTest {

    private fun parse(src: String) = JavaParser.fromJavaVersion()
        // TODO fail test in case of parse error
        .logCompilationWarningsAndErrors(true)
        .build()
        .parse(src)[0];

    @Test
    fun previousInForLoop() {
        val src = """
            class C {
                void a() {} 
                void b() {} 
                void f() {} 
                void g() {} 
                void h() {} 
                void m() {
                    a();
                    for(int i=0, j=1; i++<10 && j<10; i++, j++) {
                        f(); g(); h();
                    }
                    b();
                }
            }
        """;

        val cu = parse(src);

        assertThat(
            FindProgramPoint.print(FindProgramPoint.findProgramPoint(cu, "g()"))
        ).isEqualTo(
            "g()"
        )

        FindProgramPoint.assertPrevious(cu,"a()");
        FindProgramPoint.assertPrevious(cu,"int i=0, j=1", "a()");
        FindProgramPoint.assertPrevious(cu,"i=0", "a()");
        FindProgramPoint.assertPrevious(cu,"j=1", "i=0");
        FindProgramPoint.assertPrevious(cu,"j++", "i++");
        FindProgramPoint.assertPrevious(cu,"i++", "{ f(); g(); h(); }");
        // "j++" instead of "i++, j++" since update is List<Statement>, i.e. not a ProgramPoint
        FindProgramPoint.assertPrevious(cu,"{ f(); g(); h(); }", "int i=0, j=1", "j++");
        // "j++" instead of "i++, j++" since update is List<Statement>, i.e. not a ProgramPoint
        FindProgramPoint.assertPrevious(cu,"f()","int i=0, j=1", "j++");
        FindProgramPoint.assertPrevious(cu,"g()", "f()");
        FindProgramPoint.assertPrevious(cu,"h()", "g()");
        FindProgramPoint.assertPrevious(cu,"b()",
            "for(int i=0, j=1; i<10 && j<10; i++, j++) { f(); g(); h(); }");

    }
}
