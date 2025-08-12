# MiniSQL++ Compiler

This project implements a small SQL-like compiler with a table-driven scanner, LL(1) parser, type checker, CSV backend and Oracle SQL emitter.

## Build

```
./build.sh
```
(or `build.bat` on Windows)

## Demo

The demo query is in `query.sql`. Run it on the CSV backend:

```
./run_demo.sh
```
(on Windows use `run_demo.bat` or `RUN_ALL_DEBUG.bat` to build and run with pause).

## CLI

The shaded jar exposes the following commands:

```
java -jar target/minisqlpp-compiler-1.0-SNAPSHOT-jar-with-dependencies.jar tokens query.sql
java -jar target/... ast query.sql
java -jar target/... typecheck query.sql --catalog catalog.json
java -jar target/... run csv query.sql --catalog catalog.json --data data
java -jar target/... run oracle query.sql
```

## Tests

Run the JUnit tests with:

```
mvn test
```
