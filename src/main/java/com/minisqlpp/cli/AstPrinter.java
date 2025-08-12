package com.minisqlpp.cli;

import com.minisqlpp.ast.*;

import java.util.stream.Collectors;

public class AstPrinter {
    public static String print(Query q) {
        StringBuilder sb = new StringBuilder();
        sb.append("Query{select=");
        sb.append(q.select().stream().map(AstPrinter::printSelect).collect(Collectors.joining(",")));
        sb.append(", from=");
        sb.append(q.from().stream().map(t -> t.name() + (t.alias()!=null?" as "+t.alias():""))
                .collect(Collectors.joining(",")));
        q.where().ifPresent(w -> sb.append(", where=").append(printExpr(w)));
        if (!q.orderBy().isEmpty()) {
            sb.append(", orderBy=");
            sb.append(q.orderBy().stream().map(o -> printExpr(o.expr()) + (o.asc()?" ASC":" DESC"))
                    .collect(Collectors.joining(",")));
        }
        if (q.limit() != null) sb.append(", limit=").append(q.limit());
        sb.append("}");
        return sb.toString();
    }

    private static String printSelect(SelectItem si) {
        if (si instanceof SelectAll) return "*";
        SelectExpr se = (SelectExpr) si;
        return printExpr(se.expr()) + (se.alias()!=null?" AS "+se.alias():"");
    }

    private static String printExpr(Expr e) {
        if (e instanceof Lit lit) return String.valueOf(lit.value());
        if (e instanceof Col c) return (c.table()!=null?c.table()+".":"") + c.name();
        if (e instanceof Bin b) return "("+printExpr(b.left())+" "+b.op()+" "+printExpr(b.right())+")";
        if (e instanceof Un u) return u.op()+"("+printExpr(u.expr())+")";
        return "?";
    }
}
