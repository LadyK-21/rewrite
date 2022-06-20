package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.Incubating;
import org.openrewrite.java.tree.J;

import java.util.*;

import static org.openrewrite.java.dataflow2.ProgramPoint.ENTRY;

@Incubating(since = "7.25.0")
public abstract class DataFlowAnalysis<T> {

    final DataFlowGraph dfg;
    Map<Cursor, ProgramState<T>> analysis = new HashMap<>();
    Queue<Cursor> workList = new ArrayDeque<>();

    public DataFlowAnalysis(DataFlowGraph dfg) {
        this.dfg = dfg;
    }

    public ProgramState<T> inputState(Cursor c, TraversalControl<ProgramState<T>> t) {
        ProgramPoint pp = c.getValue();

        List<ProgramState<T>> outs = new ArrayList<>();
        Collection<Cursor> sources = dfg.previousIn(c, ENTRY);
        for (Cursor source : sources) {
            // Since program points are represented by cursors with a tree node value,
            // it is impossible to add program points when there is no corresponding tree node.
            // To work around this limitation, we use cursor messages to express that a given
            // edge goes through a virtual program point.

            if (source.getMessage("ifThenElseBranch") != null) {
                J.If ifThenElse = source.firstEnclosing(J.If.class);
                ProgramState<T> s1 = outputState(source, t);
                ProgramState<T> s2 = transferToIfThenElseBranches(ifThenElse, s1, source.getMessage("ifThenElseBranch"));
                outs.add(s2);
            } else {
                outs.add(outputState(source, t));
            }
        }
        ProgramState<T> result = join(outs);
        return result;
    }

    public abstract ProgramState<T> join(Collection<ProgramState<T>> outs);

    @SafeVarargs
    public final ProgramState<T> join(ProgramState<T>... outs) {
        return join(Arrays.asList(outs));
    }

    public Map<ProgramPoint, ProgramState<T>> visited = new HashMap<>();

    public ProgramState<T> outputState(Cursor pp, TraversalControl<ProgramState<T>> t) {
        ProgramState<T> p = visited.get(pp.getValue());
        if(p != null) {
            return p;
        } else {
            visited.put(pp.getValue(), new ProgramState<>());
            p = outputState2(pp, t);
            visited.put(pp.getValue(), p);
            return p;
        }
    }

    public ProgramState<T> outputState2(Cursor pp, TraversalControl<ProgramState<T>> t) {
        switch (pp.getValue().getClass().getName().replaceAll("^org.openrewrite.java.tree.", "")) {
            case "J$MethodInvocation":
                return transferMethodInvocation(pp, t);
            case "J$NewClass":
                return transferNewClass(pp, t);
            case "J$If":
                return transferIf(pp, t);
            case "J$If$Else":
                return transferIfElse(pp, t);
            case "J$WhileLoop":
                return transferWhileLoop(pp, t);
            case "J$ForLoop":
                return transferForLoop(pp, t);
            case "J$ForLoop$Control":
                return transferForLoopControl(pp, t);
            case "J$Block":
                return transferBlock(pp, t);
            case "J$VariableDeclarations":
                return transferVariableDeclarations(pp, t);
            case "J$VariableDeclarations$NamedVariable":
                return transferNamedVariable(pp, t);
            case "J$Unary":
                return transferUnary(pp, t);
            case "J$Binary":
                return transferBinary(pp, t);
            case "J$Assignment":
                return transferAssignment(pp, t);
            case "J$Parentheses":
                return transferParentheses(pp, t);
            case "J$ControlParentheses":
                return transferControlParentheses(pp, t);
            case "J$Literal":
                return transferLiteral(pp, t);
            case "J$Identifier":
                return transferIdentifier(pp, t);
            case "J$Empty":
                return transferEmpty(pp, t);
            case "J$FieldAccess":
                // TODO: just a hack to make the UseSecureConnection work a little bit longer before it eventually fails ;)
                // thinking previous program point is not computed for J.FieldAccess.
                return transferFieldAccess(pp, t);
            case "J$CompilationUnit":
            case "J$ClassDeclaration":
            case "J$MethodDeclaration":
                // Assert
                // ArrayAccess
                // AssignmentOperation
                // Break
                // Case
                // Continue
                // DoWhileLoop
                // EnumValue
                // EnumValueSet
                // FieldAccess
                // ForeachLoop
                // InstanceOf
                // Label
                // Lambda
                // MemberReference
                // MultiCatch
                // NewArray
                // ArrayDimension
                // NewClass
                // Return
                // Switch
                // Ternary
                // Throw
                // Try
                // TypeCast
                // WhileLoop
            default:
                throw new Error(pp.getValue().getClass().getName());
        }
    }

    public ProgramState<T> transferToIfThenElseBranches(J.If ifThenElse, ProgramState<T> s, String ifThenElseBranch) {
        return s;
    }

    public abstract ProgramState<T> defaultTransfer(Cursor pp, TraversalControl<ProgramState<T>> t);

    public ProgramState<T> transferMethodInvocation(Cursor pp, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, t);
    }

    public ProgramState<T> transferNewClass(Cursor pp, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, t);
    }

    public ProgramState<T> transferIf(Cursor pp, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, t);
    }

    public ProgramState<T> transferIfElse(Cursor pp, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, t);
    }

    public ProgramState<T> transferWhileLoop(Cursor pp, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, t);
    }

    public ProgramState<T> transferForLoop(Cursor pp, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, t);
    }

    public ProgramState<T> transferForLoopControl(Cursor pp, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, t);
    }

    public ProgramState<T> transferBlock(Cursor pp, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, t);
    }

    public ProgramState<T> transferVariableDeclarations(Cursor pp, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, t);
    }

    public ProgramState<T> transferNamedVariable(Cursor pp, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, t);
    }

    public ProgramState<T> transferUnary(Cursor pp, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, t);
    }

    public ProgramState<T> transferBinary(Cursor pp, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, t);
    }

    public ProgramState<T> transferAssignment(Cursor pp, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, t);
    }

    public ProgramState<T> transferAssignmentOperation(Cursor pp, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, t);
    }

    public ProgramState<T> transferParentheses(Cursor pp, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, t);
    }

    public ProgramState<T> transferControlParentheses(Cursor pp, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, t);
    }

    public ProgramState<T> transferLiteral(Cursor pp, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, t);
    }

    public ProgramState<T> transferIdentifier(Cursor pp, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, t);
    }

    public ProgramState<T> transferEmpty(Cursor pp, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, t);
    }

    public ProgramState<T> transferFieldAccess(Cursor pp, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, t);
    }
}
