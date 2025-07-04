# Simple Feedback API Test Script
Write-Host "=== FEEDBACK API TEST ===" -ForegroundColor Green

$baseUrl = "http://localhost:8080/api/feedbacks"
$authUrl = "http://localhost:8080/api/v1/auth"

Write-Host "Step 1: Testing Admin Login" -ForegroundColor Yellow
$adminLoginData = @{
    usernameOrEmail = "admin"
    password = "123456"
} | ConvertTo-Json

try {
    $adminLoginResponse = Invoke-WebRequest -Uri "$authUrl/login" -Method POST -Body $adminLoginData -ContentType "application/json"
    $adminLoginResult = $adminLoginResponse.Content | ConvertFrom-Json
    
    if ($adminLoginResult.success) {
        Write-Host "✅ Admin login successful!" -ForegroundColor Green
        $adminToken = $adminLoginResult.data.token
        
        $adminHeaders = @{
            "Authorization" = "Bearer $adminToken"
            "Content-Type" = "application/json"
        }
        
        Write-Host ""
        Write-Host "Step 2: Testing Admin Feedback APIs" -ForegroundColor Yellow
        
        # Test admin view all feedbacks
        try {
            $allFeedbacksResponse = Invoke-WebRequest -Uri "$baseUrl/admin/all" -Method GET -Headers $adminHeaders
            $allFeedbacksData = $allFeedbacksResponse.Content | ConvertFrom-Json
            
            if ($allFeedbacksData.success) {
                Write-Host "✅ Admin All Feedbacks API works!" -ForegroundColor Green
                Write-Host "📊 Total Feedbacks: $($allFeedbacksData.data.totalElements)" -ForegroundColor Cyan
            } else {
                Write-Host "❌ Admin All Feedbacks failed: $($allFeedbacksData.message)" -ForegroundColor Red
            }
        }
        catch {
            Write-Host "❌ Error getting all feedbacks: $($_.Exception.Message)" -ForegroundColor Red
        }
        
        # Test admin system stats
        try {
            $systemStatsResponse = Invoke-WebRequest -Uri "$baseUrl/admin/stats" -Method GET -Headers $adminHeaders
            $systemStatsData = $systemStatsResponse.Content | ConvertFrom-Json
            
            if ($systemStatsData.success) {
                Write-Host "✅ Admin System Stats API works!" -ForegroundColor Green
                Write-Host "📈 Average Rating: $($systemStatsData.data.averageRating)" -ForegroundColor Cyan
                Write-Host "🔢 Total Feedbacks: $($systemStatsData.data.totalFeedbacks)" -ForegroundColor Cyan
            } else {
                Write-Host "❌ System Stats failed: $($systemStatsData.message)" -ForegroundColor Red
            }
        }
        catch {
            Write-Host "❌ Error getting system stats: $($_.Exception.Message)" -ForegroundColor Red
        }
        
    } else {
        Write-Host "❌ Admin login failed: $($adminLoginResult.message)" -ForegroundColor Red
    }
}
catch {
    Write-Host "❌ Error during admin login: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== FEEDBACK API TEST COMPLETE ===" -ForegroundColor Green 