#!/bin/bash

# start.sh for Virtual Queue Application
# This script ensures Java 21 and Maven are installed before running the Spring Boot application.

echo "==========================================="
echo "Virtual Queue - System Check & Start Script"
echo "==========================================="

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Ensure we are running with sudo if we need to install packages
SUDO=""
if command_exists sudo; then
    SUDO="sudo"
fi

# Detect OS
OS="Unknown"
if [ -f /etc/os-release ]; then
    . /etc/os-release
    OS=$ID
fi

# Check Java
echo "Checking for Java 21..."
if command_exists java; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo "Found Java version: $JAVA_VERSION"
    # Basic check if it starts with 21
    if [[ "$JAVA_VERSION" == 21* ]]; then
        echo "Java 21 is installed."
    else
        echo "Java version is not 21. Attempting to install Java 21..."
        INSTALL_JAVA=true
    fi
else
    echo "Java is not installed. Attempting to install Java 21..."
    INSTALL_JAVA=true
fi

if [ "$INSTALL_JAVA" = true ]; then
    if [[ "$OS" == "ubuntu" || "$OS" == "debian" ]]; then
        echo "Installing OpenJDK 21 via apt..."
        $SUDO apt-get update
        $SUDO apt-get install -y openjdk-21-jdk
    elif [[ "$OS" == "fedora" || "$OS" == "centos" || "$OS" == "rhel" ]]; then
        echo "Installing OpenJDK 21 via dnf/yum..."
        if command_exists dnf; then
            $SUDO dnf install -y java-21-openjdk-devel
        else
            $SUDO yum install -y java-21-openjdk-devel
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        echo "Installing OpenJDK 21 via Homebrew..."
        if command_exists brew; then
            brew install openjdk@21
        else
            echo "Homebrew not found. Please install Java 21 manually."
            exit 1
        fi
    else
        echo "Unsupported OS for automatic Java installation. Please install Java 21 manually."
        exit 1
    fi
fi

# Check Maven
echo "Checking for Maven..."
if command_exists mvn; then
    echo "Maven is installed."
else
    echo "Maven is not installed. Attempting to install Maven..."
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
    else
        echo "Unsupported OS for automatic Maven installation. Please install Maven manually."
        exit 1
    fi
fi

# Check MySQL
echo "Checking for MySQL Server..."
if command_exists mysql; then
    echo "MySQL is installed."
else
    echo "MySQL is not installed. Attempting to install MySQL Server..."
    if [[ "$OS" == "ubuntu" || "$OS" == "debian" ]]; then
        $SUDO apt-get update
        $SUDO apt-get install -y mysql-server
    elif [[ "$OS" == "fedora" || "$OS" == "centos" || "$OS" == "rhel" ]]; then
        if command_exists dnf; then
            $SUDO dnf install -y mysql-server
        else
            $SUDO yum install -y mysql-server
        fi
    else
        echo "Unsupported OS for automatic MySQL installation. Please install MySQL manually."
        exit 1
    fi
fi

# Ensure MySQL is running
echo "Ensuring MySQL is running..."
if [[ "$OS" == "ubuntu" || "$OS" == "debian" ]]; then
    $SUDO service mysql start
elif [[ "$OS" == "fedora" || "$OS" == "centos" || "$OS" == "rhel" ]]; then
    $SUDO systemctl start mysqld
fi

# Setup Database and User
echo "Configuring MySQL Database and User..."
$SUDO mysql -e "CREATE DATABASE IF NOT EXISTS virtualqueue;"
$SUDO mysql -e "CREATE USER IF NOT EXISTS 'vqadmin'@'localhost' IDENTIFIED BY 'vqpassword';"
$SUDO mysql -e "GRANT ALL PRIVILEGES ON virtualqueue.* TO 'vqadmin'@'localhost';"
$SUDO mysql -e "FLUSH PRIVILEGES;"

echo "Starting Spring Boot in the background..."

# Use maven wrapper if it exists, otherwise fallback to mvn
if [ -f "./mvnw" ]; then
    nohup ./mvnw spring-boot:run > virtualqueue.log 2>&1 &
else
    nohup mvn spring-boot:run > virtualqueue.log 2>&1 &
fi

echo "Waiting for application to start (this might take a few seconds)..."

# Wait for the server to start on port 8080
while ! (echo > /dev/tcp/localhost/8080) 2>/dev/null; do
    sleep 2
done

echo -e "\n======================================================="
echo -e "🚀 APPLICATION IS NOW LIVE!"
echo -e "🌐 Access it at: http://localhost:8080"
echo -e ""
echo -e "Test Accounts:"
echo -e "  - Admin:   admin@hospital.com / admin123"
echo -e "  - Doctor:  sanjay.thapa@rajdhanihealthline.com / doctor123"
echo -e "  - Patient: [firstname].[lastname]1@gmail.com / patient123 (e.g. aarav.sharma1@gmail.com)"
echo -e "======================================================="
echo -e "✅ Application is running in the background."
echo -e "ℹ️  You can now safely close this terminal."
echo -e "🛑 To stop the server later, run: ./stop.sh\n"
