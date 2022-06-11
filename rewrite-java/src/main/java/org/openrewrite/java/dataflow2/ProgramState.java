package org.openrewrite.java.dataflow2;

import java.util.Collection;
import java.util.HashMap;

import lombok.*;
import org.openrewrite.Incubating;
import org.openrewrite.java.tree.JavaType;

@Incubating(since = "7.24.0")
@Data
public class ProgramState {

    @With
    LinkedListElement expressionStack;

    @With
    HashMap<JavaType.Variable, ModalBoolean> map;

    public ProgramState() {
        expressionStack  = null;
        map = new HashMap<>();
    }

    private ProgramState(HashMap<JavaType.Variable, ModalBoolean> map) {
        this.expressionStack  = null;
        this.map = map;
    }

    private ProgramState(LinkedListElement expressionStack, HashMap<JavaType.Variable, ModalBoolean> map) {
        this.expressionStack  = expressionStack;
        this.map = map;
    }

    public ModalBoolean expr() {
        if(expressionStack == null) {
            return ModalBoolean.NoIdea;
        } else {
            return expressionStack.value;
        }
    }

    public ProgramState push(ModalBoolean value) {
        return this.withExpressionStack(new LinkedListElement(expressionStack, value));
    }

    public ProgramState pop() {
        return this.withExpressionStack(expressionStack.previous);
    }

    public ModalBoolean get(JavaType.Variable ident) {
        return map.get(ident);
    }

    public ProgramState set(JavaType.Variable ident, ModalBoolean expr) {
        // TODO : Horrible
        HashMap<JavaType.Variable, ModalBoolean> m = (HashMap<JavaType.Variable, ModalBoolean>) map.clone();
        m.put(ident, expr);
        return this.withMap(m);
    }

    public static ProgramState join(Collection<ProgramState> outs) {
        HashMap<JavaType.Variable, ModalBoolean> m = new HashMap<>();
        for(ProgramState out : outs) {
            for(JavaType.Variable key : out.getMap().keySet()) {
                ModalBoolean v1 = out.getMap().get(key);
                if(!m.containsKey(key)) {
                    m.put(key, v1);
                } else {
                    ModalBoolean v2 = m.get(key);
                    m.put(key, ModalBoolean.join(v1, v2));
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
            ModalBoolean t = map.get(v);
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
class LinkedListElement {
    LinkedListElement previous;
    ModalBoolean value;

    public static boolean isEqual(LinkedListElement a, LinkedListElement b) {
        if(a == b) return true;
        if(a == null) return b == null;
        if(b == null) return a == null;
        return a.value == b.value && isEqual(a.previous, b.previous);
    }
}
