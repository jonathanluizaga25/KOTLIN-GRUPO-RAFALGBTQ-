#!/bin/zsh
set -euo pipefail

PROJECT_DIR="${0:A:h}"
SERVER_PID=""

cleanup() {
  if [[ -n "$SERVER_PID" ]] && kill -0 "$SERVER_PID" 2>/dev/null; then
    kill "$SERVER_PID" 2>/dev/null || true
    wait "$SERVER_PID" 2>/dev/null || true
  fi
}
trap cleanup EXIT INT TERM

if nc -z 127.0.0.1 9090 2>/dev/null; then
  echo "El servidor gRPC ya está activo en el puerto 9090."
else
  "$PROJECT_DIR/start-grpc-server.sh" &
  SERVER_PID=$!
  echo "Iniciando servidor gRPC…"

  for attempt in {1..30}; do
    if nc -z 127.0.0.1 9090 2>/dev/null; then
      break
    fi
    if ! kill -0 "$SERVER_PID" 2>/dev/null; then
      echo "El servidor gRPC terminó antes de abrir el puerto 9090."
      exit 1
    fi
    sleep 1
  done

  if ! nc -z 127.0.0.1 9090 2>/dev/null; then
    echo "El servidor gRPC no abrió el puerto 9090 a tiempo."
    exit 1
  fi
fi

echo "Servidor gRPC listo. Iniciando ngrok…"
"$PROJECT_DIR/start-ngrok.sh"
