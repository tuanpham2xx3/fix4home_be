# Simple Feedback Test
Write-Host "=== FEEDBACK API TEST ===" -ForegroundColor Green

$authUrl = "http://localhost:8080/api/v1/auth"
$feedbackUrl = "http://localhost:8080/api/feedbacks"

Write-Host "Testing Admin Login..." -ForegroundColor Yellow
$loginData = @{
    usernameOrEmail = "admin"
    password = "123456"
} | ConvertTo-Json

$loginResponse = Invoke-WebRequest -Uri "$authUrl/login" -Method POST -Body $loginData -ContentType "application/json"
$loginResult = $loginResponse.Content | ConvertFrom-Json

if ($loginResult.success) {
    Write-Host "✅ Admin login successful!" -ForegroundColor Green
    $token = $loginResult.data.token
    
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    Write-Host "Testing Admin Feedbacks API..." -ForegroundColor Yellow
    $feedbackResponse = Invoke-WebRequest -Uri "$feedbackUrl/admin/all" -Method GET -Headers $headers
    $feedbackData = $feedbackResponse.Content | ConvertFrom-Json
    
    if ($feedbackData.success) {
        Write-Host "✅ Admin Feedbacks API works!" -ForegroundColor Green
        Write-Host "📊 Total Feedbacks: $($feedbackData.data.totalElements)" -ForegroundColor Cyan
    } else {
        Write-Host "❌ Admin Feedbacks API failed: $($feedbackData.message)" -ForegroundColor Red
    }
} else {
    Write-Host "❌ Admin login failed: $($loginResult.message)" -ForegroundColor Red
}

Write-Host "=== TEST COMPLETE ===" -ForegroundColor Green 