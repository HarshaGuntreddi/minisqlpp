package com.minisqlpp.backend;

import com.minisqlpp.ast.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvBackend {
    public void run(Query q, Path dataDir) throws IOException {
        Map<String,List<Map<String,String>>> tables = new HashMap<>();
        for (TableRef tr : q.from()) {
            String alias = tr.alias() != null ? tr.alias() : tr.name();
            Path file = dataDir.resolve(tr.name().toLowerCase() + ".csv");
            tables.put(alias.toLowerCase(), loadTable(file));
        }
        List<Map<String, Map<String,String>>> rows = crossJoin(tables);
        if (q.where().isPresent()) {
            Expr where = q.where().get();
            rows = rows.stream().filter(r -> evalBool(where, r)).collect(Collectors.toList());
        }
        Comparator<Map<String, Map<String,String>>> comp = null;
        for (OrderSpec os : q.orderBy()) {
            final Expr e = os.expr();
            Comparator<Map<String, Map<String,String>>> c = Comparator.comparing(r -> (Comparable)eval(e, r));
            if (!os.asc()) c = c.reversed();
            comp = comp == null ? c : comp.thenComparing(c);
        }
        if (comp != null) rows.sort(comp);
        if (q.limit() != null && rows.size() > q.limit()) {
            rows = rows.subList(0, q.limit());
        }
        List<String> headers = new ArrayList<>();
        List<java.util.function.Function<Map<String, Map<String,String>>,Object>> extractors = new ArrayList<>();
        for (SelectItem si : q.select()) {
            if (si instanceof SelectAll) {
                for (var entry : tables.entrySet()) {
                    String alias = entry.getKey();
                    List<String> cols = entry.getValue().isEmpty() ? List.of() : new ArrayList<>(entry.getValue().get(0).keySet());
                    for (String col : cols) {
                        headers.add(alias + "." + col);
                        extractors.add(r -> r.get(alias).get(col));
                    }
                }
            } else if (si instanceof SelectExpr se) {
                String name = se.alias() != null ? se.alias() : se.expr().toString();
                headers.add(name);
                extractors.add(r -> eval(se.expr(), r));
            }
        }
        System.out.println(String.join(",", headers));
        for (Map<String, Map<String,String>> r : rows) {
            List<String> vals = new ArrayList<>();
            for (var ex : extractors) {
                Object v = ex.apply(r);
                vals.add(String.valueOf(v));
            }
            System.out.println(String.join(",", vals));
        }
    }

    private List<Map<String,String>> loadTable(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        if (lines.isEmpty()) return List.of();
        String[] headers = lines.get(0).split(",");
        List<Map<String,String>> rows = new ArrayList<>();
        for (int i=1;i<lines.size();i++) {
            String[] parts = lines.get(i).split(",");
            Map<String,String> row = new HashMap<>();
            for (int j=0;j<headers.length && j<parts.length;j++) {
                row.put(headers[j], parts[j]);
            }
            rows.add(row);
        }
        return rows;
    }

    private List<Map<String, Map<String,String>>> crossJoin(Map<String,List<Map<String,String>>> tables) {
        List<Map<String, Map<String,String>>> rows = new ArrayList<>();
        rows.add(new HashMap<>());
        for (var entry : tables.entrySet()) {
            String alias = entry.getKey();
            List<Map<String,String>> tableRows = entry.getValue();
            List<Map<String, Map<String,String>>> newRows = new ArrayList<>();
            for (var row : rows) {
                for (var trow : tableRows) {
                    Map<String, Map<String,String>> nr = new HashMap<>(row);
                    nr.put(alias, trow);
                    newRows.add(nr);
                }
            }
            rows = newRows;
        }
        return rows;
    }

    private boolean evalBool(Expr e, Map<String, Map<String,String>> row) {
        Object v = eval(e, row);
        if (v instanceof Boolean b) return b;
        throw new RuntimeException("Expected boolean");
    }

    private Object eval(Expr e, Map<String, Map<String,String>> row) {
        if (e instanceof Lit lit) {
            return lit.value();
        } else if (e instanceof Col c) {
            String val = row.get(c.table().toLowerCase()).get(c.name());
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException ex) {
                return val;
            }
        } else if (e instanceof Bin b) {
            Object l = eval(b.left(), row);
            Object r = eval(b.right(), row);
            return switch (b.op()) {
                case PLUS -> ((Integer)l) + ((Integer)r);
                case MINUS -> ((Integer)l) - ((Integer)r);
                case STAR -> ((Integer)l) * ((Integer)r);
                case SLASH -> ((Integer)l) / ((Integer)r);
                case EQ -> l.equals(r);
                case NEQ -> !l.equals(r);
                case LT -> ((Comparable)l).compareTo(r) < 0;
                case LTE -> ((Comparable)l).compareTo(r) <= 0;
                case GT -> ((Comparable)l).compareTo(r) > 0;
                case GTE -> ((Comparable)l).compareTo(r) >= 0;
                case AND -> (Boolean)l && (Boolean)r;
                case OR -> (Boolean)l || (Boolean)r;
            };
        } else if (e instanceof Un u) {
            Object v = eval(u.expr(), row);
            return switch (u.op()) {
                case NEG -> -((Integer)v);
                case NOT -> !(Boolean)v;
            };
        } else {
            throw new RuntimeException("Unknown expr");
        }
    }
}
