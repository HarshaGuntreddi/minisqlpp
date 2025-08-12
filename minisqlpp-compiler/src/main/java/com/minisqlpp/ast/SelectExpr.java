package com.minisqlpp.ast;

public class SelectExpr implements SelectItem {
    private final Expr expr;
    private final String alias; // may be null

    public SelectExpr(Expr expr, String alias) {
        this.expr = expr;
        this.alias = alias;
    }

    public Expr expr() { return expr; }
    public String alias() { return alias; }
}
