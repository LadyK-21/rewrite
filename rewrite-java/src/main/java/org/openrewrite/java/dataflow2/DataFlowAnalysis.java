package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.Incubating;
import org.openrewrite.java.tree.J;

import java.util.*;

import static org.openrewrite.java.dataflow2.ProgramPoint.ENTRY;

@Incubating(since = "7.25.0")
public abstract class DataFlowAnalysis<T> {

    protected final Joiner<T> joiner;

    final DataFlowGraph dfg;

    // The state AFTER given program point
    Map<ProgramPoint, ProgramState<T>> analysis = new HashMap<>();

    Set<ProgramPoint> visited = new HashSet();

    Map<ProgramPoint, Cursor> cursors = new HashMap<>();

    PriorityQueue<Cursor> workList = new PriorityQueue<>(new Comparator<Cursor>() {
        @Override
        public int compare(Cursor c1, Cursor c2) {
            ProgramPoint p1 = c1.getValue();
            ProgramPoint p2 = c2.getValue();
            // least element is first in the list
            if(nextsTransitiveClosure.get(p1) != null && nextsTransitiveClosure.get(p1).contains(p2)) {
                return -1;
            } else if(nextsTransitiveClosure.get(p2) != null && nextsTransitiveClosure.get(p2).contains(p1)) {
                return 1;
            } else {
                return 0;
            }
        }
    });

    MultiMap<ProgramPoint, ProgramPoint> nexts = new MultiMap<>();
    MultiMap<ProgramPoint, ProgramPoint> nextsTransitiveClosure = new MultiMap<>();

    public DataFlowAnalysis(DataFlowGraph dfg, Joiner<T> joiner) {
        this.dfg = dfg;
        this.joiner = joiner;
    }

    public void doAnalysis(Cursor from) {

        initNexts(from);
        initClosure();
        visited = new HashSet<>();
        initWorkList(from);

        while(!workList.isEmpty()) {
            System.out.println(workList.size());

            Cursor c = workList.remove();
            ProgramPoint pp = c.getValue();

            System.out.println(Utils.print(c));

            if("(s = \"a\")".equals(Utils.print(c))) {
                System.out.println();
            }

            ProgramState<T> previousState = analysisOrNull(pp);
            ProgramState<T> newState = transfer(c, null);


            System.out.println(previousState + "  ->  " + newState);

            if(previousState == null) {
                analysis.put(pp, newState);
            } else if(!previousState.equals(newState)) {
                analysis.put(pp, newState);
                ArrayList<ProgramPoint> nn = nexts.get(pp);
                if(nn != null) {
                    for (ProgramPoint p : nn) {
                        Cursor next = cursors.get(p);
                        workList.add(next);
                    }
                }            }
        }
    }

    void initNexts(Cursor c) {
        ProgramPoint to = c.getValue();
        if(visited.contains(to)) return;
        visited.add(to);
        cursors.put(to, c);

        Collection<Cursor> sources = dfg.previous(c);
        for (Cursor source : sources) {
            ProgramPoint from = source.getValue();
            // There is a DFG edge from->to
            // The analysis at 'to' depends on the analysis at 'from'
            nexts.add(from, to);
            initNexts(source);
        }
    }

    void initClosure() {
        for(ProgramPoint from : nexts.keySet()) {
            addTransitiveClosure(from, nexts.get(from));
        }
    }

    void addTransitiveClosure(ProgramPoint from, Collection<ProgramPoint> tos) {
        ArrayList<ProgramPoint> trans = nextsTransitiveClosure.get(from);
        if(tos != null) {
            for(ProgramPoint to : tos) {
                if(trans == null) {
                    trans = new ArrayList<>();
                    nextsTransitiveClosure.put(from, trans);
                }
                if(!trans.contains(to)) {
                    nextsTransitiveClosure.add(from, to);
                    addTransitiveClosure(from, nexts.get(to));
                }
            }
        }
    }

    void initWorkList(Cursor c) {
        ProgramPoint to = c.getValue();
        if(visited.contains(to)) return;
        visited.add(to);
        workList.add(c);

        Collection<Cursor> sources = dfg.previous(c);
        for (Cursor source : sources) {
            ProgramPoint from = source.getValue();
            initWorkList(source);
        }
    }

    public ProgramState<T> analysis(ProgramPoint p) {
        ProgramState<T> res = analysis.get(p);
        if(res == null) {
            res = new ProgramState<>(joiner.lowerBound());
            analysis.put(p, res);
        }
        return res;
    }

    public ProgramState<T> analysisOrNull(ProgramPoint p) {
        ProgramState<T> res = analysis.get(p);
        return res;
    }
    public ProgramState<T> analysis(Cursor c) {
        return analysis((ProgramPoint)c.getValue());
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
                ProgramState<T> s1 = analysis(source); // outputState(source, t, pp);
                ProgramState<T> s2 = transferToIfThenElseBranches(ifThenElse, s1, source.getMessage("ifThenElseBranch"));
                outs.add(s2);
            } else {
                outs.add(analysis(source)); // outputState(source, t, pp));
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

//    public Map<ProgramPoint, ProgramState<T>> visited = new HashMap<>();
//
//    public ProgramState<T> outputState(Cursor pp, TraversalControl<ProgramState<T>> t, ProgramPoint depend) {
//        // 'depend' depends on 'outputState(pp)'
//        ProgramState<T> p = visited.get(pp.getValue());
//        if(p != null) {
//            return p;
//        } else {
//            visited.put(pp.getValue(), new ProgramState<>());
//            p = transfer(pp, t);
//            visited.put(pp.getValue(), p);
//            return p;
//        }
//    }

    public ProgramState<T> transfer(Cursor pp, TraversalControl<ProgramState<T>> t) {
        switch (pp.getValue().getClass().getName().replaceAll("^org.openrewrite.java.tree.", "")) {
            case "J$MethodInvocation":
                return transferMethodInvocation(pp, t);
            case "J$ArrayAccess":
                return transferArrayAccess(pp, t);
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

    public ProgramState<T> transferArrayAccess(Cursor pp, TraversalControl<ProgramState<T>> t) {
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
