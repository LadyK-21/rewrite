/*
 * Copyright 2022 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.search

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.openrewrite.ExecutionContext
import org.openrewrite.InMemoryExecutionContext
import org.openrewrite.java.JavaIsoVisitor
import org.openrewrite.java.JavaParser
import org.openrewrite.java.JavaRecipeTest
import org.openrewrite.java.tree.J

interface FindNamesInScopeUtilTest : JavaRecipeTest {

    override val parser: JavaParser
        get() = JavaParser.fromJavaVersion()
            .logCompilationWarningsAndErrors(true)
            .build()

    @Test
    fun doNotAddPackagePrivateNameInSuperClass() {
        // language=java
        val source = arrayOf("""
            package foo;
            public class Super {
                int pkgPrivate = 0;
            }
        """.trimIndent(), """
            package bar;
            
            import foo.Super;
            
            class Test extends Super {
                int a = 0;
            }
        """.trimIndent())

        baseTest(source, "a", setOf("a"))
    }

    @Test
    fun staticImportFieldNames() {
        // language=java
        val source = arrayOf("""
            import static java.nio.charset.StandardCharsets.UTF_8;
            import static java.util.Collections.emptyList;
            
            class Test {
                int a = 0;
            }
        """.trimIndent())

        baseTest(source, "a", setOf("a", "UTF_8"))
    }

    @Test
    fun block() {
        // language=java
        val source = arrayOf("""
            class Test {
                int a = 0;
                void method(int b) {
                    int c = 0;
                    for (int d = 0; d < 10; d++) {
                        int e = 0;
                    }
                    int f = 0;
                }
                int g = 0;
            }
        """.trimIndent())

        baseTest(source, "e", setOf("a", "b", "c", "d", "e", "g"))
    }

    @Test
    fun innerClass() {
        // language=java
        val source = arrayOf("""
            class Test {
                int a = 0;
                void method(int b) {
                    int c = 0;
                }
                int d = 0;
                class Inner {
                    int e = 0;
                }
            }
        """.trimIndent())

        baseTest(source, "e", setOf("a", "d", "e"))
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "d:a,b,c,d,g",
            "e:a,b,c,d,e,g"
        ], delimiter = ':'
    )
    fun forLoop(idName: String, result: String) {
        // language=java
        val source = arrayOf("""
            class Test {
                int a = 0;
                void method(int b) {
                    int c = 0;
                    for (int d = 0; d < 10; d++) {
                        int e = 0;
                    }
                    int f = 0;
                }
                int g = 0;
            }
        """.trimIndent())

        val resultList: List<String> = result.split(",").map { it.trim() }
        baseTest(source, idName, resultList.toSet())
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "d:a,b,c,d,h",
            "e:a,b,c,e,h",
            "f:a,b,c,f,h"
        ], delimiter = ':'
    )
    fun ifElse(idName: String, result: String) {
        // language=java
        val source = arrayOf("""
            class Test {
                int a = 0;
                void method(int b) {
                    int c = 0;
                    if (b == 0) {
                        int d = 0;
                    } else if (b == 1) {
                        int e = 0;
                    } else {
                        int f = 0;
                    }
                    int g = 0;
                }
                int h = 0;
            }
        """.trimIndent())

        val resultList: List<String> = result.split(",").map { it.trim() }
        baseTest(source, idName, resultList.toSet())
    }

    @Suppress("UnnecessaryLocalVariable", "Convert2Lambda")
    @ParameterizedTest
    @CsvSource(
        value = [
            "d:a,b,c,d,g",
            "e:a,b,c,d,e,g",
            "f:a,b,c,d,f,g"
        ], delimiter = ':'
    )
    fun lambda(idName: String, result: String) {
        // language=java
        val source = arrayOf("""
            import java.util.function.Supplier;
            
            class Test {
                int a = 0;
                void method(int b) {
                    int c = 0;
                    Supplier<Integer> d = new Supplier<Integer>() {
                        @Override
                        public Integer get() {
                            int e = 0;
                            return e;
                        }
                    };
                    int f = 0;
                }
                int g = 0;
            }
        """.trimIndent())

        val resultList: List<String> = result.split(",").map { it.trim() }
        baseTest(source, idName, resultList.toSet())
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "a:a," +
                    "superPublic,superProtected,superPackagePrivate," +
                    "superSuperPublic,superSuperProtected,superSuperPackagePrivate"
        ], delimiter = ':'
    )
    fun superClass(idName: String, result: String) {
        // language=java
        val source = arrayOf("""
            package foo.bar;
            
            class SuperSuper {
                public int superSuperPublic;
                protected int superSuperProtected;
                private int superSuperPrivate;
                int superSuperPackagePrivate;
            }
        """.trimIndent(), """
            package foo.bar;
            
            class Super extends SuperSuper {
                public int superPublic;
                protected int superProtected;
                private int superPrivate;
                int superPackagePrivate;
            }
        """.trimIndent(), """
            package foo.bar;
            
            class Test extends Super {
                int a;
            }
        """.trimIndent())

        val resultList: List<String> = result.split(",").map { it.trim() }
        baseTest(source, idName, resultList.toSet())
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "d:a,b,c,d,g",
            "e:a,b,c,e,g"
        ], delimiter = ':'
    )
    fun switch(idName: String, result: String) {
        // language=java
        val source = arrayOf("""
            class Test {
                int a = 0;
                void method(int b) {
                    int c = 0;
                    switch (b) {
                        case 0:
                            int d = 0;
                            break;
                        case 1:
                            int e = 0;
                            break;
                        default:
                            break;
                    }
                    int f = 0;
                }
                int g = 0;
            }
        """.trimIndent())

        val resultList: List<String> = result.split(",").map { it.trim() }
        baseTest(source, idName, resultList.toSet())
    }

    @Suppress("CatchMayIgnoreException")
    @ParameterizedTest
    @CsvSource(
        value = [
            "d:a,b,c,d,k",
            "e:a,b,c,d,e,k",
            "f:a,b,c,d,e,f,k",
            "g:a,b,c,d,e,g,k",
            "h:a,b,c,d,e,g,h,k",
            "j:a,b,c,j,k"
        ], delimiter = ':'
    )
    fun tryCatchFinally(idName: String, result: String) {
        // language=java
        val source = arrayOf("""
            import java.io.*;
            
            class Test {
                int a = 0;
                void method(int b) {
                    File c = new File("file.txt");
                    try (FileInputStream d = new FileInputStream(c); FileInputStream e = new FileInputStream(c)) {
                        int f;
                    } catch (RuntimeException | IOException g) {
                        int h = 0;
                    } finally {
                        int i = 0;
                    }
                    int j = 0;
                }
                int k = 0;
            }
        """.trimIndent())

        val resultList: List<String> = result.split(",").map { it.trim() }
        baseTest(source, idName, resultList.toSet())
    }

    fun baseTest(source: Array<String>, input: String, expected: Set<String>) {
        val sources = parser.parse(
            InMemoryExecutionContext(),
            *source
        )

        val names = mutableMapOf<String, Set<String>>()
        val recipe = toRecipe {
            object : JavaIsoVisitor<ExecutionContext>() {
                override fun visitIdentifier(identifier: J.Identifier, p: ExecutionContext): J.Identifier {
                    return if (identifier.simpleName == input) {
                        val cu: J.CompilationUnit = this.cursor.dropParentUntil { it is J.CompilationUnit }.getValue()
                        names[identifier.simpleName] = FindNamesInScopeUtil.findVariableNamesInScope(cu, this.cursor)
                        identifier.withSimpleName("changed")
                    } else {
                        identifier
                    }
                }
            }
        }

        recipe.run(sources)
        val result = names[input]!!.toList().sorted()
        assertThat(result).containsAll(expected)
        assertThat(expected).containsAll(result)
    }
}
