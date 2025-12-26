# Test Registration Script
# This script tests user registration and checks if email service receives the request

$baseUrl = "http://localhost:8100/api/v1/auth"
$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$testEmail = "test+$timestamp@example.com"
$testPassword = "Test123456!"

Write-Host "=== TESTING USER REGISTRATION ===" -ForegroundColor Green
Write-Host "Test Email: $testEmail" -ForegroundColor Cyan
Write-Host "Timestamp: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host ""

# Prepare registration request
$registerBody = @{
    email = $testEmail
    password = $testPassword
    role = "CUSTOMER"
} | ConvertTo-Json

Write-Host "Sending registration request..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/register" `
        -Method POST `
        -ContentType "application/json" `
        -Body $registerBody `
        -ErrorAction Stop
    
    Write-Host "✅ Registration SUCCESS!" -ForegroundColor Green
    Write-Host "Response: $($response | ConvertTo-Json -Depth 3)" -ForegroundColor White
    Write-Host ""
    Write-Host "Please check logs for:" -ForegroundColor Yellow
    Write-Host "  1. 'Token saved successfully'" -ForegroundColor White
    Write-Host "  2. 'About to call emailVerificationService.sendActivationLink'" -ForegroundColor White
    Write-Host "  3. 'START sendActivationLink'" -ForegroundColor White
    Write-Host "  4. 'Sending activation link request to: http://localhost:8200/generate-activation'" -ForegroundColor White
    
} catch {
    Write-Host "❌ Registration FAILED!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
    Write-Host ""
    Write-Host "Please check logs for errors" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== TEST COMPLETED ===" -ForegroundColor Green
Write-Host "Check logs/fix4home.log for detailed information" -ForegroundColor Cyan

