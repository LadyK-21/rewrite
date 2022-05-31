package org.openrewrite.java.dataflow2;

public enum Ternary implements ProgramState {
    DefinitelyYes,
    DefinitelyNo,
    CantTell
}
