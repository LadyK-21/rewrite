package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.java.tree.JavaType;

import java.util.*;

public abstract class DataFlowAnalysis<S extends ProgramState> {
    /**
     * @return The *input* program state at given program point.
     */
    public S inputState(Cursor pp, ProgramState state) {
        List<S> outs = new ArrayList<>();
        Collection<Cursor> sources = DataFlowGraph.primitiveSources(pp);
        for (Cursor source : sources) {
            outs.add(outputState(source, state));
        }
        return join(outs);
    }

    public abstract S join(Collection<S> outs);

    @SafeVarargs
    public final S join(S... outs) {
        return join(Arrays.asList(outs));
    }

    public S outputState(Cursor pp, ProgramState state) {
        switch (pp.getValue().getClass().getName().replaceAll("^org.openrewrite.java.tree.", "")) {
            case "J$MethodInvocation":
                return transferMethodInvocation(pp, state);
            case "J$If":
                return transferIf(pp, state);
            case "J$If$Else":
                return transferIfElse(pp, state);
            case "J$WhileLoop":
                return transferWhileLoop(pp, state);
            case "J$ForLoop":
                return transferForLoop(pp, state);
            case "J$ForLoop$Control":
                return transferForLoopControl(pp, state);
            case "J$Block":
                return transferBlock(pp, state);
            case "J$VariableDeclarations":
                return transferVariableDeclarations(pp, state);
            case "J$VariableDeclarations$NamedVariable":
                return transferNamedVariable(pp, state);
            case "J$Unary":
                return transferUnary(pp, state);
            case "J$Binary":
                return transferBinary(pp, state);
            case "J$Assignment":
                return transferAssignment(pp, state);
            case "J$Parentheses":
                return transferParentheses(pp, state);
            case "J$ControlParentheses":
                return transferControlParentheses(pp, state);
            case "J$Literal":
                return transferLiteral(pp, state);
            case "J$Identifier":
                return transferIdentifier(pp, state);
            case "J$Empty":
                return transferEmpty(pp, state);
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

    public abstract S defaultTransfer(Cursor pp, ProgramState state);

    public S transferMethodInvocation(Cursor pp, ProgramState state) {
        return defaultTransfer(pp, state);
    }

    public S transferIf(Cursor pp, ProgramState state) {
        return defaultTransfer(pp, state);
    }

    public S transferIfElse(Cursor pp, ProgramState state) {
        return defaultTransfer(pp, state);
    }

    public S transferWhileLoop(Cursor pp, ProgramState state) {
        return defaultTransfer(pp, state);
    }

    public S transferForLoop(Cursor pp, ProgramState state) {
        return defaultTransfer(pp, state);
    }

    public S transferForLoopControl(Cursor pp, ProgramState state) {
        return defaultTransfer(pp, state);
    }

    public S transferBlock(Cursor pp, ProgramState state) {
        return defaultTransfer(pp, state);
    }

    public S transferVariableDeclarations(Cursor pp, ProgramState state) {
        return defaultTransfer(pp, state);
    }

    public S transferNamedVariable(Cursor pp, ProgramState state) {
        return defaultTransfer(pp, state);
    }

    public S transferUnary(Cursor pp, ProgramState state) {
        return defaultTransfer(pp, state);
    }

    public S transferBinary(Cursor pp, ProgramState state) {
        return defaultTransfer(pp, state);
    }

    public S transferAssignment(Cursor pp, ProgramState state) {
        return defaultTransfer(pp, state);
    }

    public S transferAssignmentOperation(Cursor pp, ProgramState state) {
        return defaultTransfer(pp, state);
    }

    public S transferParentheses(Cursor pp, ProgramState state) {
        return defaultTransfer(pp, state);
    }

    public S transferControlParentheses(Cursor pp, ProgramState state) {
        return defaultTransfer(pp, state);
    }

    public S transferLiteral(Cursor pp, ProgramState state) {
        return defaultTransfer(pp, state);
    }

    public S transferIdentifier(Cursor pp, ProgramState state) {
        return defaultTransfer(pp, state);
    }

    public S transferEmpty(Cursor pp, ProgramState state) {
        return defaultTransfer(pp, state);
    }
}
