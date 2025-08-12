package com.minisqlpp.ast;

public final class Col implements Expr {
    private String table; // may be null until resolved
    private final String name;

    public Col(String table, String name) {
        this.table = table;
        this.name = name;
    }

    public String table() { return table; }
    public void table(String t) { this.table = t; }
    public String name() { return name; }
}
