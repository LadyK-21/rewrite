package org.openrewrite.java.dataflow2;

import java.util.Arrays;
import java.util.Collection;

public abstract class Joiner<T> {
    // T must be a lattice verifying the bounded scale property
    // Namely:
    // - elements in T are partially ordered
    // - there exists an upper bound B such that for all values v, v <= B
    // - there does not exist any loop in ordering such as v1 < v2 < .. < vN < v1
    // - join(v1, .., vN) >= vi : the join operation must be monotonically increasing

    public abstract T join(Collection<T> values);
    public final T join(T... outs) {
        return join(Arrays.asList(outs));
    }

    public abstract T lowerBound();

    public abstract T defaultInitialization();
}
