package org.openrewrite.java.dataflow2;

import org.openrewrite.Incubating;

import java.util.Arrays;
import java.util.Collection;

/**
 *  A modal boolean is 'true', 'false', or one of 'NoIdea' (not enough knowledge) or 'Conflict' (contradicting knowledge).
 */
@Incubating(since = "7.24.0")
public enum ModalBoolean {
    // The lattice is ordered as:
    // NoIdea < DefinitelyYes, DefinitelyNo < Conflict
    NoIdea, // The lower bound of the lattice
    True,
    False,
    Conflict; // The upper bound of the lattice

    public static ModalBoolean join(Collection<ModalBoolean> outs) {
        ModalBoolean result = NoIdea;
        for (ModalBoolean out : outs) {
            if(out == NoIdea) continue;
            if ((result == True && out != True) ||
                    (result == False && out != False)) {
                return Conflict;
            } else if (result == Conflict) {
                return result;
            }
            result = out;
        }
        return result;
    }

    public static ModalBoolean join(ModalBoolean... outs) {
        return join(Arrays.asList(outs));
    }
}
