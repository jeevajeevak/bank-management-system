#!/bin/bash
# ============================================================
#  Run this once to configure and build JavaBank
#  chmod +x START_SETUP.sh && ./START_SETUP.sh
# ============================================================

echo "Checking Java..."
if ! java -version 2>/dev/null; then
    echo "ERROR: Java not found. Install Java 11+ from https://adoptium.net"
    exit 1
fi

cd "$(dirname "$0")"
echo "Compiling Setup Wizard..."
javac SetupWizard.java

if [ $? -ne 0 ]; then
    echo "Compile failed. Make sure Java JDK is installed."
    exit 1
fi

echo "Launching Setup Wizard..."
java SetupWizard
