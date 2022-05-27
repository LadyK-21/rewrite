/*
import org.openrewrite.Cursor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.dataflow2.ProgramPoint;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

public class FindProgramPoint extends JavaIsoVisitor {

    final String ppToFind;
    ProgramPoint result;

    public FindProgramPoint(String ppToFind) {
        this.ppToFind = ppToFind;
    }

    private static String print(ProgramPoint p, Cursor c) {
        return ((J) p).print(c).replace("\n", " ").replaceAll("[ ]+", " ").trim();
    }

    @Override
    public Statement visitStatement(Statement statement, Object o) {
        if (result != null) return statement;
        if (ppToFind.equals(print(statement, getCursor()))) {
            result = statement;
        } else {
            super.visit(statement, o);
        }
        return statement;
    }

    @Override
    public J.VariableDeclarations.NamedVariable visitVariable(J.VariableDeclarations.NamedVariable variable, Object o) {
        if (result != null) return variable;
        if (ppToFind.equals(print(variable, getCursor()))) {
            result = variable;
        } else {
            super.visit(variable, o);
        }
        return variable;
    }
}
*/