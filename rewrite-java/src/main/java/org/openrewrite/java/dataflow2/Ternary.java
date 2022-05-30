package org.openrewrite.java.dataflow2;

public class Ternary extends ProgramState {
    private Ternary() {}
    public static final Ternary DefinitelyYes = new Ternary() {
        @Override public String toString() { return "DefinitelyYes"; }
    };
    public static final Ternary DefinitelyNo = new Ternary() {
        @Override public String toString() { return "DefinitelyNo"; }
    };
    public static final Ternary CantTell = new Ternary() {
        @Override public String toString() { return "CantTell"; }
    };
}
