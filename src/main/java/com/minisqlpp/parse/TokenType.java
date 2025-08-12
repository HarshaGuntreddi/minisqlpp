package com.minisqlpp.parse;

public enum TokenType {
    // Keywords
    SELECT, FROM, WHERE, AND, OR, NOT, AS, ORDER, BY, ASC, DESC, LIMIT,
    // Identifiers and literals
    IDENT, NUMBER, STRING,
    // Operators
    EQ, NEQ, LT, LTE, GT, GTE, PLUS, MINUS, STAR, SLASH,
    // Punctuation
    COMMA, DOT, LPAREN, RPAREN,
    // End of input
    EOF
}
