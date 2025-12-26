# Script to fix registration and email sending issue
# This script helps diagnose and fix the database lock and email service issues

Write-Host "=== FIX4HOME Registration Issue Fix Script ===" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check if MySQL is running
Write-Host "Step 1: Checking MySQL connection..." -ForegroundColor Yellow
$mysqlRunning = Test-NetConnection -ComputerName localhost -Port 3307 -InformationLevel Quiet -WarningAction SilentlyContinue
if ($mysqlRunning) {
    Write-Host "  MySQL is running on port 3307" -ForegroundColor Green
} else {
    Write-Host "  ERROR: MySQL is not running on port 3307!" -ForegroundColor Red
    Write-Host "  Please start MySQL first" -ForegroundColor Yellow
    exit 1
}

# Step 2: Check if backend is running
Write-Host ""
Write-Host "Step 2: Checking backend application..." -ForegroundColor Yellow
$backendProcess = Get-NetTCPConnection -LocalPort 8100 -ErrorAction SilentlyContinue
if ($backendProcess) {
    $processId = $backendProcess | Select-Object -First 1 -ExpandProperty OwningProcess
    Write-Host "  Backend is running (PID: $processId)" -ForegroundColor Green
    
    Write-Host "  Stopping backend to clear any locks..." -ForegroundColor Yellow
    Stop-Process -Id $processId -Force
    Start-Sleep -Seconds 3
    Write-Host "  Backend stopped" -ForegroundColor Green
} else {
    Write-Host "  Backend is not running" -ForegroundColor Yellow
}

# Step 3: Check email service
Write-Host ""
Write-Host "Step 3: Checking email service..." -ForegroundColor Yellow
$emailService = Test-NetConnection -ComputerName localhost -Port 8200 -InformationLevel Quiet -WarningAction SilentlyContinue
if ($emailService) {
    Write-Host "  Email service is running on port 8200" -ForegroundColor Green
} else {
    Write-Host "  WARNING: Email service is not running on port 8200!" -ForegroundColor Red
    Write-Host "  Email sending will fail. Please start the email service." -ForegroundColor Yellow
}

# Step 4: Display MySQL fix instructions
Write-Host ""
Write-Host "Step 4: Fix MySQL locks..." -ForegroundColor Yellow
Write-Host "  Open MySQL Workbench or MySQL CLI and run:" -ForegroundColor Cyan
Write-Host "  mysql -u root -p -P 3307 < fix_database_locks.sql" -ForegroundColor White
Write-Host ""
Write-Host "  OR run these commands manually in MySQL Workbench:" -ForegroundColor Cyan
Write-Host "  1. USE fix4home_db;" -ForegroundColor White
Write-Host "  2. SHOW PROCESSLIST;" -ForegroundColor White
Write-Host "  3. Look for long-running queries (Time > 60 seconds)" -ForegroundColor White
Write-Host "  4. KILL <process_id>; (if needed)" -ForegroundColor White
Write-Host "  5. ANALYZE TABLE notifications;" -ForegroundColor White
Write-Host "  6. OPTIMIZE TABLE notifications;" -ForegroundColor White

# Step 5: Offer to restart backend
Write-Host ""
Write-Host "Step 5: Restart backend..." -ForegroundColor Yellow
$restart = Read-Host "Do you want to restart backend now? (Y/N)"
if ($restart -eq "Y" -or $restart -eq "y") {
    Write-Host "  Starting backend..." -ForegroundColor Yellow
    
    # Start in new PowerShell window
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot'; .\mvnw.cmd spring-boot:run `"-Dspring-boot.run.profiles=local`""
    
    Write-Host "  Backend is starting in a new window..." -ForegroundColor Green
    Write-Host "  Wait 10-15 seconds for it to fully start" -ForegroundColor Yellow
} else {
    Write-Host "  Skipped backend restart" -ForegroundColor Yellow
    Write-Host "  Start manually with: .\mvnw.cmd spring-boot:run `"-Dspring-boot.run.profiles=local`"" -ForegroundColor Cyan
}

# Summary
Write-Host ""
Write-Host "=== SUMMARY ===" -ForegroundColor Green
Write-Host ""
Write-Host "What was done:" -ForegroundColor Cyan
Write-Host "  Stopped backend to clear locks" -ForegroundColor White
Write-Host ""
Write-Host "What you need to do:" -ForegroundColor Cyan
Write-Host "  1. Run fix_database_locks.sql in MySQL to clear any locks" -ForegroundColor White
Write-Host "  2. Make sure email service is running (port 8200)" -ForegroundColor White
Write-Host "  3. Start backend (if not already started)" -ForegroundColor White
Write-Host "  4. Test registration with a new user" -ForegroundColor White
Write-Host ""
Write-Host "Expected behavior after fix:" -ForegroundColor Green
Write-Host "  User registration completes successfully" -ForegroundColor White
Write-Host "  Response returns to frontend immediately" -ForegroundColor White
Write-Host "  Email is sent in background (check logs)" -ForegroundColor White
Write-Host ""
Write-Host "Troubleshooting:" -ForegroundColor Yellow
Write-Host "  If still not working, check:" -ForegroundColor White
Write-Host "  - Backend logs in terminal" -ForegroundColor White
Write-Host "  - Email service logs" -ForegroundColor White
Write-Host "  - MySQL processlist for locks" -ForegroundColor White
Write-Host ""

