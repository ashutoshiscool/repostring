#!/bin/bash

# stop.sh - Stop Virtual Queue Application on Linux & macOS

echo "==========================================="
echo "Stopping Virtual Queue Application..."
echo "==========================================="

PID=$(lsof -t -i:8080 2>/dev/null)

if [ -z "$PID" ]; then
    echo "No application is currently running on port 8080."
else
    echo "Killing process $PID running on port 8080..."
    kill -9 $PID 2>/dev/null
    echo "Application stopped successfully."
fi
