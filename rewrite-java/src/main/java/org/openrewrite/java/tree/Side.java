package org.openrewrite.java.tree;

public enum Side {
    LVALUE, // refers to an expression being assigned to
    RVALUE; // refers to an expression being evaluated
}
