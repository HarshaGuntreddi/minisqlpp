package com.minisqlpp.check;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minisqlpp.ast.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TypeChecker {
    private final Map<String, Map<String, Type>> catalog = new HashMap<>();

    @SuppressWarnings("unchecked")
    public TypeChecker(Path catalogPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Map<String, String>> raw = mapper.readValue(catalogPath.toFile(), Map.class);
        for (var entry : raw.entrySet()) {
            Map<String, Type> cols = new HashMap<>();
            for (var c : entry.getValue().entrySet()) {
                cols.put(c.getKey().toLowerCase(), Type.valueOf(c.getValue().toUpperCase()));
            }
            catalog.put(entry.getKey().toLowerCase(), cols);
        }
    }

    public void check(Query q) {
        Map<String,String> aliasToTable = new HashMap<>();
        for (TableRef tr : q.from()) {
            String table = tr.name().toLowerCase();
            if (!catalog.containsKey(table)) throw new RuntimeException("Unknown table " + tr.name());
            String alias = tr.alias() != null ? tr.alias() : tr.name();
            aliasToTable.put(alias.toLowerCase(), table);
        }
        for (SelectItem si : q.select()) {
            if (si instanceof SelectExpr se) resolveExpr(se.expr(), aliasToTable);
        }
        q.where().ifPresent(e -> resolveExpr(e, aliasToTable));
        for (OrderSpec os : q.orderBy()) resolveExpr(os.expr(), aliasToTable);
    }

    private Type resolveExpr(Expr e, Map<String,String> aliasToTable) {
        if (e instanceof Lit lit) {
            return (lit.value() instanceof Integer) ? Type.INT : Type.STRING;
        } else if (e instanceof Col col) {
            if (col.table() != null) {
                String alias = col.table().toLowerCase();
                String table = aliasToTable.get(alias);
                if (table == null) throw new RuntimeException("Unknown table alias " + col.table());
                Type t = catalog.get(table).get(col.name().toLowerCase());
                if (t == null) throw new RuntimeException("Unknown column " + col.name());
                col.table(alias); // normalized
                return t;
            } else {
                String foundAlias = null; Type foundType = null; int count = 0;
                for (var entry : aliasToTable.entrySet()) {
                    Type t = catalog.get(entry.getValue()).get(col.name().toLowerCase());
                    if (t != null) {
                        foundAlias = entry.getKey(); foundType = t; count++;
                    }
                }
                if (count == 0) throw new RuntimeException("Unknown column " + col.name());
                if (count > 1) throw new RuntimeException("Ambiguous column " + col.name());
                col.table(foundAlias); return foundType;
            }
        } else if (e instanceof Bin b) {
            Type l = resolveExpr(b.left(), aliasToTable);
            Type r = resolveExpr(b.right(), aliasToTable);
            return switch (b.op()) {
                case PLUS, MINUS, STAR, SLASH -> {
                    if (l != Type.INT || r != Type.INT) throw new RuntimeException("Arithmetic on non-INT");
                    yield Type.INT;
                }
                case EQ, NEQ, LT, LTE, GT, GTE -> {
                    if (l != r) throw new RuntimeException("Type mismatch");
                    yield Type.BOOL;
                }
                case AND, OR -> {
                    if (l != Type.BOOL || r != Type.BOOL) throw new RuntimeException("Boolean op on non-bool");
                    yield Type.BOOL;
                }
            };
        } else if (e instanceof Un u) {
            Type t = resolveExpr(u.expr(), aliasToTable);
            return switch (u.op()) {
                case NEG -> {
                    if (t != Type.INT) throw new RuntimeException("Negation on non-int");
                    yield Type.INT;
                }
                case NOT -> {
                    if (t != Type.BOOL) throw new RuntimeException("Not on non-bool");
                    yield Type.BOOL;
                }
            };
        } else {
            throw new RuntimeException("Unknown expression type");
        }
    }
}
