#!/bin/sh
./build.sh
java -jar target/minisqlpp-compiler-1.0-SNAPSHOT-jar-with-dependencies.jar run csv query.sql --catalog catalog.json --data data
