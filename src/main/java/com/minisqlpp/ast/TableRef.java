package com.minisqlpp.ast;

public class TableRef implements FromItem {
    private final String name;
    private final String alias; // may be null

    public TableRef(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }

    public String name() { return name; }
    public String alias() { return alias; }
}
