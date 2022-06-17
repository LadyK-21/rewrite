package org.openrewrite.java.dataflow2.examples

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.openrewrite.java.JavaParser
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

interface UseSecureHttpConnectionTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(UseSecureHttpConnection())
    }

    @Test
    fun `find insecure uri`(javaParser: JavaParser) = rewriteRun(
        { spec -> spec.parser(javaParser) },
        java(
            """
                import java.net.URI;
                class Test {
                    void test() {
                        String s = "http://test";
                        String t = s;
                        if(System.currentTimeMillis() > 0) {
                            System.out.println(URI.create(t));
                        } else {
                            System.out.println(URI.create(t));
                        }
                    }
                }
            """, """
                import java.net.URI;
                class Test {
                    void test() {
                        String s = "https://test";
                        String t = s;
                        if(System.currentTimeMillis() > 0) {
                            System.out.println(URI.create(t));
                        } else {
                            System.out.println(URI.create(t));
                        }
                    }
                }
            """
        )
    )

    @Test
    fun `replace is a barrier guard`(javaParser: JavaParser) = rewriteRun(
        { spec -> spec.parser(javaParser) },
        java(
            """
                import java.net.URI;
                class Test {
                    void test() {
                        String s = "http://test";
                        s = s.replace("http://", "https://");
                        if(System.currentTimeMillis() > 0) {
                            System.out.println(URI.create(s));
                        } else {
                            System.out.println(URI.create(s));
                        }
                    }
                }
            """
        )
    )

    @Test
    fun `reassignment breaks data flow path`(javaParser: JavaParser) = rewriteRun(
        { spec -> spec.parser(javaParser) },
        java(
            """
                import java.net.URI;
                class Test {
                    void test() {
                        String s = "http://test";
                        s = "https://example.com";
                        if(System.currentTimeMillis() > 0) {
                            System.out.println(URI.create(s));
                        } else {
                            System.out.println(URI.create(s));
                        }
                    }
                }
            """
        )
    )

    @Test
    @Disabled("MISSING: Assignment dominance of conditional that will always evaluate to true")
    fun `reassignment in always evaluated path breaks data flow path`(javaParser: JavaParser) = rewriteRun(
        { spec -> spec.parser(javaParser) },
        java(
            """
                import java.net.URI;
                class Test {
                    void test() {
                        String s = "http://test";
                        if (true) {
                            s = "https://example.com";
                        }
                        if(System.currentTimeMillis() > 0) {
                            System.out.println(URI.create(s));
                        } else {
                            System.out.println(URI.create(s));
                        }
                    }
                }
            """
        )
    )

    @Test
    fun `reassignment within block does not break path`(javaParser: JavaParser) = rewriteRun(
        { spec -> spec.parser(javaParser) },
        java(
            """
                import java.net.URI;
                class Test {
                    void test() {
                        String s = "http://test";
                        if(System.currentTimeMillis() > 0) {
                            s = "https://example.com";
                            System.out.println(URI.create(s));
                        } else {
                            System.out.println(URI.create(s));
                        }
                    }
                }
            """,
            """
                import java.net.URI;
                class Test {
                    void test() {
                        String s = "https://test";
                        if(System.currentTimeMillis() > 0) {
                            s = "https://example.com";
                            System.out.println(URI.create(s));
                        } else {
                            System.out.println(URI.create(s));
                        }
                    }
                }
            """
        )
    )

    @Test
    fun `dataflow through ternary operator`(javaParser: JavaParser) = rewriteRun(
        { spec -> spec.parser(javaParser) },
        java(
            """
                import java.net.URI;
                class Test {
                    void test() {
                        String s = "http://test";
                        String t = true ? s : null;
                        if(System.currentTimeMillis() > 0) {
                            System.out.println(URI.create(t));
                        } else {
                            System.out.println(URI.create(t));
                        }
                    }
                }
            """,
            """
                import java.net.URI;
                class Test {
                    void test() {
                        String s = "https://test";
                        String t = true ? s : null;
                        if(System.currentTimeMillis() > 0) {
                            System.out.println(URI.create(t));
                        } else {
                            System.out.println(URI.create(t));
                        }
                    }
                }
            """
        )
    )

    @Test
    fun `example of taint tracking`(javaParser: JavaParser) = rewriteRun(
        { spec -> spec.parser(javaParser) },
        java(
            """
                import java.io.File;
                import java.net.URI;
                class Test {
                    void test() {
                        String s = "http://test" + File.separator;
                        if(System.currentTimeMillis() > 0) {
                            System.out.println(URI.create(s));
                        } else {
                            System.out.println(URI.create(s));
                        }
                    }
                }
            """,
            """
                import java.io.File;
                import java.net.URI;
                class Test {
                    void test() {
                        String s = "https://test" + File.separator;
                        if(System.currentTimeMillis() > 0) {
                            System.out.println(URI.create(s));
                        } else {
                            System.out.println(URI.create(s));
                        }
                    }
                }
            """
        )
    )

    @Test
    fun `example of taint tracking through an alternate flow path`(javaParser: JavaParser) = rewriteRun(
        { spec -> spec.parser(javaParser) },
        java(
            """
                import java.io.File;
                import java.net.URI;
                class Test {
                    void test() {
                        String s = "http://test";
                        if (System.currentTimeMillis() > 0) {
                            System.out.println(URI.create(s + "/home"));
                        } else {
                            System.out.println(URI.create(s + "/away"));
                        }
                    }
                }
            """,
            """
                import java.io.File;
                import java.net.URI;
                class Test {
                    void test() {
                        String s = "https://test";
                        if (System.currentTimeMillis() > 0) {
                            System.out.println(URI.create(s + "/home"));
                        } else {
                            System.out.println(URI.create(s + "/away"));
                        }
                    }
                }
            """
        )
    )

    @Test
    fun `example of negative taint tracking`(javaParser: JavaParser) = rewriteRun(
        { spec -> spec.parser(javaParser) },
        java(
            """
                import java.io.File;
                import java.net.URI;
                class Test {
                    void test() {
                        String s = "https://example.com/?redirect=" + "http://test";
                        if(System.currentTimeMillis() > 0) {
                            System.out.println(URI.create(s));
                        } else {
                            System.out.println(URI.create(s));
                        }
                    }
                }
            """
        )
    )

    @Test
    fun `arbitrary method calls are not dataflow`(javaParser: JavaParser) = rewriteRun(
        { spec -> spec.parser(javaParser) },
        java(
            """
                import java.io.File;
                import java.net.URI;
                class Test {
                    void test() {
                        String s = "http://test";
                        String t = someMethod(s);
                        if(System.currentTimeMillis() > 0) {
                            System.out.println(URI.create(t));
                        } else {
                            System.out.println(URI.create(t));
                        }
                    }

                    String someMethod(String input) {
                        return null;
                    }
                }
            """
        )
    )

    @Test
    fun `arbitrary method call chains are not dataflow`(javaParser: JavaParser) = rewriteRun(
        { spec -> spec.parser(javaParser) },
        java(
            """
                import java.io.File;
                import java.net.URI;
                import java.util.Locale;
                class Test {
                    void test() {
                        String s = "http://test";
                        String t = s.toLowerCase(Locale.ROOT);
                        if(System.currentTimeMillis() > 0) {
                            System.out.println(URI.create(t));
                        } else {
                            System.out.println(URI.create(t));
                        }
                    }
                }
            """
        )
    )

    @Test
    fun `special case toString on String type is DataFlow`(javaParser: JavaParser) = rewriteRun(
        { spec -> spec.parser(javaParser) },
        java(
            """
                import java.io.File;
                import java.net.URI;
                import java.util.Locale;
                @SuppressWarnings("RedundantSuppression")
                class Test {
                    @SuppressWarnings("StringOperationCanBeSimplified")
                    void test() {
                        String s = "http://test";
                        String t = s.toString();
                        if(System.currentTimeMillis() > 0) {
                            System.out.println(URI.create(t));
                        } else {
                            System.out.println(URI.create(t));
                        }
                    }
                }
            """,
            """
                import java.io.File;
                import java.net.URI;
                import java.util.Locale;
                @SuppressWarnings("RedundantSuppression")
                class Test {
                    @SuppressWarnings("StringOperationCanBeSimplified")
                    void test() {
                        String s = "https://test";
                        String t = s.toString();
                        if(System.currentTimeMillis() > 0) {
                            System.out.println(URI.create(t));
                        } else {
                            System.out.println(URI.create(t));
                        }
                    }
                }
            """
        )
    )

    @Test
    fun `zero step flow is still considered and fixed`(javaParser: JavaParser) = rewriteRun(
        { spec -> spec.parser(javaParser) },
        java(
            """
                import java.net.URI;
                class Test {
                    void test() {
                        URI uri = URI.create("http://test");
                    }
                }
            """, """
                import java.net.URI;
                class Test {
                    void test() {
                        URI uri = URI.create("https://test");
                    }
                }
            """
        )
    )
}