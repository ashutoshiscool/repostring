<#
.SYNOPSIS
start.ps1 - Virtual Queue Application Startup Script

.DESCRIPTION
This script ensures Java 21, Maven, and MySQL are installed before running the Spring Boot application on Windows.
#>

function Pause-Exit {
    Write-Host "`nPress Enter to exit..." -ForegroundColor Yellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    Exit
}

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
        Pause-Exit
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
        Pause-Exit
    }
}

Write-Host "Checking for MySQL Server..." -ForegroundColor Cyan
if (-not (Test-CommandExists "mysql")) {
    Write-Host "MySQL is not installed. Attempting to install MySQL Server via winget..." -ForegroundColor Yellow
    if (Test-CommandExists "winget") {
        winget install --id Oracle.MySQL --silent --accept-package-agreements --accept-source-agreements
        $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
        
        # In Windows, MySQL usually starts automatically as a service (e.g. MySQL80).
        # We give it a few seconds to initialize.
        Start-Sleep -Seconds 10
    } else {
        Write-Host "winget is not installed. Please install MySQL manually." -ForegroundColor Red
        Pause-Exit
    }
}

Write-Host "Configuring MySQL Database and User..." -ForegroundColor Cyan
if (Test-CommandExists "mysql") {
    # Default MySQL installation on Windows without root password can be accessed directly
    try {
        mysql -u root -e "CREATE DATABASE IF NOT EXISTS virtualqueue;"
        mysql -u root -e "CREATE USER IF NOT EXISTS 'vqadmin'@'localhost' IDENTIFIED BY 'vqpassword';"
        mysql -u root -e "GRANT ALL PRIVILEGES ON virtualqueue.* TO 'vqadmin'@'localhost';"
        mysql -u root -e "FLUSH PRIVILEGES;"
        Write-Host "MySQL configured successfully." -ForegroundColor Green
    } catch {
        Write-Host "Failed to configure MySQL automatically. You might need to set it up manually or provide the root password if you set one." -ForegroundColor Red
        Write-Host "Run: mysql -u root -p -e `"CREATE DATABASE virtualqueue; CREATE USER 'vqadmin'@'localhost' IDENTIFIED BY 'vqpassword'; GRANT ALL PRIVILEGES ON virtualqueue.* TO 'vqadmin'@'localhost'; FLUSH PRIVILEGES;`"" -ForegroundColor Yellow
        Pause-Exit
    }
} else {
    Write-Host "MySQL command not found in PATH even after installation attempt. Please restart your terminal or configure manually." -ForegroundColor Yellow
    Pause-Exit
}

Write-Host "Starting Spring Boot in the background..." -ForegroundColor Cyan

if (Test-Path ".\mvnw.cmd") {
    Start-Process -FilePath ".\mvnw.cmd" -ArgumentList "spring-boot:run" -WindowStyle Hidden -RedirectStandardOutput "virtualqueue.log" -RedirectStandardError "virtualqueue.log"
} else {
    Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -WindowStyle Hidden -RedirectStandardOutput "virtualqueue.log" -RedirectStandardError "virtualqueue.log"
}

Write-Host "Waiting for application to start (this might take a few seconds)..." -ForegroundColor Yellow

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

Write-Host "Press Enter to close this window..." -ForegroundColor DarkGray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
