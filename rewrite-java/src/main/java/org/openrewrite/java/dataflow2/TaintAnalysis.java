package org.openrewrite.java.dataflow2;

import org.openrewrite.Cursor;
import org.openrewrite.java.tree.JavaType;

import java.util.Collection;

public abstract class TaintAnalysis extends DataFlowAnalysis<Ternary> {

    @Override
    public Ternary join(Collection<Ternary> outs) {
        return Ternary.join(outs);
    }

    public Ternary defaultTransfer(Cursor c, JavaType.Variable storeOfInterest) {
        return inputState(c, storeOfInterest);
    }
}
