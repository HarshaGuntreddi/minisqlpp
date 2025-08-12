package com.minisqlpp.ast;

public sealed interface Expr permits Bin, Un, Lit, Col {
}
