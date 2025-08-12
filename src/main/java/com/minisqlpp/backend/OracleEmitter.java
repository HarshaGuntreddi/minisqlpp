package com.minisqlpp.backend;

import com.minisqlpp.ast.*;

import java.util.List;
import java.util.stream.Collectors;

public class OracleEmitter {
    public String toSql(Query q) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(q.select().stream().map(this::selectToSql).collect(Collectors.joining(", ")));
        sb.append(" FROM ");
        sb.append(q.from().stream().map(this::tableToSql).collect(Collectors.joining(", ")));
        q.where().ifPresent(w -> sb.append(" WHERE ").append(exprToSql(w)));
        if (!q.orderBy().isEmpty()) {
            sb.append(" ORDER BY ");
            sb.append(q.orderBy().stream().map(this::orderToSql).collect(Collectors.joining(", ")));
        }
        if (q.limit() != null) {
            sb.append(" FETCH FIRST ").append(q.limit()).append(" ROWS ONLY");
        }
        return sb.toString();
    }

    private String selectToSql(SelectItem si) {
        if (si instanceof SelectAll) return "*";
        SelectExpr se = (SelectExpr) si;
        String e = exprToSql(se.expr());
        return se.alias() != null ? e + " AS " + se.alias() : e;
    }

    private String tableToSql(TableRef tr) {
        return tr.alias() != null ? tr.name() + " " + tr.alias() : tr.name();
    }

    private String orderToSql(OrderSpec os) {
        return exprToSql(os.expr()) + (os.asc() ? " ASC" : " DESC");
    }

    private String exprToSql(Expr e) {
        if (e instanceof Lit lit) {
            if (lit.value() instanceof String s) {
                return "'" + s.replace("'", "''") + "'";
            } else {
                return lit.value().toString();
            }
        } else if (e instanceof Col c) {
            return (c.table() != null ? c.table() + "." : "") + c.name();
        } else if (e instanceof Bin b) {
            return "(" + exprToSql(b.left()) + " " + binOp(b.op()) + " " + exprToSql(b.right()) + ")";
        } else if (e instanceof Un u) {
            return u.op() == Un.Op.NOT ? "NOT " + exprToSql(u.expr()) : "-" + exprToSql(u.expr());
        } else {
            throw new RuntimeException("Unknown expr");
        }
    }

    private String binOp(Bin.Op op) {
        return switch (op) {
            case PLUS -> "+"; case MINUS -> "-"; case STAR -> "*"; case SLASH -> "/";
            case EQ -> "="; case NEQ -> "<>"; case LT -> "<"; case LTE -> "<=";
            case GT -> ">"; case GTE -> ">="; case AND -> "AND"; case OR -> "OR";
        };
    }
}
