package com.minisqlpp.ast;

public final class Lit implements Expr {
    private final Object value;

    public Lit(Object value) {
        this.value = value;
    }

    public Object value() { return value; }
}
