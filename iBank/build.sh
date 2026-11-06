#!/bin/sh
# Builds and runs iBank with only the JDK (no IDE, no Maven). Requires Java 17 or later.
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 -d out $(find src/main/java -name '*.java')
cp -r src/main/resources/. out/
java -cp out ca.concordia.ibank.IbankApp
