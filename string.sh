#!/bin/bash

# string.sh - Virtual Queue Spring Boot Startup Script for Windows (Git Bash / WSL / MSYS2), Linux & macOS

echo "=========================================================="
echo "Virtual Queue System - Spring Boot Application Startup"
echo "=========================================================="

command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Detect Windows environment (Git Bash, MSYS2, Cygwin, WSL)
IS_WINDOWS=false
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" || -n "$WSL_DISTRO_NAME" || "$(uname -r)" == *[M|m]icrosoft* ]]; then
    IS_WINDOWS=true
fi

# Detect Linux OS if applicable
OS="Unknown"
if [ -f /etc/os-release ]; then
    . /etc/os-release
    OS=$ID
fi

SUDO=""
if command_exists sudo && [ "$IS_WINDOWS" = false ]; then
    SUDO="sudo"
fi

# 1. Check and Install Java 21
echo -e "\n[1/4] Checking Java 21 Environment..."
INSTALL_JAVA=false

if command_exists java || command_exists java.exe; then
    JAVA_CMD="java"
    command_exists java.exe && JAVA_CMD="java.exe"
    JAVA_VERSION=$($JAVA_CMD -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo "Found Java version: $JAVA_VERSION"
    if [[ "$JAVA_VERSION" == 21* ]]; then
        echo "Java 21 is ready."
    else
        echo "Java version is not 21. Preparing to install OpenJDK 21..."
        INSTALL_JAVA=true
    fi
else
    echo "Java is not installed. Preparing to install OpenJDK 21..."
    INSTALL_JAVA=true
fi

if [ "$INSTALL_JAVA" = true ]; then
    if [ "$IS_WINDOWS" = true ] && command_exists winget.exe; then
        echo "Installing OpenJDK 21 via winget on Windows..."
        winget.exe install --id Microsoft.OpenJDK.21 -e --silent --accept-package-agreements --accept-source-agreements
    elif [[ "$OS" == "ubuntu" || "$OS" == "debian" ]]; then
        echo "Installing OpenJDK 21 via apt..."
        $SUDO apt-get update
        $SUDO apt-get install -y openjdk-21-jdk
    elif [[ "$OS" == "fedora" || "$OS" == "centos" || "$OS" == "rhel" ]]; then
        if command_exists dnf; then
            $SUDO dnf install -y java-21-openjdk-devel
        else
            $SUDO yum install -y java-21-openjdk-devel
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        if command_exists brew; then
            brew install openjdk@21
        fi
    fi
fi

# 2. Check and Install Maven
echo -e "\n[2/4] Checking Apache Maven..."
if command_exists mvn || command_exists mvn.cmd; then
    echo "Maven is ready."
else
    echo "Installing Apache Maven..."
    if [ "$IS_WINDOWS" = true ] && command_exists winget.exe; then
        winget.exe install --id Apache.Maven -e --silent --accept-package-agreements --accept-source-agreements
    elif [[ "$OS" == "ubuntu" || "$OS" == "debian" ]]; then
        $SUDO apt-get update
        $SUDO apt-get install -y maven
    elif [[ "$OS" == "fedora" || "$OS" == "centos" || "$OS" == "rhel" ]]; then
        if command_exists dnf; then
            $SUDO dnf install -y maven
        else
            $SUDO yum install -y maven
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        if command_exists brew; then
            brew install maven
        fi
    fi
fi

# 3. Check and Install MySQL Server
echo -e "\n[3/4] Checking MySQL Database Server..."
if command_exists mysql || command_exists mysql.exe; then
    echo "MySQL Server is ready."
else
    echo "Installing MySQL Server..."
    if [ "$IS_WINDOWS" = true ] && command_exists winget.exe; then
        winget.exe install --id Oracle.MySQL -e --silent --accept-package-agreements --accept-source-agreements
    elif [[ "$OS" == "ubuntu" || "$OS" == "debian" ]]; then
        $SUDO apt-get update
        $SUDO apt-get install -y mysql-server
    elif [[ "$OS" == "fedora" || "$OS" == "centos" || "$OS" == "rhel" ]]; then
        if command_exists dnf; then
            $SUDO dnf install -y mysql-server
        else
            $SUDO yum install -y mysql-server
        fi
    fi
fi

# Ensure MySQL service is active
echo "Ensuring MySQL Service is active..."
if [ "$IS_WINDOWS" = true ]; then
    if command_exists net.exe; then
        net.exe start MySQL >/dev/null 2>&1
    elif command_exists powershell.exe; then
        powershell.exe -Command "Start-Service -Name MySQL*" >/dev/null 2>&1
    fi
elif [[ "$OS" == "ubuntu" || "$OS" == "debian" ]]; then
    $SUDO service mysql start >/dev/null 2>&1
elif [[ "$OS" == "fedora" || "$OS" == "centos" || "$OS" == "rhel" ]]; then
    $SUDO systemctl start mysqld >/dev/null 2>&1
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

if [ -f "./mvnw.cmd" ] && [ "$IS_WINDOWS" = true ]; then
    cmd.exe /c "mvnw.cmd spring-boot:run > virtualqueue.log 2>&1" &
elif [ -f "./mvnw" ]; then
    nohup ./mvnw spring-boot:run > virtualqueue.log 2>&1 &
else
    nohup mvn spring-boot:run > virtualqueue.log 2>&1 &
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
    
    # Automatically open default browser on Windows, Linux, or macOS
    if [ "$IS_WINDOWS" = true ]; then
        if command_exists cmd.exe; then
            cmd.exe /c start "http://localhost:8080" >/dev/null 2>&1 &
        elif command_exists powershell.exe; then
            powershell.exe -Command "Start-Process 'http://localhost:8080'" >/dev/null 2>&1 &
        fi
    elif command_exists xdg-open; then
        xdg-open "http://localhost:8080" >/dev/null 2>&1 &
    elif command_exists open; then
        open "http://localhost:8080" >/dev/null 2>&1 &
    fi
else
    echo "Error: Server did not respond on port 8080 within $TIMEOUT seconds."
    echo "Please check 'virtualqueue.log' for error details."
fi
