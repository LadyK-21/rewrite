package org.openrewrite.java.dataflow2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TraversalControl<S extends ProgramState> {

    Set<ProgramPoint> visited = new HashSet<>();

    public void markVisited(ProgramPoint pp) {
        visited.add(pp);
    }

    public boolean isVisited(ProgramPoint pp) {
        boolean result = visited.contains(pp);
        System.out.println("isVisited(" + pp + ") = " + result);
        return result;
    }

//    Map<ProgramPoint, State> map = new HashMap<>();
//
//    public boolean noNeedToVisitAgain(ProgramPoint pp) {
//        State s = map.get(pp);
//        return s != null && s.noNeedToVisitAgain;
//    }
//
//    public S previousResult(ProgramPoint pp) {
//        State<S> s = map.get(pp);
//        assert s != null;
//        return s.value;
//    }
//
//    public boolean alreadyVisited(ProgramPoint pp) {
//        State<S> s = map.get(pp);
//        return s != null;
//    }
//
//    public void setNoNeedToVisitAgain(ProgramPoint pp) {
//        State s = map.get(pp);
//        assert s != null && !s.noNeedToVisitAgain;
//        s.noNeedToVisitAgain = true;
//    }
//
//    public void setPreviousResult(ProgramPoint pp, S result) {
//        assert map.get(pp) == null;
//        State s = new State(result);
//        map.put(pp, s);
//    }
//
//    static class State<S extends ProgramState> {
//        S value = null;
//        boolean noNeedToVisitAgain = false;
//
//        public State(S value) {
//            this.value = value;
//        }
//    }
}
