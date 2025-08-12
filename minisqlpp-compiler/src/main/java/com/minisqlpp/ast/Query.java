package com.minisqlpp.ast;

import java.util.List;
import java.util.Optional;

public class Query {
    private final List<SelectItem> select;
    private final List<TableRef> from;
    private final Optional<Expr> where;
    private final List<OrderSpec> orderBy;
    private final Integer limit; // nullable

    public Query(List<SelectItem> select, List<TableRef> from, Optional<Expr> where,
                 List<OrderSpec> orderBy, Integer limit) {
        this.select = select;
        this.from = from;
        this.where = where;
        this.orderBy = orderBy;
        this.limit = limit;
    }

    public List<SelectItem> select() { return select; }
    public List<TableRef> from() { return from; }
    public Optional<Expr> where() { return where; }
    public List<OrderSpec> orderBy() { return orderBy; }
    public Integer limit() { return limit; }
}
