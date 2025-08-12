package com.minisqlpp;

import com.minisqlpp.ast.Query;
import com.minisqlpp.backend.CsvBackend;
import com.minisqlpp.check.TypeChecker;
import com.minisqlpp.parse.Parser;
import com.minisqlpp.parse.Scanner;
import com.minisqlpp.parse.Token;
import com.minisqlpp.parse.TokenType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CompilerTest {
    @Test
    void scanKeywords() {
        Scanner sc = new Scanner("SELECT FROM WHERE");
        List<Token> tokens = sc.scan();
        assertEquals(TokenType.SELECT, tokens.get(0).type());
        assertEquals(TokenType.FROM, tokens.get(1).type());
        assertEquals(TokenType.WHERE, tokens.get(2).type());
    }

    @Test
    void parseAndRun() throws Exception {
        String sql = Files.readString(Path.of("query.sql"));
        Scanner sc = new Scanner(sql);
        Query q = new Parser(sc.scan()).parseQuery();
        new TypeChecker(Path.of("catalog.json")).check(q);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream old = System.out;
        System.setOut(new PrintStream(baos));
        new CsvBackend().run(q, Path.of("data"));
        System.setOut(old);
        String out = baos.toString();
        assertTrue(out.contains("Alice"));
    }
}
