#!/usr/bin/env bash
# =============================================================================
# CrowdCheck - Linux / macOS Launch Script
# =============================================================================

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

echo "==============================================================================="
echo "           Starting CrowdCheck Live Crowd Monitoring System...                 "
echo "==============================================================================="

if [ ! -f "target/crowdcheck.jar" ]; then
    echo "[*] Executable JAR not found. Building project with Maven..."
    mvn clean package -DskipTests
fi

echo "[*] Launching application..."
java -jar target/crowdcheck.jar
