#!/bin/bash

# string.bash - Virtual Queue Spring Boot Application Startup Script for Windows Git Bash / WSL / MSYS2
# Automatically checks/installs dependencies via winget, configures MySQL, starts Spring Boot,
# and opens http://localhost:8080 in your default Windows browser.

echo "=========================================================="
echo "Virtual Queue System - Windows Git Bash Startup Script"
echo "=========================================================="

command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# 1. Check and Install Java 21 on Windows
echo -e "\n[1/4] Checking Java 21 Environment on Windows..."
INSTALL_JAVA=false

if command_exists java.exe || command_exists java; then
    JAVA_CMD="java"
    command_exists java.exe && JAVA_CMD="java.exe"
    JAVA_VERSION=$($JAVA_CMD -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo "Found Java version: $JAVA_VERSION"
    if [[ "$JAVA_VERSION" == 21* ]]; then
        echo "Java 21 is ready."
    else
        echo "Java version is not 21. Installing OpenJDK 21 via winget..."
        INSTALL_JAVA=true
    fi
else
    echo "Java not found. Installing OpenJDK 21 via winget..."
    INSTALL_JAVA=true
fi

if [ "$INSTALL_JAVA" = true ]; then
    if command_exists winget.exe; then
        winget.exe install --id Microsoft.OpenJDK.21 -e --silent --accept-package-agreements --accept-source-agreements
    else
        echo "winget.exe not found. Please install Java 21 manually."
    fi
fi

# 2. Check and Install Maven on Windows
echo -e "\n[2/4] Checking Apache Maven on Windows..."
if command_exists mvn.cmd || command_exists mvn; then
    echo "Maven is ready."
else
    echo "Installing Apache Maven via winget..."
    if command_exists winget.exe; then
        winget.exe install --id Apache.Maven -e --silent --accept-package-agreements --accept-source-agreements
    fi
fi

# 3. Check and Install MySQL Server on Windows
echo -e "\n[3/4] Checking MySQL Database Server on Windows..."
if command_exists mysql.exe || command_exists mysql; then
    echo "MySQL Server is ready."
else
    echo "Installing MySQL Server via winget..."
    if command_exists winget.exe; then
        winget.exe install --id Oracle.MySQL -e --silent --accept-package-agreements --accept-source-agreements
    fi
fi

# Ensure Windows MySQL service is running
echo "Ensuring Windows MySQL Service is active..."
if command_exists net.exe; then
    net.exe start MySQL >/dev/null 2>&1
elif command_exists powershell.exe; then
    powershell.exe -Command "Start-Service -Name MySQL*" >/dev/null 2>&1
fi

# Setup Database and User
echo "Setting up Database 'virtualqueue' and User 'vqadmin'..."
MYSQL_BIN="mysql"
command_exists mysql.exe && MYSQL_BIN="mysql.exe"

$MYSQL_BIN -u root -e "CREATE DATABASE IF NOT EXISTS virtualqueue;" 2>/dev/null
$MYSQL_BIN -u root -e "CREATE USER IF NOT EXISTS 'vqadmin'@'localhost' IDENTIFIED BY 'vqpassword';" 2>/dev/null
$MYSQL_BIN -u root -e "GRANT ALL PRIVILEGES ON virtualqueue.* TO 'vqadmin'@'localhost';" 2>/dev/null
$MYSQL_BIN -u root -e "FLUSH PRIVILEGES;" 2>/dev/null

# 4. Launch Spring Boot Application
echo -e "\n[4/4] Launching Spring Boot Application..."
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

if [ -f "./mvnw.cmd" ]; then
    cmd.exe /c "mvnw.cmd spring-boot:run > virtualqueue.log 2>&1" &
else
    cmd.exe /c "mvn spring-boot:run > virtualqueue.log 2>&1" &
fi

echo "Waiting for Spring Boot to start on port 8080 (this may take 15-45 seconds)..."
TIMEOUT=60
COUNTER=0
STARTED=false

while [ $COUNTER -lt $TIMEOUT ]; do
    if (echo > /dev/tcp/localhost/8080) 2>/dev/null || (curl -s http://localhost:8080 >/dev/null 2>&1); then
        STARTED=true
        break
    fi
    sleep 2
    COUNTER=$((COUNTER + 2))
    echo -n "."
done

echo ""

if [ "$STARTED" = true ]; then
    echo "=========================================================="
    echo "SPRING BOOT APPLICATION IS NOW LIVE!"
    echo "Opening http://localhost:8080 in your default browser..."
    echo "=========================================================="
    echo "Default Test Credentials:"
    echo "  - Admin Portal:   admin@hospital.com / admin123"
    echo "  - Doctor Portal:  sanjay.thapa@rajdhanihealthline.com / doctor123"
    echo "  - Patient Portal: aarav.sharma1@gmail.com / patient123"
    echo "=========================================================="
    
    # Open default browser on Windows
    if command_exists cmd.exe; then
        cmd.exe /c start "http://localhost:8080" >/dev/null 2>&1 &
    elif command_exists powershell.exe; then
        powershell.exe -Command "Start-Process 'http://localhost:8080'" >/dev/null 2>&1 &
    fi
else
    echo "Error: Server did not respond on port 8080 within $TIMEOUT seconds."
    echo "Please check 'virtualqueue.log' for error details."
fi
