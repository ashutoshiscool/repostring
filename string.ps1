<#
.SYNOPSIS
string.ps1 - Virtual Queue Spring Boot Application Startup Script for Windows 11

.DESCRIPTION
This script automatically prepares and launches the Virtual Queue Spring Boot Application on Windows 11.
It checks for Java 21, Maven, and MySQL Server, installs any missing dependencies using Windows Package Manager (winget),
configures the MySQL database, starts the Spring Boot server, and automatically opens http://localhost:8080 in your default browser.
#>

# 1. Elevate to Administrator if not already running as Admin
if (-Not ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host "This script requires Administrator privileges to setup MySQL and launch the Spring Boot application." -ForegroundColor Yellow
    Write-Host "Restarting PowerShell as Administrator..." -ForegroundColor Cyan
    try {
        Start-Process powershell -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`"" -Verb RunAs
        Exit
    } catch {
        Write-Host "Failed to elevate privileges. Please right-click string.ps1 and select 'Run with PowerShell' as Administrator." -ForegroundColor Red
        Write-Host "Press Enter to exit..." -ForegroundColor Yellow
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
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

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Virtual Queue System - Windows 11 Spring Boot Startup" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 2. Check and Install Java 21
Write-Host "`n[1/4] Checking Java 21 Environment..." -ForegroundColor Cyan
Refresh-EnvPath
if (Get-Command "java" -ErrorAction SilentlyContinue) {
    $javaVer = (& java -version 2>&1 | Select-String -Pattern 'version "([^"]+)"').Matches.Groups[1].Value
    if ($javaVer -notmatch "^21\.") {
        Write-Host "Found Java $javaVer. Installing OpenJDK 21 via winget..." -ForegroundColor Yellow
        winget install --id Microsoft.OpenJDK.21 -e --silent --accept-package-agreements --accept-source-agreements
    } else {
        Write-Host "Java 21 is ready." -ForegroundColor Green
    }
} else {
    Write-Host "Java not found. Installing OpenJDK 21 via winget..." -ForegroundColor Yellow
    winget install --id Microsoft.OpenJDK.21 -e --silent --accept-package-agreements --accept-source-agreements
}

# 3. Check and Install Maven
Write-Host "`n[2/4] Checking Apache Maven..." -ForegroundColor Cyan
Refresh-EnvPath
if (-not (Get-Command "mvn" -ErrorAction SilentlyContinue)) {
    Write-Host "Installing Apache Maven via winget..." -ForegroundColor Yellow
    winget install --id Apache.Maven -e --silent --accept-package-agreements --accept-source-agreements
} else {
    Write-Host "Maven is ready." -ForegroundColor Green
}

# 4. Check and Install MySQL Server
Write-Host "`n[3/4] Checking MySQL Database Server..." -ForegroundColor Cyan
Refresh-EnvPath
$mysqlInstalled = Get-Command "mysql" -ErrorAction SilentlyContinue

if (-not $mysqlInstalled) {
    $mysqlBin = (Get-ChildItem -Path "C:\Program Files\MySQL" -Filter "mysql.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1).DirectoryName
    if ($null -eq $mysqlBin) {
        Write-Host "Installing MySQL Server via winget..." -ForegroundColor Yellow
        winget install --id Oracle.MySQL -e --silent --accept-package-agreements --accept-source-agreements
        $mysqlBin = (Get-ChildItem -Path "C:\Program Files\MySQL" -Filter "mysql.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1).DirectoryName
    }

    if ($null -ne $mysqlBin) {
        Write-Host "Adding MySQL to System PATH..." -ForegroundColor Green
        $machinePath = [System.Environment]::GetEnvironmentVariable("Path","Machine")
        [System.Environment]::SetEnvironmentVariable("Path", $machinePath + ";$mysqlBin", "Machine")
        Refresh-EnvPath
    }
}

# Initialize and Start MySQL Service
$mysqldPath = (Get-ChildItem -Path "C:\Program Files\MySQL" -Filter "mysqld.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1).FullName
if ($null -ne $mysqldPath) {
    $service = Get-Service -Name "MySQL*" -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($null -eq $service) {
        Write-Host "Initializing MySQL data directory..." -ForegroundColor Yellow
        & $mysqldPath --initialize-insecure --console
        & $mysqldPath --install MySQL
        $service = Get-Service -Name "MySQL" -ErrorAction SilentlyContinue
    }
    
    if ($service.Status -ne 'Running') {
        Write-Host "Starting MySQL Service..." -ForegroundColor Yellow
        Start-Service -Name $service.Name
    }
}

# Configure Database and User
Write-Host "Setting up Database 'virtualqueue' and User 'vqadmin'..." -ForegroundColor Cyan
try {
    mysql -u root -e "CREATE DATABASE IF NOT EXISTS virtualqueue;" 2>$null
    mysql -u root -e "CREATE USER IF NOT EXISTS 'vqadmin'@'localhost' IDENTIFIED BY 'vqpassword';" 2>$null
    mysql -u root -e "GRANT ALL PRIVILEGES ON virtualqueue.* TO 'vqadmin'@'localhost';" 2>$null
    mysql -u root -e "FLUSH PRIVILEGES;" 2>$null
    Write-Host "MySQL Database setup completed." -ForegroundColor Green
} catch {
    Write-Host "Database configuration warning. Ensure MySQL root user has access." -ForegroundColor Yellow
}

# 5. Launch Spring Boot Application
Write-Host "`n[4/4] Launching Spring Boot Application..." -ForegroundColor Cyan
$projectDir = $PSScriptRoot
Set-Location -Path $projectDir

if (Test-Path ".\mvnw.cmd") {
    Start-Process -FilePath ".\mvnw.cmd" -ArgumentList "spring-boot:run" -WindowStyle Hidden -RedirectStandardOutput "$projectDir\virtualqueue.log" -RedirectStandardError "$projectDir\virtualqueue.log"
} else {
    Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -WindowStyle Hidden -RedirectStandardOutput "$projectDir\virtualqueue.log" -RedirectStandardError "$projectDir\virtualqueue.log"
}

Write-Host "Waiting for Spring Boot to start on port 8080 (this may take 15-45 seconds)..." -ForegroundColor Yellow
$timeout = 60
$counter = 0
$started = $false

while ($counter -lt $timeout) {
    $tcp = New-Object System.Net.Sockets.TcpClient
    try {
        $tcp.Connect("127.0.0.1", 8080)
        $started = $true
        break
    } catch {
        Start-Sleep -Seconds 2
        $counter += 2
        Write-Host "." -NoNewline -ForegroundColor Gray
    } finally {
        if ($tcp -ne $null) { $tcp.Dispose() }
    }
}

Write-Host ""

if ($started) {
    Write-Host "==========================================================" -ForegroundColor Cyan
    Write-Host "SPRING BOOT APPLICATION IS NOW LIVE!" -ForegroundColor Green
    Write-Host "Opening http://localhost:8080 in your default browser..." -ForegroundColor Green
    Write-Host "==========================================================" -ForegroundColor Cyan
    Write-Host "Default Test Credentials:" -ForegroundColor Cyan
    Write-Host "  - Admin Portal:   admin@hospital.com / admin123"
    Write-Host "  - Doctor Portal:  sanjay.thapa@rajdhanihealthline.com / doctor123"
    Write-Host "  - Patient Portal: aarav.sharma1@gmail.com / patient123"
    Write-Host "==========================================================" -ForegroundColor Cyan
    
    # Automatically open default browser on Windows 11
    Start-Process "http://localhost:8080"
} else {
    Write-Host "Error: Server did not respond on port 8080 within $timeout seconds." -ForegroundColor Red
    Write-Host "Please check 'virtualqueue.log' for error details." -ForegroundColor Yellow
}

Pause-Exit
