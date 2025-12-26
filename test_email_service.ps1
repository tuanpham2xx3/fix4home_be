# Simple Test Script - Check if email service is receiving requests
Write-Host "=== CHECKING EMAIL SERVICE STATUS ===" -ForegroundColor Green
Write-Host ""

# Test 1: Health Check
Write-Host "1. Testing Email Service Health Check..." -ForegroundColor Yellow
try {
    $healthResponse = Invoke-RestMethod -Uri "http://localhost:8200/health" -Method GET -ErrorAction Stop
    Write-Host "   ✅ Email Service is HEALTHY" -ForegroundColor Green
    Write-Host "   Response: $($healthResponse | ConvertTo-Json)" -ForegroundColor White
} catch {
    Write-Host "   ❌ Email Service Health Check FAILED" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 2: Check Backend Logs
Write-Host "2. Checking recent logs for email service calls..." -ForegroundColor Yellow
$logFile = "logs\fix4home.log"
if (Test-Path $logFile) {
    $recentLogs = Get-Content $logFile -Tail 100 | Select-String -Pattern "sendActivationLink|emailVerificationService|Email service" -Context 1
    if ($recentLogs) {
        Write-Host "   Recent email service related logs:" -ForegroundColor Cyan
        $recentLogs | ForEach-Object { Write-Host "   $_" -ForegroundColor White }
    } else {
        Write-Host "   ⚠️ No recent email service calls found in logs" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ⚠️ Log file not found: $logFile" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== NEXT STEPS ===" -ForegroundColor Green
Write-Host "1. Try registering a new user via API or frontend" -ForegroundColor White
Write-Host "2. Check logs/fix4home.log for:" -ForegroundColor White
Write-Host "   - 'Token saved successfully'" -ForegroundColor Cyan
Write-Host "   - 'About to call emailVerificationService.sendActivationLink'" -ForegroundColor Cyan
Write-Host "   - 'START sendActivationLink'" -ForegroundColor Cyan
Write-Host "   - 'Sending activation link request to: http://localhost:8200/generate-activation'" -ForegroundColor Cyan

