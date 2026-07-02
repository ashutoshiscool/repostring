<#
.SYNOPSIS
start.ps1 - Virtual Queue Application Startup Script

.DESCRIPTION
This script ensures Java 21 and Maven are installed before running the Spring Boot application on Windows.
#>

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "Virtual Queue - System Check & Start Script" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan

# Function to check if a command exists
function Test-CommandExists {
    param ($command)
    $null = Get-Command $command -ErrorAction SilentlyContinue
    return $?
}

$installJava = $false
$installMaven = $false

# Check for Java
Write-Host "Checking for Java 21..."
if (Test-CommandExists "java") {
    $javaVersionOutput = & java -version 2>&1
    $javaVersion = ($javaVersionOutput | Select-String -Pattern 'version "([^"]+)"').Matches.Groups[1].Value
    Write-Host "Found Java version: $javaVersion" -ForegroundColor Green
    if ($javaVersion -notmatch "^21\.") {
        Write-Host "Java version is not 21. Will attempt to install Java 21..." -ForegroundColor Yellow
        $installJava = $true
    } else {
        Write-Host "Java 21 is installed." -ForegroundColor Green
    }
} else {
    Write-Host "Java is not installed. Will attempt to install Java 21..." -ForegroundColor Yellow
    $installJava = $true
}

# Install Java if needed
if ($installJava) {
    if (Test-CommandExists "winget") {
        Write-Host "Installing Microsoft OpenJDK 21 via winget..." -ForegroundColor Cyan
        winget install --id Microsoft.OpenJDK.21 --source winget --accept-package-agreements --accept-source-agreements
        # Refresh environment variables
        $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
    } else {
        Write-Host "winget is not installed. Please install Java 21 manually from https://learn.microsoft.com/en-us/java/openjdk/download" -ForegroundColor Red
        Exit
    }
}

# Check for Maven
Write-Host "Checking for Maven..."
if (Test-CommandExists "mvn") {
    Write-Host "Maven is installed." -ForegroundColor Green
} else {
    Write-Host "Maven is not installed. Will attempt to install Maven..." -ForegroundColor Yellow
    $installMaven = $true
}

# Install Maven if needed
if ($installMaven) {
    if (Test-CommandExists "winget") {
        Write-Host "Installing Maven via winget..." -ForegroundColor Cyan
        winget install --id Apache.Maven --source winget --accept-package-agreements --accept-source-agreements
        # Refresh environment variables
        $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
    } else {
        Write-Host "winget is not installed. Please install Maven manually from https://maven.apache.org/download.cgi" -ForegroundColor Red
        Exit
    }
}

Write-Host "Configuring MySQL Database and User..." -ForegroundColor Cyan
if (Test-CommandExists "mysql") {
    mysql -u root -e "CREATE DATABASE IF NOT EXISTS virtualqueue;"
    mysql -u root -e "CREATE USER IF NOT EXISTS 'vqadmin'@'localhost' IDENTIFIED BY 'vqpassword';"
    mysql -u root -e "GRANT ALL PRIVILEGES ON virtualqueue.* TO 'vqadmin'@'localhost';"
    mysql -u root -e "FLUSH PRIVILEGES;"
    Write-Host "MySQL configured successfully." -ForegroundColor Green
} else {
    Write-Host "MySQL command not found. Please ensure MySQL is installed and in your PATH, and manually create the 'virtualqueue' database." -ForegroundColor Yellow
}

Write-Host "Starting Spring Boot in the background..." -ForegroundColor Cyan

# Use maven wrapper if it exists, otherwise fallback to mvn
if (Test-Path ".\mvnw.cmd") {
    Start-Process -FilePath ".\mvnw.cmd" -ArgumentList "spring-boot:run" -WindowStyle Hidden -RedirectStandardOutput "virtualqueue.log" -RedirectStandardError "virtualqueue.log"
} else {
    Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -WindowStyle Hidden -RedirectStandardOutput "virtualqueue.log" -RedirectStandardError "virtualqueue.log"
}

Write-Host "Waiting for application to start (this might take a few seconds)..." -ForegroundColor Yellow

# Wait for the server to start on port 8080
while ($true) {
    $tcp = New-Object System.Net.Sockets.TcpClient
    try {
        $tcp.Connect("127.0.0.1", 8080)
        break
    } catch {
        Start-Sleep -Seconds 2
    } finally {
        if ($tcp -ne $null) { $tcp.Dispose() }
    }
}

Write-Host "`n=======================================================" -ForegroundColor Cyan
Write-Host "🚀 APPLICATION IS NOW LIVE!" -ForegroundColor Green
Write-Host "🌐 Access it at: http://localhost:8080" -ForegroundColor Green
Write-Host ""
Write-Host "Test Accounts:" -ForegroundColor Cyan
Write-Host "  - Admin:   admin@hospital.com / admin123"
Write-Host "  - Doctor:  sanjay.thapa@rajdhanihealthline.com / doctor123"
Write-Host "  - Patient: [firstname].[lastname]1@gmail.com / patient123 (e.g. aarav.sharma1@gmail.com)"
Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host " Application is running in the background." -ForegroundColor Green
Write-Host "ℹ  You can now safely close this terminal." -ForegroundColor Cyan
Write-Host "To stop the server later, run: .\stop.ps1`n" -ForegroundColor Yellow
