#!/bin/zsh
set -euo pipefail

PROJECT_DIR="${0:A:h}"
JAVA_BIN="/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java"
SERVER_JAR="$PROJECT_DIR/grpc-server/build/libs/ucbp1-grpc-server-1.0.0.jar"

if [[ ! -x "$JAVA_BIN" ]]; then
  echo "No se encontró Java de Android Studio en: $JAVA_BIN"
  exit 1
fi

if [[ ! -f "$SERVER_JAR" ]]; then
  echo "No se encontró el JAR. Compílalo antes con:"
  echo "JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ./gradlew -p grpc-server build"
  exit 1
fi

exec "$JAVA_BIN" -jar "$SERVER_JAR"
