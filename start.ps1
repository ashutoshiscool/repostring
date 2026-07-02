<#
.SYNOPSIS
start.ps1 - Virtual Queue Application Startup Script (Goated Edition)

.DESCRIPTION
This script ensures Java 21, Maven, and MySQL are installed before running the Spring Boot application on Windows. 
It assumes a FRESH Windows 11 install, automatically elevates to Administrator, installs missing dependencies via winget,
locates them, initializes MySQL if needed, and starts the app.
#>

# 1. Self-Elevate to Administrator
if (-Not ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host "This script needs Administrator privileges to install dependencies and configure MySQL." -ForegroundColor Yellow
    Write-Host "Restarting as Administrator..." -ForegroundColor Cyan
    try {
        Start-Process powershell -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`"" -Verb RunAs
        Exit
    } catch {
        Write-Host "Failed to elevate privileges. Please right-click and run as Administrator." -ForegroundColor Red
        Pause
        Exit
    }
}

function Pause-Exit {
    Write-Host "`nPress Enter to exit..." -ForegroundColor Yellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    Exit
}

function Refresh-EnvPath {
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
}

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "Virtual Queue - System Check & Start Script" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan

# 2. Check and Install Java 21
Write-Host "`n[1/4] Checking for Java 21..." -ForegroundColor Cyan
Refresh-EnvPath
if (Get-Command "java" -ErrorAction SilentlyContinue) {
    $javaVer = (& java -version 2>&1 | Select-String -Pattern 'version "([^"]+)"').Matches.Groups[1].Value
    if ($javaVer -notmatch "^21\.") {
        Write-Host "Found Java $javaVer, but require Java 21. Installing..." -ForegroundColor Yellow
        winget install --id Microsoft.OpenJDK.21 -e --silent --accept-package-agreements --accept-source-agreements
    } else {
        Write-Host "Java 21 is already installed." -ForegroundColor Green
    }
} else {
    Write-Host "Java not found. Installing Java 21..." -ForegroundColor Yellow
    winget install --id Microsoft.OpenJDK.21 -e --silent --accept-package-agreements --accept-source-agreements
}

# 3. Check and Install Maven
Write-Host "`n[2/4] Checking for Maven..." -ForegroundColor Cyan
Refresh-EnvPath
if (-not (Get-Command "mvn" -ErrorAction SilentlyContinue)) {
    Write-Host "Maven not found. Installing Maven..." -ForegroundColor Yellow
    winget install --id Apache.Maven -e --silent --accept-package-agreements --accept-source-agreements
} else {
    Write-Host "Maven is already installed." -ForegroundColor Green
}

# 4. Check and Install MySQL
Write-Host "`n[3/4] Checking for MySQL..." -ForegroundColor Cyan
Refresh-EnvPath
$mysqlInstalled = Get-Command "mysql" -ErrorAction SilentlyContinue

if (-not $mysqlInstalled) {
    Write-Host "MySQL not found in PATH. Checking default installation directories..." -ForegroundColor Yellow
    $mysqlBin = (Get-ChildItem -Path "C:\Program Files\MySQL" -Filter "mysql.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1).DirectoryName
    
    if ($null -eq $mysqlBin) {
        Write-Host "MySQL not installed. Installing MySQL Server via winget..." -ForegroundColor Yellow
        winget install --id Oracle.MySQL -e --silent --accept-package-agreements --accept-source-agreements
        
        Write-Host "Searching for installed MySQL..." -ForegroundColor Cyan
        $mysqlBin = (Get-ChildItem -Path "C:\Program Files\MySQL" -Filter "mysql.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1).DirectoryName
    }

    if ($null -ne $mysqlBin) {
        Write-Host "Found MySQL at $mysqlBin. Adding to PATH..." -ForegroundColor Green
        $machinePath = [System.Environment]::GetEnvironmentVariable("Path","Machine")
        [System.Environment]::SetEnvironmentVariable("Path", $machinePath + ";$mysqlBin", "Machine")
        Refresh-EnvPath
    } else {
        Write-Host "Failed to locate MySQL after installation. Please install manually." -ForegroundColor Red
        Pause-Exit
    }
} else {
    Write-Host "MySQL is already installed." -ForegroundColor Green
}

# Ensure MySQL Service is initialized and running
$mysqldPath = (Get-ChildItem -Path "C:\Program Files\MySQL" -Filter "mysqld.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1).FullName
if ($null -ne $mysqldPath) {
    $service = Get-Service -Name "MySQL*" -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($null -eq $service) {
        Write-Host "MySQL service not found. Initializing data directory (no root password)..." -ForegroundColor Yellow
        & $mysqldPath --initialize-insecure --console
        Write-Host "Installing MySQL service..." -ForegroundColor Yellow
        & $mysqldPath --install MySQL
        $service = Get-Service -Name "MySQL" -ErrorAction SilentlyContinue
    }
    
    if ($service.Status -ne 'Running') {
        Write-Host "Starting MySQL Service..." -ForegroundColor Yellow
        Start-Service -Name $service.Name
    }
}

# Configure Database
Write-Host "Configuring MySQL Database..." -ForegroundColor Cyan
try {
    # Assuming root has no password (fresh install via our script)
    mysql -u root -e "CREATE DATABASE IF NOT EXISTS virtualqueue;" 2>$null
    mysql -u root -e "CREATE USER IF NOT EXISTS 'vqadmin'@'localhost' IDENTIFIED BY 'vqpassword';" 2>$null
    mysql -u root -e "GRANT ALL PRIVILEGES ON virtualqueue.* TO 'vqadmin'@'localhost';" 2>$null
    mysql -u root -e "FLUSH PRIVILEGES;" 2>$null
    Write-Host "Database configured successfully." -ForegroundColor Green
} catch {
    Write-Host "Failed to configure database. If you set a root password manually, run this in MySQL:" -ForegroundColor Red
    Write-Host "CREATE DATABASE virtualqueue; CREATE USER 'vqadmin'@'localhost' IDENTIFIED BY 'vqpassword'; GRANT ALL PRIVILEGES ON virtualqueue.* TO 'vqadmin'@'localhost'; FLUSH PRIVILEGES;" -ForegroundColor Yellow
}

# 5. Start Spring Boot
Write-Host "`n[4/4] Starting Spring Boot in the background..." -ForegroundColor Cyan
if (Test-Path ".\mvnw.cmd") {
    Start-Process -FilePath ".\mvnw.cmd" -ArgumentList "spring-boot:run" -WindowStyle Hidden -RedirectStandardOutput "virtualqueue.log" -RedirectStandardError "virtualqueue.log"
} else {
    Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -WindowStyle Hidden -RedirectStandardOutput "virtualqueue.log" -RedirectStandardError "virtualqueue.log"
}

Write-Host "Waiting for application to start (this might take up to 60 seconds)..." -ForegroundColor Yellow
$timeout = 60
$counter = 0
while ($counter -lt $timeout) {
    $tcp = New-Object System.Net.Sockets.TcpClient
    try {
        $tcp.Connect("127.0.0.1", 8080)
        break
    } catch {
        Start-Sleep -Seconds 2
        $counter += 2
    } finally {
        if ($tcp -ne $null) { $tcp.Dispose() }
    }
}

if ($counter -ge $timeout) {
    Write-Host "Error: The application failed to start on port 8080 within 60 seconds." -ForegroundColor Red
    Write-Host "Please check 'virtualqueue.log' for details." -ForegroundColor Yellow
    Pause-Exit
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
Write-Host "✅ Application is running in the background." -ForegroundColor Green
Write-Host "ℹ️ You can now safely close this terminal." -ForegroundColor Cyan
Write-Host "🛑 To stop the server later, run: .\stop.ps1`n" -ForegroundColor Yellow

Pause-Exit
