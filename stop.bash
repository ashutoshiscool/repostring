#!/bin/bash

# stop.bash - Stop Virtual Queue Application on Windows Git Bash

echo "==========================================="
echo "Stopping Virtual Queue Application..."
echo "==========================================="

PID=""

if command_exists netstat.exe; then
    PID=$(netstat.exe -ano | grep ":8080 " | grep "LISTENING" | awk '{print $5}' | head -n 1 | tr -d '\r')
fi

if [ -z "$PID" ] && command_exists powershell.exe; then
    PID=$(powershell.exe -Command "(Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue).OwningProcess" | tr -d '\r')
fi

if [ -z "$PID" ] || [ "$PID" = "0" ]; then
    echo "No application is currently running on port 8080."
else
    echo "Killing process $PID running on port 8080..."
    if command_exists taskkill.exe; then
        taskkill.exe //F //PID "$PID" >/dev/null 2>&1
    elif command_exists powershell.exe; then
        powershell.exe -Command "Stop-Process -Id $PID -Force" >/dev/null 2>&1
    else
        kill -9 "$PID" 2>/dev/null
    fi
    echo "Application stopped successfully."
fi
