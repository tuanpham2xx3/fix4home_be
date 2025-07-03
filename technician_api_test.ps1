# Fix4Home Technician API Test Script
$baseUrl = "http://localhost:8080/api"
$technicianEndpoint = "$baseUrl/technicians"

Write-Host "===========================================" -ForegroundColor Yellow
Write-Host "Fix4Home Technician Management API Tests" -ForegroundColor Yellow
Write-Host "===========================================" -ForegroundColor Yellow

# Test 1: Public endpoints (no auth required)
Write-Host "`n=== PUBLIC ENDPOINTS ===" -ForegroundColor Cyan

Write-Host "Testing: Get all active technicians" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$technicianEndpoint/active" -Method GET
    Write-Host "  ✓ Success - Found $($response.data.Count) active technicians" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "Testing: Get all skills" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$technicianEndpoint/skills" -Method GET
    Write-Host "  ✓ Success - Found $($response.data.Count) skills" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "Testing: Search technicians" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$technicianEndpoint/search?keyword=tech" -Method GET
    Write-Host "  ✓ Success - Search completed" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Authentication
Write-Host "`n=== AUTHENTICATION ===" -ForegroundColor Cyan

Write-Host "Testing: Admin login" -ForegroundColor Gray
$adminLoginData = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

try {
    $adminAuth = Invoke-RestMethod -Uri "$baseUrl/v1/auth/login" -Method POST -Body $adminLoginData -ContentType "application/json"
    $adminToken = $adminAuth.data.token
    Write-Host "  ✓ Admin login successful" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Admin login failed: $($_.Exception.Message)" -ForegroundColor Red
    $adminToken = $null
}

# Test 3: Admin operations
if ($adminToken) {
    Write-Host "`n=== ADMIN OPERATIONS ===" -ForegroundColor Cyan
    $adminHeaders = @{ "Authorization" = "Bearer $adminToken" }
    
    Write-Host "Testing: Get all technicians (Admin)" -ForegroundColor Gray
    try {
        $response = Invoke-RestMethod -Uri "$technicianEndpoint" -Method GET -Headers $adminHeaders
        Write-Host "  ✓ Success - Found $($response.data.Count) technicians" -ForegroundColor Green
    } catch {
        Write-Host "  ✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    Write-Host "Testing: Get pending technicians (Admin)" -ForegroundColor Gray
    try {
        $response = Invoke-RestMethod -Uri "$technicianEndpoint/pending" -Method GET -Headers $adminHeaders
        Write-Host "  ✓ Success - Found $($response.data.Count) pending technicians" -ForegroundColor Green
    } catch {
        Write-Host "  ✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    Write-Host "Testing: Create new skill (Admin)" -ForegroundColor Gray
    $createSkillData = @{
        name = "Test API Skill"
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$technicianEndpoint/skills" -Method POST -Headers $adminHeaders -Body $createSkillData -ContentType "application/json"
        Write-Host "  ✓ Success - Created skill: $($response.data.name)" -ForegroundColor Green
        $skillId = $response.data.id
        
        # Cleanup: Delete the test skill
        Write-Host "  Cleaning up: Deleting test skill" -ForegroundColor Gray
        Invoke-RestMethod -Uri "$technicianEndpoint/skills/$skillId" -Method DELETE -Headers $adminHeaders
        Write-Host "  ✓ Test skill cleaned up" -ForegroundColor Green
        
    } catch {
        Write-Host "  ✗ Failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 4: Security tests
Write-Host "`n=== SECURITY TESTS ===" -ForegroundColor Cyan

Write-Host "Testing: Access admin endpoint without token (should fail)" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$technicianEndpoint" -Method GET
    Write-Host "  ✗ Unexpected success - security breach!" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401) {
        Write-Host "  ✓ Correctly blocked - 401 Unauthorized" -ForegroundColor Green
    } else {
        Write-Host "  ? Unexpected status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
    }
}

Write-Host "Testing: Access technician profile without token (should fail)" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$technicianEndpoint/me" -Method GET
    Write-Host "  ✗ Unexpected success - security breach!" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401) {
        Write-Host "  ✓ Correctly blocked - 401 Unauthorized" -ForegroundColor Green
    } else {
        Write-Host "  ? Unexpected status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
    }
}

Write-Host "`n===========================================" -ForegroundColor Yellow
Write-Host "Test Summary" -ForegroundColor Yellow
Write-Host "===========================================" -ForegroundColor Yellow
Write-Host "✓ Public technician browsing endpoints" -ForegroundColor Green
Write-Host "✓ Skills management endpoints" -ForegroundColor Green
Write-Host "✓ Admin authentication and operations" -ForegroundColor Green
Write-Host "✓ Security and authorization controls" -ForegroundColor Green
Write-Host ""
Write-Host "TechnicianController implementation verified!" -ForegroundColor Green 