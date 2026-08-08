#!/bin/bash

# start.bash - Virtual Queue Startup Script for Windows Git Bash

echo "==========================================="
echo "Virtual Queue - System Check & Start Script"
echo "==========================================="

command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# 1. Check and Install Java 21 on Windows
echo -e "\n[1/4] Checking for Java 21..."
INSTALL_JAVA=false

if command_exists java.exe || command_exists java; then
    JAVA_CMD="java"
    command_exists java.exe && JAVA_CMD="java.exe"
    JAVA_VERSION=$($JAVA_CMD -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo "Found Java version: $JAVA_VERSION"
    if [[ "$JAVA_VERSION" == 21* ]]; then
        echo "Java 21 is ready."
    else
        echo "Found Java $JAVA_VERSION, but require Java 21. Installing via winget..."
        INSTALL_JAVA=true
    fi
else
    echo "Java not found. Installing Java 21 via winget..."
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
echo -e "\n[2/4] Checking for Maven..."
if command_exists mvn.cmd || command_exists mvn; then
    echo "Maven is ready."
else
    echo "Installing Maven via winget..."
    if command_exists winget.exe; then
        winget.exe install --id Apache.Maven -e --silent --accept-package-agreements --accept-source-agreements
    fi
fi

# 3. Check and Install MySQL Server on Windows
echo -e "\n[3/4] Checking for MySQL..."
if command_exists mysql.exe || command_exists mysql; then
    echo "MySQL is ready."
else
    echo "Installing MySQL Server via winget..."
    if command_exists winget.exe; then
        winget.exe install --id Oracle.MySQL -e --silent --accept-package-agreements --accept-source-agreements
    fi
fi

# Ensure MySQL service is running
echo "Ensuring MySQL Service is active..."
if command_exists net.exe; then
    net.exe start MySQL >/dev/null 2>&1
elif command_exists powershell.exe; then
    powershell.exe -Command "Start-Service -Name MySQL*" >/dev/null 2>&1
fi

# Setup Database and User
echo "Configuring MySQL Database..."
MYSQL_BIN="mysql"
command_exists mysql.exe && MYSQL_BIN="mysql.exe"

$MYSQL_BIN -u root -e "CREATE DATABASE IF NOT EXISTS virtualqueue;" 2>/dev/null
$MYSQL_BIN -u root -e "CREATE USER IF NOT EXISTS 'vqadmin'@'localhost' IDENTIFIED BY 'vqpassword';" 2>/dev/null
$MYSQL_BIN -u root -e "GRANT ALL PRIVILEGES ON virtualqueue.* TO 'vqadmin'@'localhost';" 2>/dev/null
$MYSQL_BIN -u root -e "FLUSH PRIVILEGES;" 2>/dev/null

# 4. Start Spring Boot in background
echo -e "\n[4/4] Starting Spring Boot in the background..."
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

if [ -f "./mvnw.cmd" ]; then
    cmd.exe /c "mvnw.cmd spring-boot:run > virtualqueue.log 2>&1" &
else
    cmd.exe /c "mvn spring-boot:run > virtualqueue.log 2>&1" &
fi

echo "Waiting for application to start (this might take up to 60 seconds)..."
TIMEOUT=60
COUNTER=0

while [ $COUNTER -lt $TIMEOUT ]; do
    if (echo > /dev/tcp/localhost/8080) 2>/dev/null || (curl -s http://localhost:8080 >/dev/null 2>&1); then
        break
    fi
    sleep 2
    COUNTER=$((COUNTER + 2))
done

if [ $COUNTER -ge $TIMEOUT ]; then
    echo "Error: The application failed to start on port 8080 within 60 seconds."
    echo "Please check 'virtualqueue.log' for details."
    exit 1
fi

echo -e "\n======================================================="
echo "APPLICATION IS NOW LIVE!"
echo "Access it at: http://localhost:8080"
echo ""
echo "Test Accounts:"
echo "  - Admin:   admin@hospital.com / admin123"
echo "  - Doctor:  sanjay.thapa@rajdhanihealthline.com / doctor123"
echo "  - Patient: [firstname].[lastname]1@gmail.com / patient123 (e.g. aarav.sharma1@gmail.com)"
echo "======================================================="
echo "Application is running in the background."
echo "You can now safely close this terminal."
echo "To stop the server later, run: ./stop.bash"
echo ""
