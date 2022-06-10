package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.java.tree.J;

import java.util.*;

public abstract class DataFlowAnalysis<S extends ProgramState> {

    final DataFlowGraph dfg;
    Map<Cursor, S> analysis = new HashMap<>();
    WorkList<Cursor> workList = new WorkList();

    public DataFlowAnalysis(DataFlowGraph dfg) {
        this.dfg = dfg;
    }

    public void analyze( Cursor init, TraversalControl<S> t) {

        initialize(init, t);

        // iterate
        while(!workList.isEmpty()) {
            Cursor c = workList.extract();
            S currentState = outputState(c, t); // lookup
            S newState = transfer(c, t); // computation
            if(!newState.isEqualTo(currentState)) {
                analysis.put(c, newState);
                workList.insertAll(dfg.next(c));
            }
        }
    }

    private void initialize(Cursor c, TraversalControl<S> t) {
        workList.insert(c);
        Collection<Cursor> sources = dependencies(c, t);
        for (Cursor source : sources) {
            if(!workList.contains(source)) {
                initialize(source, t);
            }
        }
    }

    public S outputState(Cursor c, TraversalControl<S> t) {
        return outputState(c, t, 0);
    }

    public S outputState(Cursor c, TraversalControl<S> t, int exprCount) {
        ProgramPoint pp = c.getValue();
        S result = analysis.get(c);
        if(result == null) {
            result = (S) new ProgramState();
            analysis.put(c, result);
        }
        int actual = 0;
        for(LinkedListElement e = result.expressionStack; e != null; e = e.previous) actual++;
        while(exprCount > actual) {
            LinkedListElement e = new LinkedListElement(result.expressionStack, Ternary.Bottom);
            result.expressionStack = e;
            exprCount--;
        }
        return result;
    }

    public S inputState(Cursor c, TraversalControl<S> t) {
        ProgramPoint pp = c.getValue();
        List<S> outs = new ArrayList<>();
        Collection<Cursor> sources = dfg.previous(c);
        for (Cursor source : sources) {
            outs.add(outputState(source, t, 0));
        }
        S result = join(outs);
//        System.out.println(pp);
//        for(S out : outs) {
//            System.out.println("      " + out);
//        }
//        System.out.println("   -> " + result);
        return result;
    }

    public abstract S join(Collection<S> outs);

    @SafeVarargs
    public final S join(S... outs) {
        return join(Arrays.asList(outs));
    }

    public S transfer(Cursor pp, TraversalControl<S> t) {
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

    public Collection<Cursor> dependencies(Cursor pp, TraversalControl<S> t) {
        switch (pp.getValue().getClass().getName().replaceAll("^org.openrewrite.java.tree.", "")) {
            case "J$MethodInvocation":
                return dependenciesMethodInvocation(pp, t);
            case "J$If":
                return dependenciesIf(pp, t);
            case "J$If$Else":
                return dependenciesIfElse(pp, t);
            case "J$WhileLoop":
                return dependenciesWhileLoop(pp, t);
            case "J$ForLoop":
                return dependenciesForLoop(pp, t);
            case "J$ForLoop$Control":
                return dependenciesForLoopControl(pp, t);
            case "J$Block":
                return dependenciesBlock(pp, t);
            case "J$VariableDeclarations":
                return dependenciesVariableDeclarations(pp, t);
            case "J$VariableDeclarations$NamedVariable":
                return dependenciesNamedVariable(pp, t);
            case "J$Unary":
                return dependenciesUnary(pp, t);
            case "J$Binary":
                return dependenciesBinary(pp, t);
            case "J$Assignment":
                return dependenciesAssignment(pp, t);
            case "J$Parentheses":
                return dependenciesParentheses(pp, t);
            case "J$ControlParentheses":
                return dependenciesControlParentheses(pp, t);
            case "J$Literal":
                return dependenciesLiteral(pp, t);
            case "J$Identifier":
                return dependenciesIdentifier(pp, t);
            case "J$Empty":
                return dependenciesEmpty(pp, t);
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


    public Collection<Cursor> defaultDependencies(Cursor pp, TraversalControl<S> t) {
        return dfg.previous(pp);
    }

    public Collection<Cursor> dependenciesMethodInvocation(Cursor pp, TraversalControl<S> t) {
        return defaultDependencies(pp, t);
    }

    public Collection<Cursor> dependenciesIf(Cursor pp, TraversalControl<S> t) {
        return defaultDependencies(pp, t);
    }

    public Collection<Cursor> dependenciesIfElse(Cursor pp, TraversalControl<S> t) {
        return defaultDependencies(pp, t);
    }

    public Collection<Cursor> dependenciesWhileLoop(Cursor pp, TraversalControl<S> t) {
        return defaultDependencies(pp, t);
    }

    public Collection<Cursor> dependenciesForLoop(Cursor pp, TraversalControl<S> t) {
        return defaultDependencies(pp, t);
    }

    public Collection<Cursor> dependenciesForLoopControl(Cursor pp, TraversalControl<S> t) {
        return defaultDependencies(pp, t);
    }

    public Collection<Cursor> dependenciesBlock(Cursor pp, TraversalControl<S> t) {
        return defaultDependencies(pp, t);
    }

    public Collection<Cursor> dependenciesVariableDeclarations(Cursor pp, TraversalControl<S> t) {
        return defaultDependencies(pp, t);
    }

    public Collection<Cursor> dependenciesNamedVariable(Cursor pp, TraversalControl<S> t) {
        return defaultDependencies(pp, t);
    }

    public Collection<Cursor> dependenciesUnary(Cursor pp, TraversalControl<S> t) {
        return defaultDependencies(pp, t);
    }

    public Collection<Cursor> dependenciesBinary(Cursor pp, TraversalControl<S> t) {
        return defaultDependencies(pp, t);
    }

    public Collection<Cursor> dependenciesAssignment(Cursor pp, TraversalControl<S> t) {
        return defaultDependencies(pp, t);
    }

    public Collection<Cursor> dependenciesAssignmentOperation(Cursor pp, TraversalControl<S> t) {
        return defaultDependencies(pp, t);
    }

    public Collection<Cursor> dependenciesParentheses(Cursor pp, TraversalControl<S> t) {
        return defaultDependencies(pp, t);
    }

    public Collection<Cursor> dependenciesControlParentheses(Cursor pp, TraversalControl<S> t) {
        return defaultDependencies(pp, t);
    }

    public Collection<Cursor> dependenciesLiteral(Cursor pp, TraversalControl<S> t) {
        return defaultDependencies(pp, t);
    }

    public Collection<Cursor> dependenciesIdentifier(Cursor pp, TraversalControl<S> t) {
        return defaultDependencies(pp, t);
    }

    public Collection<Cursor> dependenciesEmpty(Cursor pp, TraversalControl<S> t) {
        return defaultDependencies(pp, t);
    }


    static class WorkList<E> {

        private Queue<E> q = new ArrayDeque<E>();

        public void insert(E e) {
            q.add(e);
        }

        public void insertAll(Collection<E> ee) {
            for(E e : ee) insert(e);
        }

        public boolean isEmpty() {
            return q.isEmpty();
        }

        public E extract() {
            return q.remove();
        }

        public boolean contains(E e) {
            return q.contains(e);
        }
    }
}
