#!/bin/zsh
set -euo pipefail

PROJECT_DIR="${0:A:h}"
NGROK_BIN="$PROJECT_DIR/tools/ngrok/ngrok"

if [[ ! -x "$NGROK_BIN" ]]; then
  if command -v ngrok >/dev/null 2>&1; then
    NGROK_BIN="$(command -v ngrok)"
  else
    echo "Instala ngrok desde https://ngrok.com/download o coloca el ejecutable en tools/ngrok/ngrok."
    exit 1
  fi
fi

echo "Abriendo un túnel HTTPS hacia el servidor gRPC del puerto 9090…"
exec "$NGROK_BIN" http --upstream-protocol=http2 9090
