package com.minisqlpp.cli;

import com.minisqlpp.ast.Query;
import com.minisqlpp.backend.CsvBackend;
import com.minisqlpp.backend.OracleEmitter;
import com.minisqlpp.check.TypeChecker;
import com.minisqlpp.parse.Parser;
import com.minisqlpp.parse.Scanner;
import com.minisqlpp.parse.Token;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Command(name="minisqlpp", subcommands = {Main.Tokens.class, Main.Ast.class, Main.Typecheck.class, Main.Run.class})
public class Main implements Runnable {
    public static void main(String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }
    @Override public void run() {}

    @Command(name="tokens", description="Print tokens")
    static class Tokens implements Runnable {
        @Parameters(paramLabel="FILE") Path file;
        public void run() {
            try {
                String sql = Files.readString(file);
                Scanner sc = new Scanner(sql);
                List<Token> tokens = sc.scan();
                tokens.forEach(System.out::println);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @Command(name="ast", description="Print AST")
    static class Ast implements Runnable {
        @Parameters(paramLabel="FILE") Path file;
        public void run() {
            try {
                Query q = parse(file);
                System.out.println(AstPrinter.print(q));
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @Command(name="typecheck", description="Type check query")
    static class Typecheck implements Runnable {
        @Parameters(paramLabel="FILE") Path file;
        @Option(names="--catalog", defaultValue="catalog.json") Path catalog;
        public void run() {
            try {
                Query q = parse(file);
                new TypeChecker(catalog).check(q);
                System.out.println("OK");
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @Command(name="run", description="Run query")
    static class Run implements Runnable {
        @Parameters(index="0", paramLabel="BACKEND") String backend; // csv or oracle
        @Parameters(index="1", paramLabel="FILE") Path file;
        @Option(names="--catalog", defaultValue="catalog.json") Path catalog;
        @Option(names="--data", defaultValue="data") Path dataDir;
        public void run() {
            try {
                Query q = parse(file);
                new TypeChecker(catalog).check(q);
                if (backend.equalsIgnoreCase("csv")) {
                    new CsvBackend().run(q, dataDir);
                } else if (backend.equalsIgnoreCase("oracle")) {
                    System.out.println(new OracleEmitter().toSql(q));
                } else {
                    System.err.println("Unknown backend " + backend);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    static Query parse(Path file) throws Exception {
        String sql = Files.readString(file);
        Scanner sc = new Scanner(sql);
        List<Token> tokens = sc.scan();
        Parser p = new Parser(tokens);
        return p.parseQuery();
    }
}
