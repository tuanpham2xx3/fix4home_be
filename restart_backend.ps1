# Script to restart backend application
Write-Host "=== RESTARTING BACKEND APPLICATION ===" -ForegroundColor Green
Write-Host ""

# Step 1: Kill existing process on port 8100
Write-Host "1. Checking for existing process on port 8100..." -ForegroundColor Yellow
$connection = Get-NetTCPConnection -LocalPort 8100 -ErrorAction SilentlyContinue
if ($connection) {
    $processId = $connection.OwningProcess
    $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
    if ($process) {
        Write-Host "   Found process: $($process.ProcessName) (PID: $processId)" -ForegroundColor Cyan
        Write-Host "   Killing process..." -ForegroundColor Yellow
        Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 2
        Write-Host "   ✅ Process killed successfully" -ForegroundColor Green
    }
} else {
    Write-Host "   ✅ Port 8100 is free" -ForegroundColor Green
}

Write-Host ""

# Step 2: Verify port is free
Write-Host "2. Verifying port 8100 is free..." -ForegroundColor Yellow
Start-Sleep -Seconds 1
$isFree = -not (Test-NetConnection -ComputerName localhost -Port 8100 -InformationLevel Quiet -WarningAction SilentlyContinue)
if ($isFree) {
    Write-Host "   ✅ Port 8100 is now free" -ForegroundColor Green
} else {
    Write-Host "   ⚠️ Port 8100 is still in use. Please wait a moment and try again." -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "=== NEXT STEPS ===" -ForegroundColor Green
Write-Host "1. Start your backend application using your IDE or Maven:" -ForegroundColor White
Write-Host "   - IDE: Run Fix4homeApplication.java" -ForegroundColor Cyan
Write-Host "   - Maven: mvn spring-boot:run" -ForegroundColor Cyan
Write-Host ""
Write-Host "2. Wait for application to start (check logs for 'Started Fix4homeApplication')" -ForegroundColor White
Write-Host ""
Write-Host "3. Test registration again" -ForegroundColor White

