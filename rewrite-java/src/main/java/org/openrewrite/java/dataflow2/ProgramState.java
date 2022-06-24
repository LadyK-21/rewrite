package org.openrewrite.java.dataflow2;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import lombok.*;
import org.openrewrite.Incubating;
import org.openrewrite.java.tree.JavaType;

@Incubating(since = "7.25.0")
@Data
@EqualsAndHashCode
public class ProgramState<T> {

    @With
    LinkedListElement<T> expressionStack;

    @With
    HashMap<JavaType.Variable, T> map;

    public ProgramState(T lowerBound) {
        expressionStack  = new LinkedListElement<>(null, lowerBound);
        map = new HashMap<>();
    }

    private ProgramState(HashMap<JavaType.Variable, T> map) {
        this.expressionStack  = null;
        this.map = map;
    }

    private ProgramState(LinkedListElement<T> expressionStack, HashMap<JavaType.Variable, T> map) {
        this.expressionStack  = expressionStack;
        this.map = map;
    }

    public T expr() {
        if(expressionStack == null) {
            // If this happens, it means that some expression didn't push its value
            throw new NullPointerException("Empty expression stack");
        }
        return expressionStack.value;
    }

    public T expr(int depth) {
        LinkedListElement<T> s = expressionStack;
        for(int i=0; i<depth; i++) {
            s = s.previous;
        }
        return s.value;
    }

    public ProgramState<T> push(T value) {
        return this.withExpressionStack(new LinkedListElement<>(expressionStack, value));
    }

    public ProgramState<T> pop() {
        return withExpressionStack(expressionStack.previous);
    }

    public T get(JavaType.Variable ident) {
        return map.get(ident);
    }

    public ProgramState<T> set(JavaType.Variable ident, T expr) {
        // TODO : Horrible
        HashMap<JavaType.Variable, T> m = (HashMap<JavaType.Variable, T>) map.clone();
        m.put(ident, expr);
        return this.withMap(m);
    }

    public static <T> ProgramState<T> join(Joiner<T> joiner, List<ProgramState<T>> outs) {
        if(outs.size() == 1) {
            return outs.get(0);
        }
        HashMap<JavaType.Variable, T> m = new HashMap<>();
        for(ProgramState<T> out : outs) {
            for(JavaType.Variable key : out.getMap().keySet()) {
                T v1 = out.getMap().get(key);
                if(!m.containsKey(key)) {
                    m.put(key, v1);
                } else {
                    T v2 = m.get(key);
                    m.put(key, joiner.join(v1, v2));
                }
            }
        }
        // ... combine stacks : must have the same length ...
        if(outs.size() > 0) {
            int len = LinkedListElement.length(outs.get(0).expressionStack);
            for (int i = 1; i < outs.size(); i++) {
                assert len == LinkedListElement.length(outs.get(i).expressionStack);
            }

            assert len == 0; // TODO        }
        }
        return new ProgramState<>(new LinkedListElement<>(null, joiner.lowerBound()), m);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("{");
        for(LinkedListElement<T> e = expressionStack; e != null; e = e.previous) {
            s.append(" ");
            s.append(e.value == null ? "null" : e.value.toString());
        }
        s.append(" |");
        for(JavaType.Variable v : map.keySet()) {
            T t = map.get(v);
            s.append(" ").append(v.getName()).append(" -> ").append(t);
        }
        s.append(" }");
        return s.toString();
    }

    public <S extends ProgramState<T>> boolean isEqualTo(S other) {
        return map.equals(other.map) && LinkedListElement.isEqual(expressionStack, other.expressionStack);
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    static class LinkedListElement<T> {
        LinkedListElement<T> previous;
        T value;

        public static <T> boolean isEqual(LinkedListElement<T> a, LinkedListElement<T> b) {
            if(a == b) return true;
            if (a == null ^ b == null) {
                return false;
            }
            return a.value == b.value && isEqual(a.previous, b.previous);
        }

        public static <T> int length(LinkedListElement<T> expressionStack) {
            if(expressionStack == null) {
                return 0;
            } else {
                return 1 + length(expressionStack.previous);
            }
        }
    }
}
