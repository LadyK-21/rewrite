package org.openrewrite.java.dataflow2;

import java.util.Collection;
import java.util.HashMap;

import lombok.*;
import org.openrewrite.java.tree.JavaType;

@Data
public class ProgramState {

    @With
    LinkedListElement expressionStack;

    @With
    HashMap<JavaType.Variable, Ternary> map;

    public ProgramState() {
        expressionStack  = null;
        map = new HashMap<>();
    }

    private ProgramState(HashMap<JavaType.Variable, Ternary> map) {
        this.expressionStack  = null;
        this.map = map;
    }

    private ProgramState(LinkedListElement expressionStack, HashMap<JavaType.Variable, Ternary> map) {
        this.expressionStack  = expressionStack;
        this.map = map;
    }

    public Ternary expr() {
        if(expressionStack == null) {
            return Ternary.Bottom;
        } else {
            return expressionStack.value;
        }
    }

    public ProgramState push(Ternary value) {
        return this.withExpressionStack(new LinkedListElement(expressionStack, value));
    }

    public ProgramState pop() {
        return this.withExpressionStack(expressionStack.previous);
    }

    public Ternary get(JavaType.Variable ident) {
        return map.get(ident);
    }

    public ProgramState set(JavaType.Variable ident, Ternary expr) {
        // TODO : Horrible
        HashMap<JavaType.Variable, Ternary> m = (HashMap<JavaType.Variable, Ternary>) map.clone();
        m.put(ident, expr);
        return this.withMap(m);
    }

    public static ProgramState join(Collection<ProgramState> outs) {
        HashMap<JavaType.Variable, Ternary> m = new HashMap<>();
        for(ProgramState out : outs) {
            for(JavaType.Variable key : out.getMap().keySet()) {
                Ternary v1 = out.getMap().get(key);
                if(!m.containsKey(key)) {
                    m.put(key, v1);
                } else {
                    Ternary v2 = m.get(key);
                    m.put(key, Ternary.join(v1, v2));
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
            Ternary t = map.get(v);
            s += " " + v.getName() + " -> " + t;
        }
        s += " }";
        return s;
    }

    public <S extends ProgramState> boolean isEqualTo(S other) {
        return this.map.equals(other.map) && LinkedListElement.isEqual(this.expressionStack, other.expressionStack);
    }
}

@AllArgsConstructor
class LinkedListElement {
    LinkedListElement previous;
    Ternary value;

    public static boolean isEqual(LinkedListElement a, LinkedListElement b) {
        if(a == b) return true;
        if(a == null) return b == null;
        if(b == null) return a == null;
        return a.value == b.value && isEqual(a.previous, b.previous);
    }
}
