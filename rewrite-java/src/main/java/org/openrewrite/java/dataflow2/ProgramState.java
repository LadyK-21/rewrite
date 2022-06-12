package org.openrewrite.java.dataflow2;

import java.util.Collection;
import java.util.HashMap;

import lombok.*;
import org.openrewrite.Incubating;
import org.openrewrite.java.tree.JavaType;

@Incubating(since = "7.24.0")
@Data
public class ProgramState<T> {

    @With
    LinkedListElement<T> expressionStack;

    @With
    HashMap<JavaType.Variable, T> map;

    public ProgramState() {
        expressionStack  = null;
        map = new HashMap<>();
    }

    private ProgramState(HashMap<JavaType.Variable, T> map) {
        this.expressionStack  = null;
        this.map = map;
    }

    private ProgramState(LinkedListElement expressionStack, HashMap<JavaType.Variable, T> map) {
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

    public ProgramState push(T value) {
        return this.withExpressionStack(new LinkedListElement(expressionStack, value));
    }

    public ProgramState pop() {
        return this.withExpressionStack(expressionStack.previous);
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

    public static <T> ProgramState<T> join(Joiner<T> joiner, Collection<ProgramState<T>> outs) {
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
        return new ProgramState(m);
    }

    @Override
    public String toString() {
        String s = "{";
        for(LinkedListElement e = expressionStack; e != null; e = e.previous) {
            s += " ";
            s += e.value == null ? "null" : e.value.toString();
        }
        s += " |";
        for(JavaType.Variable v : map.keySet()) {
            T t = map.get(v);
            s += " " + v.getName() + " -> " + t;
        }
        s += " }";
        return s;
    }

    public <S extends ProgramState> boolean isEqualTo(S other) {
        return this.map.equals(other.map) && LinkedListElement.isEqual(this.expressionStack, other.expressionStack);
    }
}

@Incubating(since = "7.24.0")
@AllArgsConstructor
class LinkedListElement<T> {
    LinkedListElement previous;
    T value;

    public static <T> boolean isEqual(LinkedListElement<T> a, LinkedListElement<T> b) {
        if(a == b) return true;
        if(a == null) return b == null;
        if(b == null) return a == null;
        return a.value == b.value && isEqual(a.previous, b.previous);
    }
}
