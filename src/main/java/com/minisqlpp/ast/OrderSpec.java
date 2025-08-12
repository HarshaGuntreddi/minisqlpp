package com.minisqlpp.ast;

public class OrderSpec {
    private final Expr expr;
    private final boolean asc;

    public OrderSpec(Expr expr, boolean asc) {
        this.expr = expr;
        this.asc = asc;
    }

    public Expr expr() { return expr; }
    public boolean asc() { return asc; }
}
