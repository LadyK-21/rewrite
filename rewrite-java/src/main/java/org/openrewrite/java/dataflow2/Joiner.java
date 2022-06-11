package org.openrewrite.java.dataflow2;

import java.util.Arrays;
import java.util.Collection;

public abstract class Joiner<T> {
    // T must be a lattice verifying the bounded scale property
    public abstract T join(Collection<T> values);
    public abstract T lowerBound();

    public T join(T... outs) {
        return join(Arrays.asList(outs));
    }
}
