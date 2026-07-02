#!/bin/bash

echo "==========================================="
echo "🛑 Stopping Virtual Queue Application..."
echo "==========================================="

# Find process ID listening on port 8080
PID=$(lsof -t -i:8080 2>/dev/null)

if [ -z "$PID" ]; then
    echo "No application is currently running on port 8080."
else
    echo "Killing process $PID running on port 8080..."
    kill -9 $PID
    echo "✅ Application stopped successfully."
fi
