package org.openrewrite.java.dataflow2

import org.junit.jupiter.api.Test
import org.openrewrite.java.JavaParser
import org.openrewrite.java.tree.J
import org.openrewrite.test.RewriteTest

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.Disabled

import org.openrewrite.Cursor

import org.openrewrite.java.dataflow2.ProgramPoint.ENTRY

import org.openrewrite.java.dataflow2.ProgramPoint.EXIT

import org.openrewrite.java.dataflow2.Utils.print
import java.util.stream.Collectors

interface DataFlowGraphTest : RewriteTest {

    @Test
    fun arrayAccess(jp: JavaParser) {
        val ac = "s = str[0]"
        val cu :J.CompilationUnit = compile(ac, jp)

        assertPrevious(cu, "s = str[0]", ENTRY, "str[0]")
        assertPrevious(cu, "s = str[0]", EXIT, "s = str[0]")

        assertPrevious(cu, "str[0]", ENTRY,"0")
        assertPrevious(cu, "str[0]", EXIT,"str[0]")

        assertPrevious(cu, "0", ENTRY, "str")
        assertPrevious(cu, "0", EXIT, "0")

        assertPrevious(cu, "str", ENTRY,"a()")
        assertPrevious(cu, "str", EXIT,"str")

    }

    @Test
    fun methodInvocation(jp: JavaParser) {
        val ac = "abc = myMethod(a1, b1)"
        val cu :J.CompilationUnit = compile(ac, jp)

        assertPrevious(cu, ac, ENTRY, "myMethod(a1, b1)")
        assertPrevious(cu, ac, EXIT, ac)

        assertPrevious(cu, "myMethod(a1, b1)", ENTRY,"b1")
        assertPrevious(cu, "myMethod(a1, b1)", EXIT,"myMethod(a1, b1)")

        assertPrevious(cu, "b1", ENTRY, "a1")
        assertPrevious(cu, "b1", EXIT, "b1")

        assertPrevious(cu, "a1", ENTRY, "b1")
        assertPrevious(cu, "a1", EXIT, "a1")

        assertPrevious(cu, "myMethod", EXIT,"myMethod")
        assertPrevious(cu, "myMethod", ENTRY,"a()")

    }
    fun compile(s :String, jp: JavaParser): J.CompilationUnit {
        val template =
            """
        class A {
            void a();
            void b();
            void m(String u, String v) {
                a();
                __FRAGMENT__
                b();
            }
        }
        """.trimIndent()
        return jp.parse(template.replace("__FRAGMENT__", s))[0]
    }

    @Test
    fun basicDataFlow(jp: JavaParser) {
        val source = """
            class C {
                void a() {}
                void b() {}
                void m() {
                    a();
                    int i = u + v, j = w;
                    b();
                }
            }
        """.trimIndent()
        val cu :J.CompilationUnit = jp.parse(source)[0]
        assertThat(cu.printAll()).isEqualTo(source)
        assertPrevious(cu, "b()", ENTRY, "j = w")
        assertPrevious(cu,"j = w", EXIT, "j = w")
        assertPrevious(cu,"j = w", ENTRY, "w")
        assertPrevious(cu,"w", EXIT,"w")
        assertPrevious(cu,"w", ENTRY,"i = u + v")
        assertPrevious(cu,"i = u + v", EXIT, "i = u + v")
        assertPrevious(cu,"i = u + v", ENTRY, "u + v")
        assertPrevious(cu,"u + v", EXIT, "u + v")
        assertPrevious(cu,"u + v", ENTRY, "v")
        assertPrevious(cu,"v", EXIT, "v")
        assertPrevious(cu,"v", ENTRY, "u")
        assertPrevious(cu,"u", EXIT, "u")
        assertPrevious(cu,"u", ENTRY, "a()")
    }

    fun assertPrevious(cu: J.CompilationUnit, pp :String, entryOrExit :ProgramPoint, vararg previous :String) {
        assertThat(cu).isNotNull
        val c :Cursor = Utils.findProgramPoint(cu, pp)
        assertThat(c).isNotNull
        val dfg = DataFlowGraph(cu)
        val pps :Collection<Cursor> = dfg.previousIn(c, entryOrExit)
        assertThat(pps).isNotNull
        assertThat(pps).isNotEmpty

        val actual :List<String> = pps.stream().map { prev -> print(prev) }.collect(Collectors.toList())
        val expected :List<String> = previous.asList()

        AssertionsForClassTypes
            .assertThat(actual)
            .withFailMessage("previous($pp, $entryOrExit)\nexpected: $expected\n but was: $actual")
            .isEqualTo(expected)

    }
}