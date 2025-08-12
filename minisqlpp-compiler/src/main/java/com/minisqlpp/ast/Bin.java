package com.minisqlpp.ast;

public final class Bin implements Expr {
    public enum Op { PLUS, MINUS, STAR, SLASH, EQ, NEQ, LT, LTE, GT, GTE, AND, OR }

    private final Expr left;
    private final Op op;
    private final Expr right;

    public Bin(Expr left, Op op, Expr right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public Expr left() { return left; }
    public Op op() { return op; }
    public Expr right() { return right; }
}
