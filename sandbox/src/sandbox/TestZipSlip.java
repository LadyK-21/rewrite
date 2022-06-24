package sandbox;

import org.assertj.core.api.AssertionsForClassTypes;
import org.openrewrite.Cursor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.dataflow2.DataFlowGraph;
import org.openrewrite.java.dataflow2.ProgramState;
import org.openrewrite.java.dataflow2.examples.ZipSlip;
import org.openrewrite.java.dataflow2.examples.ZipSlipValue;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

import static sandbox.TestUtils.parse;

public class TestZipSlip {

    public static void test()
    {
        testZipSlip();
    }

    public static void testZipSlip() {

        testZipSlip("source1", ZipSlipValue.NewFileFromZipEntry.class,
                "import java.io.File; \n" +
                        "import java.io.FileOutputStream; \n" +
                        "import java.io.RandomAccessFile; \n" +
                        "import java.io.FileWriter; \n" +
                        "import java.util.zip.ZipEntry; \n" +
                        "public class ZipTest { \n" +
                        "    public void m1(ZipEntry entry, File dir) throws Exception { \n" +
                        "        String name = entry.getName(); \n" +
                        "        File file = new File(dir, name); \n" +
                        "        FileOutputStream os = new FileOutputStream(file); // ZipSlip \n" +
                        "    } \n" +
                        "} \n" +
                        "");

        testZipSlip("source2", ZipSlipValue.NewFileFromZipEntry.class,
                "import java.io.File; \n" +
                        "import java.io.FileOutputStream; \n" +
                        "import java.io.RandomAccessFile; \n" +
                        "import java.io.FileWriter; \n" +
                        "import java.util.zip.ZipEntry; \n" +
                        "public class ZipTest { \n" +
                        "    public void m1(ZipEntry entry, File dir) throws Exception { \n" +
                        "        String name1 = entry.getName(); \n" +
                        "        String name2 = name1; \n" +
                        "        File file = new File(dir, name2); \n" +
                        "        FileOutputStream os = new FileOutputStream(file); // ZipSlip \n" +
                        "    } \n" +
                        "} \n" +
                        "");

        testZipSlip("source3", ZipSlipValue.Unknown.class,
                "import java.io.File; \n" +
                        "import java.io.FileOutputStream; \n" +
                        "import java.io.RandomAccessFile; \n" +
                        "import java.io.FileWriter; \n" +
                        "import java.util.zip.ZipEntry; \n" +
                        "public class ZipTest { \n" +
                        "    public void m1(ZipEntry entry, File dir) throws Exception { \n" +
                        "        String name1 = entry.getName(); \n" +
                        "        String name2 = name1 + \"/\"; \n" +
                        "        File file = new File(dir, name2); \n" +
                        "        FileOutputStream os = new FileOutputStream(file); // ZipSlip \n" +
                        "    } \n" +
                        "} \n" +
                        "");

        testZipSlip("source4", ZipSlipValue.Unknown.class,
                "import java.io.File; \n" +
                        "import java.io.FileOutputStream; \n" +
                        "import java.io.RandomAccessFile; \n" +
                        "import java.io.FileWriter; \n" +
                        "import java.util.zip.ZipEntry; \n" +
                        "public class ZipTest { \n" +
                        "    public void m1(ZipEntry entry, File dir) throws Exception { \n" +
                        "        String name1 = entry.getName(); \n" +
                        "        String name2 = someUnknownMethod(name1); \n" +
                        "        File file = new File(dir, name2); \n" +
                        "        FileOutputStream os = new FileOutputStream(file); // ZipSlip \n" +
                        "    } \n" +
                        "} \n" +
                        "");

        testZipSlip("source5", ZipSlipValue.Safe.class,
                "import java.io.File; \n" +
                        "import java.io.FileOutputStream; \n" +
                        "import java.io.RandomAccessFile; \n" +
                        "import java.io.FileWriter; \n" +
                        "import java.util.zip.ZipEntry; \n" +
                        "public class ZipTest { \n" +
                        "    public void m1(ZipEntry entry, File dir) throws Exception { \n" +
                        "        String name = entry.getName(); \n" +
                        "        File file = new File(dir, name); \n" +
                        "        if (!file.toPath().startsWith(dir.toPath())) { \n" +
                        "            //throw new UncheckedIOException(\"ZipSlip attack detected\"); \n" +
                        "            file = null; \n" +
                        "        } \n" +
                        "        FileOutputStream os = new FileOutputStream(file); // ZipSlip \n" +
                        "    } \n" +
                        "} \n" +
                        "");

    }

    public static void testZipSlip(String name, Class expectedClass, String source) {

        System.out.println("Processing test " + name);
        J.CompilationUnit cu = parse(source);

        FindConstructorInvocationVisitor visitor = new FindConstructorInvocationVisitor();
        visitor.visit(cu, null);

        Cursor newClassCursor = visitor.result;
        J.NewClass newClass = newClassCursor.getValue();

        Expression file = newClass.getArguments().get(0);
        Cursor fileCursor = new Cursor(newClassCursor, file);

        // We're interested in the expr() of the output state of 'file'
        ZipSlip zipSlip = new ZipSlip(new DataFlowGraph(cu));
        zipSlip.doAnalysis(fileCursor);

        ProgramState<ZipSlipValue> state = zipSlip.analysis2(file);

        ZipSlipValue actual = state.expr();

        System.out.println("state.expr() = " + actual);

        if(actual instanceof ZipSlipValue.NewFileFromZipEntry) {
            // unsafe, and we know the value of 'dir'
            System.out.println(" -> requires a guard");
        } else if(state.expr() == ZipSlipValue.UNKNOWN) {
            System.out.println(" -> maybe requires a guard");
        } else if(state.expr() == ZipSlipValue.SAFE) {
            System.out.println(" -> does not require a guard");
        } else if(state.expr() == ZipSlipValue.UNSAFE) {
            System.out.println(" -> requires a guard");
        }

        AssertionsForClassTypes.assertThat(actual.getClass())
                .withFailMessage("expected: " + expectedClass + "\n but was: " + actual)
                .isEqualTo(expectedClass);

        System.out.println();
    }

    static class FindConstructorInvocationVisitor extends JavaIsoVisitor {
        MethodMatcher m = new MethodMatcher("java.io.FileOutputStream <constructor>(java.io.File)");

        Cursor result = null;

        @Override
        public J.NewClass visitNewClass(J.NewClass newClass, Object o) {
            if(m.matches(newClass)) {
                System.out.println("Found constructor invocation " + newClass.print(getCursor()));
                result = getCursor();
            }
            return super.visitNewClass(newClass, o);
        }
    }
}
