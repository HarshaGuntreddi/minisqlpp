@echo off
call build.bat
java -jar target/minisqlpp-compiler-1.0-SNAPSHOT-jar-with-dependencies.jar run csv query.sql --catalog data/catalog.json --data data
