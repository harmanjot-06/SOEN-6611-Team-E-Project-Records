@echo off
rem Builds and runs iBank with only the JDK (no IDE, no Maven). Requires Java 17 or later.
cd /d "%~dp0"
if not exist out mkdir out
dir /s /b src\main\java\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
del sources.txt
xcopy /e /y /q src\main\resources out\ > nul
java -cp out ca.concordia.ibank.IbankApp
