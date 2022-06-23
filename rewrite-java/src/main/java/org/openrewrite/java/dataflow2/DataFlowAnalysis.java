/*
 * Copyright 2022 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

//    PriorityQueue<Cursor> workList = new PriorityQueue<>(new Comparator<Cursor>() {
//        @Override
//        public int compare(Cursor c1, Cursor c2) {
//            ProgramPoint p1 = c1.getValue();
//            ProgramPoint p2 = c2.getValue();
//            // least element is first in the list
//            if(nextsTransitiveClosure.get(p1) != null && nextsTransitiveClosure.get(p1).contains(p2)) {
//                return -1;
//            } else if(nextsTransitiveClosure.get(p2) != null && nextsTransitiveClosure.get(p2).contains(p1)) {
//                return 1;
//            } else {
//                return 0;
//            }
//        }
//    });

    Deque<Cursor> workList = new ArrayDeque<>();

    MultiMap<ProgramPoint, ProgramPoint> nexts = new MultiMap<>();

//    MultiMap<ProgramPoint, ProgramPoint> nextsTransitiveClosure = new MultiMap<>();

    public DataFlowAnalysis(DataFlowGraph dfg, Joiner<T> joiner) {
        this.dfg = dfg;
        this.joiner = joiner;
    }

    public void doAnalysis(Cursor from) {

        initNexts(from);
        //initClosure();
        visited = new HashSet<>();
        initWorkList(from);

        while(!workList.isEmpty()) {
            System.out.println(workList.size());

            Cursor c = workList.remove();
            ProgramPoint pp = c.getValue();

            System.out.println(pp.getClass().getSimpleName() + "   " + Utils.print(c));

//            if("s = null".equals(Utils.print(c))) {
//                System.out.println();
//            }

            ProgramState<T> previousState = analysisOrNull2(pp);

            ProgramState<T> inputState = inputState2(c, null);
            ProgramState<T> newState = transfer(c, inputState, null); // linearTransfer(c, null);

            System.out.println(inputState + "  ->  " + newState);

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
                }
            }
        }
    }

//    ProgramState<T> linearTransfer(Cursor c, TraversalControl<ProgramState<T>> t) {
//        List<Cursor> prev = dfg.previous(c);
//        if(prev.size() == 1) {
//            return linearTransfer(prev.get(0), t);
//        } else {
//            return transfer(c, t);
//        }
//    }

    void initNexts(Cursor c) {
        ProgramPoint to = c.getValue();
        if(visited.contains(to)) return;
        visited.add(to);
        cursors.put(to, c);

        List<Cursor> sources = dfg.previous(c);

//        while(sources.size() == 1) {
//            sources = dfg.previous(sources.get(0));
//        }

        for (Cursor source : sources) {
            ProgramPoint from = source.getValue();
            // There is a DFG edge from->to
            // The analysis at 'to' depends on the analysis at 'from'
            nexts.add(from, to);
            initNexts(source);
        }
    }

//    void initClosure() {
//        for(ProgramPoint from : nexts.keySet()) {
//            addTransitiveClosure(from, nexts.get(from));
//        }
//    }
//
//    void addTransitiveClosure(ProgramPoint from, Collection<ProgramPoint> tos) {
//        ArrayList<ProgramPoint> trans = nextsTransitiveClosure.get(from);
//        if(tos != null) {
//            for(ProgramPoint to : tos) {
//                if(trans == null) {
//                    trans = new ArrayList<>();
//                    nextsTransitiveClosure.put(from, trans);
//                }
//                if(!trans.contains(to)) {
//                    nextsTransitiveClosure.add(from, to);
//                    addTransitiveClosure(from, nexts.get(to));
//                }
//            }
//        }
//    }

    void initWorkList(Cursor c) {
        ProgramPoint to = c.getValue();
        if(visited.contains(to)) return;
        visited.add(to);
        workList.addFirst(c);

        List<Cursor> sources = dfg.previous(c);

//        while(sources.size() == 1) {
//            sources = dfg.previous(sources.get(0));
//        }

        for (Cursor source : sources) {
            ProgramPoint from = source.getValue();
            initWorkList(source);
        }
    }

    public ProgramState<T> analysis2(ProgramPoint p) {
        ProgramState<T> res = analysis.get(p);
        if(res == null) {
            res = new ProgramState<>(joiner.lowerBound());
            analysis.put(p, res);
        }
        return res;
    }

    public ProgramState<T> analysisOrNull2(ProgramPoint p) {
        ProgramState<T> res = analysis.get(p);
        return res;
    }
    public ProgramState<T> analysis2(Cursor c) {
        return analysis2((ProgramPoint)c.getValue());
    }

    public ProgramState<T> inputState2(Cursor c, TraversalControl<ProgramState<T>> t) {
        ProgramPoint pp = c.getValue();

        List<ProgramState<T>> outs = new ArrayList<>();
        Collection<Cursor> sources = dfg.previous(c);
        for (Cursor source : sources) {
            // Since program points are represented by cursors with a tree node value,
            // it is impossible to add program points when there is no corresponding tree node.
            // To work around this limitation, we use cursor messages to express that a given
            // edge goes through a virtual program point.

            if (source.getMessage("ifThenElseBranch") != null) {
                J.If ifThenElse = source.firstEnclosing(J.If.class);
                ProgramState<T> s1 = analysis2(source); // outputState(source, t, pp);
                ProgramState<T> s2 = transferToIfThenElseBranches(ifThenElse, s1, source.getMessage("ifThenElseBranch"));
                outs.add(s2);
            } else {
                outs.add(analysis2(source)); // outputState(source, t, pp));
            }
        }
        ProgramState<T> result = join(outs);
        return result;
    }

    public abstract ProgramState<T> join(List<ProgramState<T>> outs);

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

    public ProgramState<T> transfer(Cursor pp, ProgramState inputState, TraversalControl<ProgramState<T>> t) {
        switch (pp.getValue().getClass().getName().replaceAll("^org.openrewrite.java.tree.", "")) {
            case "J$MethodInvocation":
                return transferMethodInvocation(pp, inputState, t);
            case "J$ArrayAccess":
                return transferArrayAccess(pp, inputState, t);
            case "J$Assert":
                return transferAssert(pp, inputState, t);
            case "J$NewClass":
                return transferNewClass(pp, inputState, t);
            case "J$If":
                return transferIf(pp, inputState, t);
            case "J$If$Else":
                return transferIfElse(pp, inputState, t);
            case "J$WhileLoop":
                return transferWhileLoop(pp, inputState, t);
            case "J$ForLoop":
                return transferForLoop(pp, inputState, t);
            case "J$ForLoop$Control":
                return transferForLoopControl(pp, inputState, t);
            case "J$Block":
                return transferBlock(pp, inputState, t);
            case "J$VariableDeclarations":
                return transferVariableDeclarations(pp, inputState, t);
            case "J$VariableDeclarations$NamedVariable":
                return transferNamedVariable(pp, inputState, t);
            case "J$Unary":
                return transferUnary(pp, inputState, t);
            case "J$Binary":
                return transferBinary(pp, inputState, t);
            case "J$Assignment":
                return transferAssignment(pp, inputState, t);
            case "J$Parentheses":
                return transferParentheses(pp, inputState, t);
            case "J$ControlParentheses":
                return transferControlParentheses(pp, inputState, t);
            case "J$Literal":
                return transferLiteral(pp, inputState, t);
            case "J$Identifier":
                return transferIdentifier(pp, inputState, t);
            case "J$Empty":
                return transferEmpty(pp, inputState, t);
            case "J$FieldAccess":
                return transferFieldAccess(pp, inputState, t);
            case "J$CompilationUnit":
            case "J$ClassDeclaration":
            case "J$MethodDeclaration":

                // AssignmentOperation x+=1
                // EnumValue
                // EnumValueSet
                // ForeachLoop
                // DoWhileLoop
                // InstanceOf like binary
                // NewArray like new class with dimension
                // Ternary (? :)
                // TypeCast like parenthesis evaluate expression not type
                // MemberReference

                // Switch
                // Case
                // Lambda
                // Break
                // Continue
                // Label
                // Return
                // Throw
                // Try
                // MultiCatch

            default:
                throw new Error(pp.getValue().getClass().getName());
        }
    }

    public ProgramState<T> transferAssert(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }

    public ProgramState<T> transferToIfThenElseBranches(J.If ifThenElse, ProgramState<T> s, String ifThenElseBranch) {
        return s;
    }

    public abstract ProgramState<T> defaultTransfer(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t);

    public ProgramState<T> transferMethodInvocation(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }

    public ProgramState<T> transferArrayAccess(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }
    public ProgramState<T> transferNewClass(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }

    public ProgramState<T> transferIf(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }

    public ProgramState<T> transferIfElse(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }

    public ProgramState<T> transferWhileLoop(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }

    public ProgramState<T> transferForLoop(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }

    public ProgramState<T> transferForLoopControl(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }

    public ProgramState<T> transferBlock(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }

    public ProgramState<T> transferVariableDeclarations(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }

    public ProgramState<T> transferNamedVariable(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }

    public ProgramState<T> transferUnary(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }

    public ProgramState<T> transferBinary(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }

    public ProgramState<T> transferAssignment(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }

    public ProgramState<T> transferAssignmentOperation(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }

    public ProgramState<T> transferParentheses(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }

    public ProgramState<T> transferControlParentheses(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }

    public ProgramState<T> transferLiteral(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }

    public ProgramState<T> transferIdentifier(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }

    public ProgramState<T> transferEmpty(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }

    public ProgramState<T> transferFieldAccess(Cursor pp, ProgramState<T> inputState, TraversalControl<ProgramState<T>> t) {
        return defaultTransfer(pp, inputState, t);
    }
}
