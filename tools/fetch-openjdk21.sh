#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CACHE_DIR="$ROOT_DIR/.cache"
LINUX_TOOLCHAIN_DIR="$ROOT_DIR/.toolchains"
JDK_DIR="$LINUX_TOOLCHAIN_DIR/linux-openjdk21"
ARCHIVE="$CACHE_DIR/openjdk21.tar.xz"
URL="https://builds.shipilev.net/openjdk-jdk21/openjdk-jdk21-linux-x86_64-server.tar.xz"

mkdir -p "$CACHE_DIR" "$LINUX_TOOLCHAIN_DIR"

echo "[muisca] Descargando OpenJDK 21 desde $URL"
curl -L "$URL" -o "$ARCHIVE"

rm -rf "$JDK_DIR"
mkdir -p "$JDK_DIR"
tar -xJf "$ARCHIVE" -C "$JDK_DIR" --strip-components=1

echo "[muisca] JDK 21 extraído en $JDK_DIR"
echo "export JAVA_HOME=\"$JDK_DIR\""
echo "export PATH=\"\$JAVA_HOME/bin:\$PATH\""
