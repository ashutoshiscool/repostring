<#
.SYNOPSIS
stop.ps1 - Stop Virtual Queue Application

.DESCRIPTION
This script finds and kills the java process running on port 8080.
#>

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "Stopping Virtual Queue Application..." -ForegroundColor Yellow
Write-Host "===========================================" -ForegroundColor Cyan

$process = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue

if ($null -eq $process) {
    Write-Host "No application is currently running on port 8080." -ForegroundColor Yellow
} else {
    $pidToKill = $process.OwningProcess
    Write-Host "Killing process $pidToKill running on port 8080..." -ForegroundColor Cyan
    Stop-Process -Id $pidToKill -Force -ErrorAction SilentlyContinue
    Write-Host "Application stopped successfully." -ForegroundColor Green
}
