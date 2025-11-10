#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JDK_DIR="$ROOT_DIR/.toolchains/openjdk21"
ARCHIVE="$ROOT_DIR/.cache/openjdk21.tar.xz"
URL="https://builds.shipilev.net/openjdk-jdk21/openjdk-jdk21-linux-x86_64-server.tar.xz"

mkdir -p "$(dirname "$ARCHIVE")"

echo "[muisca] Descargando OpenJDK 21 desde $URL"
curl -L "$URL" -o "$ARCHIVE"

rm -rf "$JDK_DIR"
mkdir -p "$JDK_DIR"
tar -xJf "$ARCHIVE" -C "$(dirname "$JDK_DIR")"

mv "$(dirname "$JDK_DIR")"/jdk "$JDK_DIR"

echo "[muisca] JDK 21 extraído en $JDK_DIR"
echo "export JAVA_HOME=\"$JDK_DIR\""
echo "export PATH=\"\$JAVA_HOME/bin:\$PATH\""
