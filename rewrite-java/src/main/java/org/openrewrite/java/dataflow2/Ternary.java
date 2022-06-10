package org.openrewrite.java.dataflow2;

import java.util.Arrays;
import java.util.Collection;

public enum Ternary {
    // Bottom < DefinitelyYes, DefinitelyNo < CantTell
    Bottom,
    DefinitelyYes,
    DefinitelyNo,
    CantTell;

    public static Ternary join(Collection<Ternary> outs) {
        Ternary result = null;
        for (Ternary out : outs) {
            if(out == Bottom) continue;
            if ((result == DefinitelyYes && out != DefinitelyYes) ||
                    (result == DefinitelyNo && out != DefinitelyNo)) {
                return CantTell;
            } else if (result == CantTell) {
                return result;
            }
            result = out;
        }
        return result == null ? Bottom : result;
    }

    public static Ternary join(Ternary... outs) {
        return join(Arrays.asList(outs));
    }
}
