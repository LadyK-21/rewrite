package org.openrewrite.java.dataflow3;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Ternary implements ProgramState<Ternary> {
    Yes(false),
    No(false),
    Unknown(true);

    @Getter
    final boolean terminal;

    @Override
    public Ternary reduce(Ternary out, Ternary result) {
        if ((result == Ternary.Yes && out != Ternary.Yes) ||
                (result == Ternary.No && out != Ternary.No)) {
            return Ternary.Unknown;
        } else if (result == Ternary.Unknown) {
            return result;
        }
        return out;
    }
}
