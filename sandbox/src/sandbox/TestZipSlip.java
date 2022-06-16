package sandbox;

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
        testZipSlip("source1",
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

        testZipSlip("source2",
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

        testZipSlip("source3",
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

        testZipSlip("source4",
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

        testZipSlip("source5",
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

    public static void testZipSlip(String name, String source) {

        System.out.println("Processing test " + name);
        MethodMatcher m = new MethodMatcher("java.io.FileOutputStream <constructor>(java.io.File)");
        J.CompilationUnit cu = parse(source);

        JavaIsoVisitor myVisitor = new JavaIsoVisitor() {

            @Override
            public J.NewClass visitNewClass(J.NewClass newClass, Object o) {
                if(m.matches(newClass)) {
                    System.out.println("Found constructor invocation " + newClass.print(getCursor()));

                    Expression arg = newClass.getArguments().get(0);

                    // We're interested in the expr() of the output state of arg
                    ZipSlip zipSlip = new ZipSlip(new DataFlowGraph(cu));
                    ProgramState<ZipSlipValue> state = zipSlip.outputState(new Cursor(new Cursor(getCursor(), newClass.getArguments()), arg), null);

                    System.out.println("state.expr() = " + state.expr());
                    if(state.expr().dir != null) {
                        // unsafe, and we know the value of 'dir'
                        System.out.println(" -> requires a guard");
                    } else if(state.expr() == ZipSlipValue.UNKNOWN) {
                        System.out.println(" -> maybe requires a guard");
                    } else if(state.expr() == ZipSlipValue.SAFE) {
                        System.out.println(" -> does not require a guard");
                    } else if(state.expr() == ZipSlipValue.UNSAFE) {
                        System.out.println(" -> requires a guard");
                    }
                }
                return super.visitNewClass(newClass, o);
            }
        };

        myVisitor.visit(cu, null);

        System.out.println();
    }
}
