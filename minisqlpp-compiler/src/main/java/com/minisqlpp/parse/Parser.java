package com.minisqlpp.parse;

import com.minisqlpp.ast.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token peek() { return tokens.get(pos); }
    private boolean match(TokenType type) { if (peek().type() == type) { pos++; return true; } return false; }
    private Token expect(TokenType type) {
        Token t = peek();
        if (t.type() != type) throw new RuntimeException("Expected " + type + " but found " + t.type());
        pos++; return t;
    }

    public Query parseQuery() {
        expect(TokenType.SELECT);
        List<SelectItem> select = parseSelectList();
        expect(TokenType.FROM);
        List<TableRef> from = parseFromList();
        Optional<Expr> where = Optional.empty();
        if (match(TokenType.WHERE)) {
            where = Optional.of(parseExpr());
        }
        List<OrderSpec> orderBy = new ArrayList<>();
        if (match(TokenType.ORDER)) {
            expect(TokenType.BY);
            orderBy.add(parseOrderSpec());
            while (match(TokenType.COMMA)) {
                orderBy.add(parseOrderSpec());
            }
        }
        Integer limit = null;
        if (match(TokenType.LIMIT)) {
            Token num = expect(TokenType.NUMBER);
            limit = Integer.parseInt(num.text());
        }
        expect(TokenType.EOF);
        return new Query(select, from, where, orderBy, limit);
    }

    private List<SelectItem> parseSelectList() {
        List<SelectItem> items = new ArrayList<>();
        if (match(TokenType.STAR)) {
            items.add(new SelectAll());
        } else {
            items.add(parseSelectItem());
            while (match(TokenType.COMMA)) {
                items.add(parseSelectItem());
            }
        }
        return items;
    }

    private SelectItem parseSelectItem() {
        Expr expr = parseExpr();
        String alias = null;
        if (match(TokenType.AS)) {
            alias = expect(TokenType.IDENT).text();
        } else if (peek().type() == TokenType.IDENT) {
            // allow implicit alias after expression
            alias = tokens.get(pos++).text();
        }
        return new SelectExpr(expr, alias);
    }

    private List<TableRef> parseFromList() {
        List<TableRef> items = new ArrayList<>();
        items.add(parseTableRef());
        while (match(TokenType.COMMA)) {
            items.add(parseTableRef());
        }
        return items;
    }

    private TableRef parseTableRef() {
        String name = expect(TokenType.IDENT).text();
        String alias = null;
        if (match(TokenType.AS)) {
            alias = expect(TokenType.IDENT).text();
        } else if (peek().type() == TokenType.IDENT) {
            alias = tokens.get(pos++).text();
        }
        return new TableRef(name, alias);
    }

    private OrderSpec parseOrderSpec() {
        Expr expr = parseExpr();
        boolean asc = true;
        if (match(TokenType.ASC)) asc = true;
        else if (match(TokenType.DESC)) asc = false;
        return new OrderSpec(expr, asc);
    }

    private Expr parseExpr() { return parseOr(); }

    private Expr parseOr() {
        Expr left = parseAnd();
        while (match(TokenType.OR)) {
            Expr right = parseAnd();
            left = new Bin(left, Bin.Op.OR, right);
        }
        return left;
    }

    private Expr parseAnd() {
        Expr left = parseCompare();
        while (match(TokenType.AND)) {
            Expr right = parseCompare();
            left = new Bin(left, Bin.Op.AND, right);
        }
        return left;
    }

    private Expr parseCompare() {
        Expr left = parseAdd();
        while (true) {
            if (match(TokenType.EQ)) { left = new Bin(left, Bin.Op.EQ, parseAdd()); }
            else if (match(TokenType.NEQ)) { left = new Bin(left, Bin.Op.NEQ, parseAdd()); }
            else if (match(TokenType.LT)) { left = new Bin(left, Bin.Op.LT, parseAdd()); }
            else if (match(TokenType.LTE)) { left = new Bin(left, Bin.Op.LTE, parseAdd()); }
            else if (match(TokenType.GT)) { left = new Bin(left, Bin.Op.GT, parseAdd()); }
            else if (match(TokenType.GTE)) { left = new Bin(left, Bin.Op.GTE, parseAdd()); }
            else break;
        }
        return left;
    }

    private Expr parseAdd() {
        Expr left = parseMul();
        while (true) {
            if (match(TokenType.PLUS)) { left = new Bin(left, Bin.Op.PLUS, parseMul()); }
            else if (match(TokenType.MINUS)) { left = new Bin(left, Bin.Op.MINUS, parseMul()); }
            else break;
        }
        return left;
    }

    private Expr parseMul() {
        Expr left = parseUnary();
        while (true) {
            if (match(TokenType.STAR)) { left = new Bin(left, Bin.Op.STAR, parseUnary()); }
            else if (match(TokenType.SLASH)) { left = new Bin(left, Bin.Op.SLASH, parseUnary()); }
            else break;
        }
        return left;
    }

    private Expr parseUnary() {
        if (match(TokenType.NOT)) {
            return new Un(Un.Op.NOT, parseUnary());
        } else if (match(TokenType.MINUS)) {
            return new Un(Un.Op.NEG, parseUnary());
        } else {
            return parsePrimary();
        }
    }

    private Expr parsePrimary() {
        Token t = peek();
        switch (t.type()) {
            case NUMBER -> { pos++; return new Lit(Integer.parseInt(t.text())); }
            case STRING -> { pos++; return new Lit(t.text()); }
            case IDENT -> {
                pos++;
                String table = null; String name = t.text();
                if (match(TokenType.DOT)) {
                    table = name; name = expect(TokenType.IDENT).text();
                }
                return new Col(table, name);
            }
            case LPAREN -> {
                pos++; Expr e = parseExpr(); expect(TokenType.RPAREN); return e;
            }
            default -> throw new RuntimeException("Unexpected token " + t.type());
        }
    }
}
