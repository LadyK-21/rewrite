package org.openrewrite.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.tree.J;

import java.util.List;

public class Java11EffectsAnalysisTest {
    @Test
    public void test() {
        Java11Parser parser = new Java11Parser.Builder().build();
        ExecutionContext ctx = new InMemoryExecutionContext();
        String source = "class C { void m() { int x = 0; x = 1; }}";
        List<J.CompilationUnit> cus = parser.parse(ctx, source);
        J.CompilationUnit cu = cus.get(0);
        cu.print();
    }
}
