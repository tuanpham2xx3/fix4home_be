# Fix4Home API Testing Guide - PowerShell
Write-Host "Fix4Home API Testing Guide" -ForegroundColor Cyan

# Configuration
$baseUrl = "http://localhost:8080"
$headers = @{ "Content-Type" = "application/json" }

Write-Host "`n=== 1. HEALTH CHECK ENDPOINTS ===" -ForegroundColor Yellow

# Health Check
Write-Host "`nTesting Health Check..." -ForegroundColor Green
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/api/v1/test/health" -Method GET
    Write-Host "✅ Health Check: $($health.message)" -ForegroundColor Green
    $health | ConvertTo-Json | Write-Host
} catch {
    Write-Host "❌ Health Check Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Database Check
Write-Host "`nTesting Database Check..." -ForegroundColor Green
try {
    $database = Invoke-RestMethod -Uri "$baseUrl/api/v1/test/database" -Method GET
    Write-Host "✅ Database Check: $($database.message)" -ForegroundColor Green
} catch {
    Write-Host "❌ Database Check Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 2. AUTHENTICATION ENDPOINTS ===" -ForegroundColor Yellow

# Customer Registration
Write-Host "`nTesting Customer Registration..." -ForegroundColor Green
$customerData = @{
    username = "api_test_customer"
    password = "password123"
    email = "api.test.customer@example.com"
    phoneNumber = "0111222333"
    role = "CUSTOMER"
    fullName = "API Test Customer"
} | ConvertTo-Json

try {
    $customerReg = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/register" -Method POST -Body $customerData -Headers $headers
    Write-Host "✅ Customer Registration: $($customerReg.message)" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode -eq 409) {
        Write-Host "⚠️ Customer already exists (409 Conflict)" -ForegroundColor Yellow
    } else {
        Write-Host "❌ Customer Registration Failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Customer Login
Write-Host "`nTesting Customer Login..." -ForegroundColor Green
$loginData = @{
    usernameOrEmail = "api_test_customer"
    password = "password123"
} | ConvertTo-Json

try {
    $customerLogin = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/login" -Method POST -Body $loginData -Headers $headers
    Write-Host "✅ Customer Login: $($customerLogin.message)" -ForegroundColor Green
    $global:customerToken = $customerLogin.data.accessToken
    Write-Host "   Token: $($global:customerToken.Substring(0,30))..." -ForegroundColor White
} catch {
    Write-Host "❌ Customer Login Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 3. PROTECTED ENDPOINTS ===" -ForegroundColor Yellow

if ($global:customerToken) {
    $authHeaders = @{ 
        "Authorization" = "Bearer $($global:customerToken)"
        "Content-Type" = "application/json"
    }
    
    # Customer Endpoint
    Write-Host "`nTesting Customer Protected Endpoint..." -ForegroundColor Green
    try {
        $customerEndpoint = Invoke-RestMethod -Uri "$baseUrl/api/v1/test/customer" -Method GET -Headers $authHeaders
        Write-Host "✅ Customer Endpoint: $($customerEndpoint.message)" -ForegroundColor Green
    } catch {
        Write-Host "❌ Customer Endpoint Failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    # Cross-Role Test (should fail)
    Write-Host "`nTesting Cross-Role Access (should fail)..." -ForegroundColor Green
    try {
        $techEndpoint = Invoke-RestMethod -Uri "$baseUrl/api/v1/test/technician" -Method GET -Headers $authHeaders
        Write-Host "❌ SECURITY ISSUE: Customer accessed Technician endpoint!" -ForegroundColor Red
    } catch {
        Write-Host "✅ RBAC Working: Cross-role access denied (403)" -ForegroundColor Green
    }
} else {
    Write-Host "⚠️ No token available for protected endpoint testing" -ForegroundColor Yellow
}

Write-Host "`n=== 4. ERROR HANDLING TESTS ===" -ForegroundColor Yellow

# Test Invalid Login
Write-Host "`nTesting Invalid Login..." -ForegroundColor Green
$invalidLogin = @{
    usernameOrEmail = "nonexistent"
    password = "wrongpassword"
} | ConvertTo-Json

try {
    $invalidResult = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/login" -Method POST -Body $invalidLogin -Headers $headers
    Write-Host "❌ Security Issue: Invalid login succeeded!" -ForegroundColor Red
} catch {
    Write-Host "✅ Invalid Login Correctly Rejected (401)" -ForegroundColor Green
}

# Test Unauthorized Access
Write-Host "`nTesting Unauthorized Access..." -ForegroundColor Green
try {
    $unauthorizedResult = Invoke-RestMethod -Uri "$baseUrl/api/v1/test/customer" -Method GET
    Write-Host "❌ Security Issue: Unauthorized access succeeded!" -ForegroundColor Red
} catch {
    Write-Host "✅ Unauthorized Access Correctly Blocked (401)" -ForegroundColor Green
}

Write-Host "`n=== API TESTING COMPLETE ===" -ForegroundColor Cyan
Write-Host "Use these patterns to test your APIs:" -ForegroundColor White
Write-Host "1. Always test health endpoints first" -ForegroundColor White
Write-Host "2. Test authentication flow" -ForegroundColor White
Write-Host "3. Test protected endpoints with valid tokens" -ForegroundColor White
Write-Host "4. Test cross-role access (security)" -ForegroundColor White
Write-Host "5. Test error scenarios" -ForegroundColor White 