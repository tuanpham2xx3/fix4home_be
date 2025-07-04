# Simple Payment API Test Script
# Fix4Home Payment Controller Test

Write-Host "=== FIX4HOME PAYMENT API TEST ===" -ForegroundColor Green
Write-Host ""

$baseUrl = "http://localhost:8080/api/payments"
$authUrl = "http://localhost:8080/api/v1/auth"

Write-Host "Step 1: Testing Payment Methods (Public)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/methods" -Method GET -ContentType "application/json"
    $data = $response.Content | ConvertFrom-Json
    
    if ($data.success) {
        Write-Host "✅ Payment Methods API works!" -ForegroundColor Green
        Write-Host "📋 Available Methods: $($data.data.Count)" -ForegroundColor Cyan
        foreach ($method in $data.data) {
            Write-Host "   - $($method.displayName) ($($method.method))" -ForegroundColor White
        }
    } else {
        Write-Host "❌ Payment Methods API failed: $($data.message)" -ForegroundColor Red
    }
}
catch {
    Write-Host "❌ Error calling payment methods: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "Step 2: Testing Authentication" -ForegroundColor Yellow

# Test customer login
$loginData = @{
    usernameOrEmail = "customer1@test.com"
    password = "123456"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-WebRequest -Uri "$authUrl/login" -Method POST -Body $loginData -ContentType "application/json"
    $loginResult = $loginResponse.Content | ConvertFrom-Json
    
    if ($loginResult.success) {
        Write-Host "✅ Customer login successful!" -ForegroundColor Green
        $token = $loginResult.data.token
        
        Write-Host ""
        Write-Host "Step 3: Testing Customer Payment APIs" -ForegroundColor Yellow
        
        # Test customer payment history
        $headers = @{
            "Authorization" = "Bearer $token"
            "Content-Type" = "application/json"
        }
        
        try {
            $paymentResponse = Invoke-WebRequest -Uri "$baseUrl/my" -Method GET -Headers $headers
            $paymentData = $paymentResponse.Content | ConvertFrom-Json
            
            if ($paymentData.success) {
                Write-Host "✅ Customer Payment History API works!" -ForegroundColor Green
                Write-Host "📊 Total Payments: $($paymentData.data.totalElements)" -ForegroundColor Cyan
            } else {
                Write-Host "❌ Payment History failed: $($paymentData.message)" -ForegroundColor Red
            }
        }
        catch {
            Write-Host "❌ Error getting payment history: $($_.Exception.Message)" -ForegroundColor Red
        }
        
        # Test customer payment stats
        try {
            $statsResponse = Invoke-WebRequest -Uri "$baseUrl/my/stats" -Method GET -Headers $headers
            $statsData = $statsResponse.Content | ConvertFrom-Json
            
            if ($statsData.success) {
                Write-Host "✅ Customer Payment Stats API works!" -ForegroundColor Green
                Write-Host "💰 Total Spent: $($statsData.data.totalRevenue) VNĐ" -ForegroundColor Cyan
            } else {
                Write-Host "❌ Payment Stats failed: $($statsData.message)" -ForegroundColor Red
            }
        }
        catch {
            Write-Host "❌ Error getting payment stats: $($_.Exception.Message)" -ForegroundColor Red
        }
        
    } else {
        Write-Host "❌ Customer login failed: $($loginResult.message)" -ForegroundColor Red
    }
}
catch {
    Write-Host "❌ Error during login: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "Step 4: Testing Admin APIs" -ForegroundColor Yellow

# Test admin login
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
        
        # Test admin view all payments
        try {
            $allPaymentsResponse = Invoke-WebRequest -Uri "$baseUrl/admin/all" -Method GET -Headers $adminHeaders
            $allPaymentsData = $allPaymentsResponse.Content | ConvertFrom-Json
            
            if ($allPaymentsData.success) {
                Write-Host "✅ Admin All Payments API works!" -ForegroundColor Green
                Write-Host "📊 Total Payments in System: $($allPaymentsData.data.totalElements)" -ForegroundColor Cyan
            } else {
                Write-Host "❌ Admin All Payments failed: $($allPaymentsData.message)" -ForegroundColor Red
            }
        }
        catch {
            Write-Host "❌ Error getting all payments: $($_.Exception.Message)" -ForegroundColor Red
        }
        
        # Test admin system stats
        try {
            $systemStatsResponse = Invoke-WebRequest -Uri "$baseUrl/admin/stats" -Method GET -Headers $adminHeaders
            $systemStatsData = $systemStatsResponse.Content | ConvertFrom-Json
            
            if ($systemStatsData.success) {
                Write-Host "✅ Admin System Stats API works!" -ForegroundColor Green
                Write-Host "💰 Total System Revenue: $($systemStatsData.data.totalRevenue) VNĐ" -ForegroundColor Cyan
                Write-Host "📈 Success Rate: $($systemStatsData.data.successRate)%" -ForegroundColor Cyan
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
Write-Host "=== PAYMENT API TEST COMPLETE ===" -ForegroundColor Green 