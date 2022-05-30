package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.util.*;

public abstract class DataFlowAnalysis<S extends ProgramState>
{
    /**
     * @return The *input* program state at given program point.
     */
    public S inputState(Cursor pp, JavaType.Variable storeOfInterest)
    {
        List<S> outs = new ArrayList<>();
        Collection<Cursor> sources = DataFlowGraph.primitiveSources(pp);
        for(Cursor source : sources) {
            outs.add(outputState(source, storeOfInterest));
        }
        return join(outs);
    }

    public abstract S join(Collection<S> outs);

    public S join(S... outs)  { return join(Arrays.asList(outs)); }

    public <S extends ProgramState> S outputState(Cursor pp, JavaType.Variable storeOfInterest)
    {
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
            case "J$CompilationUnit":
            case "J$ClassDeclaration":
            case "J$MethodDeclaration":
            default:
                throw new Error(pp.getClass().getName());
        }
    }

    public abstract <S extends ProgramState> S defaultTransfer(Cursor pp, JavaType.Variable storeOfInterest);

    public <S extends ProgramState> S transferMethodInvocation(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }

    public <S extends ProgramState> S transferIf(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }

    public <S extends ProgramState> S transferIfElse(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }
    public <S extends ProgramState> S transferWhileLoop(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }
    public <S extends ProgramState> S transferForLoop(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }
    public <S extends ProgramState> S transferForLoopControl(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }
    public <S extends ProgramState> S transferBlock(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }
    public <S extends ProgramState> S transferVariableDeclarations(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }
    public <S extends ProgramState> S transferNamedVariable(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }
    public <S extends ProgramState> S transferUnary(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }
    public <S extends ProgramState> S transferBinary(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }
    public <S extends ProgramState> S transferAssignment(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }
    public <S extends ProgramState> S transferParentheses(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }
    public <S extends ProgramState> S transferControlParentheses(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }
    public <S extends ProgramState> S transferLiteral(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }
    public <S extends ProgramState> S transferIdentifier(Cursor pp, JavaType.Variable storeOfInterest) {
        return defaultTransfer(pp, storeOfInterest);
    }
}
