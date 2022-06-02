package org.openrewrite.java.dataflow2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.openrewrite.java.tree.J;
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

    private ProgramState(LinkedListElement expressionStack, HashMap<JavaType.Variable, Ternary> map) {
        this.expressionStack  = expressionStack;
        this.map = map;
    }

    public Ternary expr() {
        return expressionStack.value;
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
        // TODO
        return new ProgramState(null, new HashMap<>());
    }
}

@AllArgsConstructor
class LinkedListElement {
    LinkedListElement previous;
    Ternary value;
}
