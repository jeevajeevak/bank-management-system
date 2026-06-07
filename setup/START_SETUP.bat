@echo off
:: ============================================================
::  STEP 1 OF 1 — Double-click this file to start Setup
::  No configuration needed, the wizard will guide you!
:: ============================================================
title JavaBank Setup Wizard

echo Checking Java...
java -version 2>nul
IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Java not found!
    echo Please install Java 11+ from https://adoptium.net
    echo Then try again.
    pause
    exit /b 1
)

echo Compiling Setup Wizard...
cd /d "%~dp0"
javac SetupWizard.java
IF %ERRORLEVEL% NEQ 0 (
    echo Compile failed. Make sure Java JDK is installed (not just JRE).
    pause
    exit /b 1
)

echo Launching Setup Wizard...
java SetupWizard
