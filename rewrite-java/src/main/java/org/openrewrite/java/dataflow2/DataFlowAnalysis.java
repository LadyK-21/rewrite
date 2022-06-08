package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.java.tree.JavaType;

import java.util.*;

public abstract class DataFlowAnalysis<S extends ProgramState> {
    /**
     * @return The *input* program state at given program point.
     */
    public S inputState(Cursor c, TraversalControl<S> t) {
        ProgramPoint pp = c.getValue();
        t.markVisited(pp);
        List<S> outs = new ArrayList<>();
        Collection<Cursor> sources = DataFlowGraph.previous(c);
        for (Cursor source : sources) {
            if(t.isVisited(source.getValue())) {
                outs.add((S)new ProgramState());
            } else {
                outs.add(outputState(source, t));
            }
        }
        S result = join(outs);
        System.out.println(pp);
        for(S out : outs) {
            System.out.println("      " + out);
        }
        System.out.println("   -> " + result);
        return result;
    }

    public abstract S join(Collection<S> outs);

    @SafeVarargs
    public final S join(S... outs) {
        return join(Arrays.asList(outs));
    }

    public S outputState(Cursor pp, TraversalControl<S> t) {
        switch (pp.getValue().getClass().getName().replaceAll("^org.openrewrite.java.tree.", "")) {
            case "J$MethodInvocation":
                return transferMethodInvocation(pp, t);
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
                throw new Error(pp.getClass().getName());
        }
    }

    public abstract S defaultTransfer(Cursor pp, TraversalControl<S> t);

    public S transferMethodInvocation(Cursor pp, TraversalControl<S> t) {
        return defaultTransfer(pp, t);
    }

    public S transferIf(Cursor pp, TraversalControl<S> t) {
        return defaultTransfer(pp, t);
    }

    public S transferIfElse(Cursor pp, TraversalControl<S> t) {
        return defaultTransfer(pp, t);
    }

    public S transferWhileLoop(Cursor pp, TraversalControl<S> t) {
        return defaultTransfer(pp, t);
    }

    public S transferForLoop(Cursor pp, TraversalControl<S> t) {
        return defaultTransfer(pp, t);
    }

    public S transferForLoopControl(Cursor pp, TraversalControl<S> t) {
        return defaultTransfer(pp, t);
    }

    public S transferBlock(Cursor pp, TraversalControl<S> t) {
        return defaultTransfer(pp, t);
    }

    public S transferVariableDeclarations(Cursor pp, TraversalControl<S> t) {
        return defaultTransfer(pp, t);
    }

    public S transferNamedVariable(Cursor pp, TraversalControl<S> t) {
        return defaultTransfer(pp, t);
    }

    public S transferUnary(Cursor pp, TraversalControl<S> t) {
        return defaultTransfer(pp, t);
    }

    public S transferBinary(Cursor pp, TraversalControl<S> t) {
        return defaultTransfer(pp, t);
    }

    public S transferAssignment(Cursor pp, TraversalControl<S> t) {
        return defaultTransfer(pp, t);
    }

    public S transferAssignmentOperation(Cursor pp, TraversalControl<S> t) {
        return defaultTransfer(pp, t);
    }

    public S transferParentheses(Cursor pp, TraversalControl<S> t) {
        return defaultTransfer(pp, t);
    }

    public S transferControlParentheses(Cursor pp, TraversalControl<S> t) {
        return defaultTransfer(pp, t);
    }

    public S transferLiteral(Cursor pp, TraversalControl<S> t) {
        return defaultTransfer(pp, t);
    }

    public S transferIdentifier(Cursor pp, TraversalControl<S> t) {
        return defaultTransfer(pp, t);
    }

    public S transferEmpty(Cursor pp, TraversalControl<S> t) {
        return defaultTransfer(pp, t);
    }
}
