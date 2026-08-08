#!/bin/bash

# string.sh - Virtual Queue Spring Boot Application Startup Script for Linux & macOS
# Automatically checks/installs dependencies, configures MySQL, starts Spring Boot,
# and opens http://localhost:8080 in your default browser.

echo "=========================================================="
echo "Virtual Queue System - Spring Boot Application Startup"
echo "=========================================================="

command_exists() {
    command -v "$1" >/dev/null 2>&1
}

SUDO=""
if command_exists sudo; then
    SUDO="sudo"
fi

OS="Unknown"
if [ -f /etc/os-release ]; then
    . /etc/os-release
    OS=$ID
fi

# 1. Check and Install Java 21
echo -e "\n[1/4] Checking Java 21 Environment..."
INSTALL_JAVA=false

if command_exists java; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
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
    if [[ "$OS" == "ubuntu" || "$OS" == "debian" ]]; then
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
        else
            echo "Homebrew not found. Please install Java 21 manually."
            exit 1
        fi
    else
        echo "Unsupported OS for automated Java 21 installation."
        exit 1
    fi
fi

# 2. Check and Install Maven
echo -e "\n[2/4] Checking Apache Maven..."
if command_exists mvn; then
    echo "Maven is ready."
else
    echo "Installing Apache Maven..."
    if [[ "$OS" == "ubuntu" || "$OS" == "debian" ]]; then
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
        else
            echo "Homebrew not found. Please install Maven manually."
            exit 1
        fi
    fi
fi

# 3. Check and Install MySQL Server
echo -e "\n[3/4] Checking MySQL Database Server..."
if command_exists mysql; then
    echo "MySQL Server is ready."
else
    echo "Installing MySQL Server..."
    if [[ "$OS" == "ubuntu" || "$OS" == "debian" ]]; then
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

# Ensure MySQL service is running
echo "Ensuring MySQL Service is active..."
if [[ "$OS" == "ubuntu" || "$OS" == "debian" ]]; then
    $SUDO service mysql start >/dev/null 2>&1
elif [[ "$OS" == "fedora" || "$OS" == "centos" || "$OS" == "rhel" ]]; then
    $SUDO systemctl start mysqld >/dev/null 2>&1
fi

# Setup Database and User
echo "Setting up Database 'virtualqueue' and User 'vqadmin'..."
$SUDO mysql -e "CREATE DATABASE IF NOT EXISTS virtualqueue;" 2>/dev/null
$SUDO mysql -e "CREATE USER IF NOT EXISTS 'vqadmin'@'localhost' IDENTIFIED BY 'vqpassword';" 2>/dev/null
$SUDO mysql -e "GRANT ALL PRIVILEGES ON virtualqueue.* TO 'vqadmin'@'localhost';" 2>/dev/null
$SUDO mysql -e "FLUSH PRIVILEGES;" 2>/dev/null

# 4. Launch Spring Boot Application
echo -e "\n[4/4] Launching Spring Boot Application..."
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

if [ -f "./mvnw" ]; then
    nohup ./mvnw spring-boot:run > virtualqueue.log 2>&1 &
else
    nohup mvn spring-boot:run > virtualqueue.log 2>&1 &
fi

echo "Waiting for Spring Boot to start on port 8080 (this may take 15-45 seconds)..."
TIMEOUT=60
COUNTER=0
STARTED=false

while [ $COUNTER -lt $TIMEOUT ]; do
    if (echo > /dev/tcp/localhost/8080) 2>/dev/null; then
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
    
    # Automatically open default browser on Linux or macOS
    if command_exists xdg-open; then
        xdg-open "http://localhost:8080" >/dev/null 2>&1 &
    elif command_exists open; then
        open "http://localhost:8080" >/dev/null 2>&1 &
    fi
else
    echo "Error: Server did not respond on port 8080 within $TIMEOUT seconds."
    echo "Please check 'virtualqueue.log' for error details."
fi
