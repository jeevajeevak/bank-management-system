#!/bin/bash
# ============================================================
#  build_and_run.sh  (Linux / macOS)
#  chmod +x build_and_run.sh && ./build_and_run.sh
# ============================================================

echo "[1/3] Checking Java..."
if ! java -version 2>/dev/null; then
    echo "ERROR: Java not found. Install Java 11+."
    exit 1
fi

echo "[2/3] Compiling Java sources..."
mkdir -p out
javac -cp "lib/*" -d out \
    src/bank/Main.java \
    src/bank/ui/*.java \
    src/bank/service/*.java \
    src/bank/dao/*.java \
    src/bank/model/*.java \
    src/bank/util/*.java

if [ $? -ne 0 ]; then
    echo "COMPILE ERROR. Check the messages above."
    exit 1
fi

echo "[3/3] Launching JavaBank..."
java -cp "out:lib/*" bank.Main
