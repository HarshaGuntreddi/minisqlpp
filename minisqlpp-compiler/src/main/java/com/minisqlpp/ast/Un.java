package com.minisqlpp.ast;

public final class Un implements Expr {
    public enum Op { NEG, NOT }
    private final Op op;
    private final Expr expr;

    public Un(Op op, Expr expr) {
        this.op = op;
        this.expr = expr;
    }

    public Op op() { return op; }
    public Expr expr() { return expr; }
}
