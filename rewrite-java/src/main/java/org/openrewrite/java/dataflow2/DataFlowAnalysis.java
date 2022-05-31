package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.java.tree.JavaType;

import java.util.*;

public abstract class DataFlowAnalysis<S extends ProgramState> {
    /**
     * @return The *input* program state at given program point.
     */
    public S inputState(Cursor pp, JavaType.Variable storeOfInterest) {
        List<S> outs = new ArrayList<>();
        Collection<Cursor> sources = DataFlowGraph.primitiveSources(pp);
        for (Cursor source : sources) {
            outs.add(outputState(source, storeOfInterest));
        }
        return join(outs);
    }

    public abstract S join(Collection<S> outs);

    @SafeVarargs
    public final S join(S... outs) {
        return join(Arrays.asList(outs));
    }

    public S outputState(Cursor pp, JavaType.Variable storeOfInterest) {
        switch (pp.getValue().getClass().getName().replaceAll("^org.openrewrite.java.tree.", "")) {
            case "J$MethodInvocation":
                return transferMethodInvocation(pp, storeOfInterest);
            case "J$If":
                return transferIf(pp, storeOfInterest);
            case "J$If$Else":
                return transferIfElse(pp, storeOfInterest);
            case "J$WhileLoop":
                return transferWhileLoop(pp, storeOfInterest);
            case "J$ForLoop":
                return transferForLoop(pp, storeOfInterest);
            case "J$ForLoop$Control":
                return transferForLoopControl(pp, storeOfInterest);
            case "J$Block":
                return transferBlock(pp, storeOfInterest);
            case "J$VariableDeclarations":
                return transferVariableDeclarations(pp, storeOfInterest);
            case "J$VariableDeclarations$NamedVariable":
                return transferNamedVariable(pp, storeOfInterest);
            case "J$Unary":
                return transferUnary(pp, storeOfInterest);
            case "J$Binary":
                return transferBinary(pp, storeOfInterest);
            case "J$Assignment":
                return transferAssignment(pp, storeOfInterest);
            case "J$Parentheses":
                return transferParentheses(pp, storeOfInterest);
            case "J$ControlParentheses":
                return transferControlParentheses(pp, storeOfInterest);
            case "J$Literal":
                return transferLiteral(pp, storeOfInterest);
            case "J$Identifier":
                return transferIdentifier(pp, storeOfInterest);
            case "J$Empty":
                return transferEmpty(pp, storeOfInterest);
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

    public abstract S defaultTransfer(Cursor pp, JavaType.Variable storeOfInterest);

    public S transferMethodInvocation(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }

    public S transferIf(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }

    public S transferIfElse(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }

    public S transferWhileLoop(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }

    public S transferForLoop(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }

    public S transferForLoopControl(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }

    public S transferBlock(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }

    public S transferVariableDeclarations(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }

    public S transferNamedVariable(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }

    public S transferUnary(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }

    public S transferBinary(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }

    public S transferAssignment(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }

    public S transferParentheses(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }

    public S transferControlParentheses(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }

    public S transferLiteral(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }

    public S transferIdentifier(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }

    public S transferEmpty(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }
}
