:: ============================================================
::  build_and_run.bat  (Windows)
::  Double-click this file to compile and run the application
:: ============================================================
@echo off

echo [1/3] Checking Java...
java -version 2>nul
IF %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java not found. Install Java 11+ and add it to PATH.
    pause & exit /b 1
)

echo [2/3] Compiling Java sources...
if not exist out mkdir out
javac -cp "lib/*" -d out -sourcepath src src\bank\Main.java src\bank\ui\*.java src\bank\service\*.java src\bank\dao\*.java src\bank\model\*.java src\bank\util\*.java
IF %ERRORLEVEL% NEQ 0 (
    echo COMPILE ERROR. Check the messages above.
    pause & exit /b 1
)

echo [3/3] Launching JavaBank...
java -cp "out;lib/*" bank.Main
pause
