# Simple Notification API Test
Write-Host "Testing Notification API..." -ForegroundColor Green

$baseUrl = "http://localhost:8080"

try {
    $testResponse = Invoke-RestMethod -Uri "$baseUrl/api/notifications/unread-count" -Method GET
    Write-Host "Notification API is accessible" -ForegroundColor Green
}
catch {
    Write-Host "Error testing API: $($_.Exception.Message)" -ForegroundColor Red
} 