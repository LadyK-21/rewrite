package sandbox;

import org.assertj.core.api.AssertionsForClassTypes;
import org.openrewrite.Cursor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.dataflow2.DataFlowGraph;
import org.openrewrite.java.dataflow2.ProgramPoint;
import org.openrewrite.java.dataflow2.Utils;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.openrewrite.java.dataflow2.Utils.print;

public class TestUtils {



    public static void assertPrevious(J.CompilationUnit cu, String pp, ProgramPoint entryOrExit, String... previous) {
        DataFlowGraph dfg = new DataFlowGraph(cu);
        List<String> expected = Arrays.asList(previous);
        Cursor c = Utils.findProgramPoint(cu, pp);
        AssertionsForClassTypes.assertThat(c).withFailMessage("program point <" + pp + "> not found").isNotNull();
        Collection<Cursor> prevs = dfg.previousIn(c, entryOrExit);
        AssertionsForClassTypes.assertThat(prevs).withFailMessage("previous() returned null").isNotNull();
        List<String> actual = prevs.stream().map(prev -> print(prev)).collect(Collectors.toList());

        expected.sort(String.CASE_INSENSITIVE_ORDER);
        actual.sort(String.CASE_INSENSITIVE_ORDER);
        AssertionsForClassTypes.assertThat(actual)
                .withFailMessage("previous(" + pp + ", " + entryOrExit + ")\nexpected: " + expected + "\n but was: " + actual)
                .isEqualTo(expected);
    }

    /*
    public static void assertLast(J.CompilationUnit cu, String pp, String... last) {
        List<String> expected = Arrays.asList(last);
        Cursor c = TestUtils.findProgramPoint(cu, pp);
        assertThat(c).withFailMessage("program point <" + pp + "> not found").isNotNull();
        Collection<Cursor> lasts = DataFlowGraph.last(c);
        assertThat(c).withFailMessage("last() returned null").isNotNull();
        List<String> actual = lasts.stream().map(l -> print(l)).collect(Collectors.toList());

        expected.sort(String.CASE_INSENSITIVE_ORDER);
        actual.sort(String.CASE_INSENSITIVE_ORDER);
        assertThat(actual)
                .withFailMessage("last(" + pp + ")\nexpected: " + expected + "\n but was: " + actual)
                .isEqualTo(expected);
    }
*/

}
