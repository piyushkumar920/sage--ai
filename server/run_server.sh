#!/bin/bash
# Helper script to check status or launch Sage Node.js API if not already running
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if pgrep -f "node server.js" > /dev/null 2>&1; then
  echo "Sage backend is already running (PID $(pgrep -f 'node server.js' | head -n 1))."
  exit 0
fi

echo "Starting Sage backend on port ${DEFAULT_APP_PORT:-3000}..."
exec node "$SCRIPT_DIR/server.js"
