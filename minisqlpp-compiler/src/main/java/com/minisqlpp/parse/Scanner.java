package com.minisqlpp.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String input;
    private final int length;
    private int pos = 0;

    private static final Map<String, TokenType> keywords = new HashMap<>();
    static {
        keywords.put("SELECT", TokenType.SELECT);
        keywords.put("FROM", TokenType.FROM);
        keywords.put("WHERE", TokenType.WHERE);
        keywords.put("AND", TokenType.AND);
        keywords.put("OR", TokenType.OR);
        keywords.put("NOT", TokenType.NOT);
        keywords.put("AS", TokenType.AS);
        keywords.put("ORDER", TokenType.ORDER);
        keywords.put("BY", TokenType.BY);
        keywords.put("ASC", TokenType.ASC);
        keywords.put("DESC", TokenType.DESC);
        keywords.put("LIMIT", TokenType.LIMIT);
    }

    public Scanner(String input) {
        this.input = input;
        this.length = input.length();
    }

    private char peek() {
        return pos < length ? input.charAt(pos) : '\0';
    }

    private char advance() {
        return pos < length ? input.charAt(pos++) : '\0';
    }

    private void skipWhitespace() {
        while (Character.isWhitespace(peek())) advance();
    }

    public List<Token> scan() {
        List<Token> tokens = new ArrayList<>();
        while (true) {
            skipWhitespace();
            int start = pos;
            char c = peek();
            if (c == '\0') {
                tokens.add(new Token(TokenType.EOF, "", pos));
                break;
            }
            if (Character.isLetter(c) || c == '_') {
                while (Character.isLetterOrDigit(peek()) || peek() == '_') advance();
                String text = input.substring(start, pos).toUpperCase();
                TokenType type = keywords.getOrDefault(text, TokenType.IDENT);
                tokens.add(new Token(type, input.substring(start, pos), start));
            } else if (Character.isDigit(c)) {
                while (Character.isDigit(peek())) advance();
                tokens.add(new Token(TokenType.NUMBER, input.substring(start, pos), start));
            } else if (c == '\'') {
                advance();
                StringBuilder sb = new StringBuilder();
                while (true) {
                    char ch = advance();
                    if (ch == '\0') throw new RuntimeException("Unterminated string at position " + start);
                    if (ch == '\'') {
                        if (peek() == '\'') { advance(); sb.append('\''); continue; }
                        break;
                    }
                    sb.append(ch);
                }
                tokens.add(new Token(TokenType.STRING, sb.toString(), start));
            } else {
                switch (c) {
                    case '=' -> { advance(); tokens.add(new Token(TokenType.EQ, "=", start)); }
                    case '!' -> {
                        advance();
                        if (peek() == '=') { advance(); tokens.add(new Token(TokenType.NEQ, "!=", start)); }
                        else throw new RuntimeException("Unexpected character ! at " + start);
                    }
                    case '<' -> {
                        advance();
                        if (peek() == '=') { advance(); tokens.add(new Token(TokenType.LTE, "<=", start)); }
                        else tokens.add(new Token(TokenType.LT, "<", start));
                    }
                    case '>' -> {
                        advance();
                        if (peek() == '=') { advance(); tokens.add(new Token(TokenType.GTE, ">=", start)); }
                        else tokens.add(new Token(TokenType.GT, ">", start));
                    }
                    case '+' -> { advance(); tokens.add(new Token(TokenType.PLUS, "+", start)); }
                    case '-' -> { advance(); tokens.add(new Token(TokenType.MINUS, "-", start)); }
                    case '*' -> { advance(); tokens.add(new Token(TokenType.STAR, "*", start)); }
                    case '/' -> { advance(); tokens.add(new Token(TokenType.SLASH, "/", start)); }
                    case ',' -> { advance(); tokens.add(new Token(TokenType.COMMA, ",", start)); }
                    case '.' -> { advance(); tokens.add(new Token(TokenType.DOT, ".", start)); }
                    case '(' -> { advance(); tokens.add(new Token(TokenType.LPAREN, "(", start)); }
                    case ')' -> { advance(); tokens.add(new Token(TokenType.RPAREN, ")", start)); }
                    default -> throw new RuntimeException("Unexpected character " + c + " at " + start);
                }
            }
        }
        return tokens;
    }
}
