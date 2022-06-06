package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.java.tree.JavaType;

import java.util.*;

public abstract class DataFlowAnalysis<S extends ProgramState> {
    /**
     * @return The *input* program state at given program point.
     */
    public S inputState(Cursor pp) {
        List<S> outs = new ArrayList<>();
        Collection<Cursor> sources = DataFlowGraph.previous(pp);
        for (Cursor source : sources) {
            outs.add(outputState(source));
        }
        return join(outs);
    }

    public abstract S join(Collection<S> outs);

    @SafeVarargs
    public final S join(S... outs) {
        return join(Arrays.asList(outs));
    }

    public S outputState(Cursor pp) {
        switch (pp.getValue().getClass().getName().replaceAll("^org.openrewrite.java.tree.", "")) {
            case "J$MethodInvocation":
                return transferMethodInvocation(pp);
            case "J$If":
                return transferIf(pp);
            case "J$If$Else":
                return transferIfElse(pp);
            case "J$WhileLoop":
                return transferWhileLoop(pp);
            case "J$ForLoop":
                return transferForLoop(pp);
            case "J$ForLoop$Control":
                return transferForLoopControl(pp);
            case "J$Block":
                return transferBlock(pp);
            case "J$VariableDeclarations":
                return transferVariableDeclarations(pp);
            case "J$VariableDeclarations$NamedVariable":
                return transferNamedVariable(pp);
            case "J$Unary":
                return transferUnary(pp);
            case "J$Binary":
                return transferBinary(pp);
            case "J$Assignment":
                return transferAssignment(pp);
            case "J$Parentheses":
                return transferParentheses(pp);
            case "J$ControlParentheses":
                return transferControlParentheses(pp);
            case "J$Literal":
                return transferLiteral(pp);
            case "J$Identifier":
                return transferIdentifier(pp);
            case "J$Empty":
                return transferEmpty(pp);
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

    public abstract S defaultTransfer(Cursor pp);

    public S transferMethodInvocation(Cursor pp) {
        return defaultTransfer(pp);
    }

    public S transferIf(Cursor pp) {
        return defaultTransfer(pp);
    }

    public S transferIfElse(Cursor pp) {
        return defaultTransfer(pp);
    }

    public S transferWhileLoop(Cursor pp) {
        return defaultTransfer(pp);
    }

    public S transferForLoop(Cursor pp) {
        return defaultTransfer(pp);
    }

    public S transferForLoopControl(Cursor pp) {
        return defaultTransfer(pp);
    }

    public S transferBlock(Cursor pp) {
        return defaultTransfer(pp);
    }

    public S transferVariableDeclarations(Cursor pp) {
        return defaultTransfer(pp);
    }

    public S transferNamedVariable(Cursor pp) {
        return defaultTransfer(pp);
    }

    public S transferUnary(Cursor pp) {
        return defaultTransfer(pp);
    }

    public S transferBinary(Cursor pp) {
        return defaultTransfer(pp);
    }

    public S transferAssignment(Cursor pp) {
        return defaultTransfer(pp);
    }

    public S transferAssignmentOperation(Cursor pp) {
        return defaultTransfer(pp);
    }

    public S transferParentheses(Cursor pp) {
        return defaultTransfer(pp);
    }

    public S transferControlParentheses(Cursor pp) {
        return defaultTransfer(pp);
    }

    public S transferLiteral(Cursor pp) {
        return defaultTransfer(pp);
    }

    public S transferIdentifier(Cursor pp) {
        return defaultTransfer(pp);
    }

    public S transferEmpty(Cursor pp) {
        return defaultTransfer(pp);
    }
}
