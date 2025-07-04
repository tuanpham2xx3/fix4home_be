#!/usr/bin/env powershell

# =====================================================================
# Fix4Home Admin API Test Script
# =====================================================================
# Tests all admin endpoints including:
# - System Dashboard & Overview
# - User Management
# - Bulk Operations
# - System Reports
# - Health Monitoring
# - Analytics & Performance
# =====================================================================

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$AdminToken = "",
    [switch]$Verbose
)

# Global variables
$Global:BaseUrl = $BaseUrl
$Global:Headers = @{}
$Global:TestResults = @{
    Passed = 0
    Failed = 0
    Errors = @()
}

# Color definitions
function Write-Success { param($Message) Write-Host $Message -ForegroundColor Green }
function Write-Error { param($Message) Write-Host $Message -ForegroundColor Red }
function Write-Info { param($Message) Write-Host $Message -ForegroundColor Cyan }
function Write-Warning { param($Message) Write-Host $Message -ForegroundColor Yellow }

function Test-AdminEndpoint {
    param(
        [string]$Method,
        [string]$Endpoint,
        [object]$Body = $null,
        [string]$Description,
        [int]$ExpectedStatus = 200
    )

    try {
        Write-Host "`n⚡ Testing: $Description" -ForegroundColor Blue
        Write-Host "   $Method $Endpoint" -ForegroundColor Gray

        $params = @{
            Uri = "$Global:BaseUrl$Endpoint"
            Method = $Method
            Headers = $Global:Headers
            ContentType = "application/json"
        }

        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json -Depth 10)
            if ($Verbose) {
                Write-Host "   Request Body:" -ForegroundColor Gray
                Write-Host "   $($params.Body)" -ForegroundColor DarkGray
            }
        }

        $response = Invoke-RestMethod @params
        
        if ($Verbose) {
            Write-Host "   Response:" -ForegroundColor Gray
            Write-Host "   $($response | ConvertTo-Json -Depth 3)" -ForegroundColor DarkGray
        }

        Write-Success "   ✅ SUCCESS: $Description"
        $Global:TestResults.Passed++
        return $response

    } catch {
        $errorMessage = $_.Exception.Message
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.value__
            Write-Error "   ❌ FAILED ($statusCode): $Description"
        } else {
            Write-Error "   ❌ FAILED: $Description"
        }
        Write-Error "   Error: $errorMessage"
        
        $Global:TestResults.Failed++
        $Global:TestResults.Errors += @{
            Test = $Description
            Error = $errorMessage
            Endpoint = $Endpoint
        }
        return $null
    }
}

function Setup-Authentication {
    Write-Info "🔐 Setting up admin authentication..."
    
    if ($AdminToken) {
        Write-Info "Using provided admin token"
        $Global:Headers["Authorization"] = "Bearer $AdminToken"
        return
    }

    # Try to login as admin
    try {
        $loginRequest = @{
            username = "admin"
            password = "admin123"
        }

        $loginResponse = Invoke-RestMethod -Uri "$Global:BaseUrl/api/v1/auth/login" -Method POST -Body ($loginRequest | ConvertTo-Json) -ContentType "application/json"
        
        if ($loginResponse.success -and $loginResponse.data.token) {
            $Global:Headers["Authorization"] = "Bearer $($loginResponse.data.token)"
            Write-Success "✅ Admin authentication successful"
        } else {
            throw "Login failed: Invalid response format"
        }
    } catch {
        Write-Error "❌ Admin authentication failed: $($_.Exception.Message)"
        Write-Warning "Please ensure admin user exists or provide a valid admin token with -AdminToken parameter"
        exit 1
    }
}

function Test-DashboardEndpoints {
    Write-Info "`n📊 Testing Dashboard & Overview Endpoints..."

    # Test system overview/dashboard
    Test-AdminEndpoint -Method "GET" -Endpoint "/api/admin/dashboard" -Description "Get system dashboard overview"

    # Test system health
    Test-AdminEndpoint -Method "GET" -Endpoint "/api/admin/system/health" -Description "Get system health status"

    # Test system stats
    Test-AdminEndpoint -Method "GET" -Endpoint "/api/admin/system/stats" -Description "Get quick system statistics"

    # Test performance metrics
    Test-AdminEndpoint -Method "GET" -Endpoint "/api/admin/performance/metrics" -Description "Get performance metrics"
}

function Test-UserManagementEndpoints {
    Write-Info "`n👥 Testing User Management Endpoints..."

    # Get all users (with pagination)
    Test-AdminEndpoint -Method "GET" -Endpoint "/api/admin/users?page=0`&size=10`&sortBy=createdAt`&sortDir=desc" -Description "Get all users with pagination"

    # Get users filtered by role
    Test-AdminEndpoint -Method "GET" -Endpoint "/api/admin/users?role=CUSTOMER" -Description "Get customers only"
    Test-AdminEndpoint -Method "GET" -Endpoint "/api/admin/users?role=TECHNICIAN" -Description "Get technicians only"

    # Try to get user details (using user ID 1 as example)
    $userResponse = Test-AdminEndpoint -Method "GET" -Endpoint "/api/admin/users/1" -Description "Get user details by ID"
    
    if ($userResponse) {
        $userId = $userResponse.data.id
        
        # Test update user status
        $updateStatusRequest = @{
            status = "ACTIVE"
            reason = "Admin approval"
        }
        Test-AdminEndpoint -Method "PUT" -Endpoint "/api/admin/users/$userId/status" -Body $updateStatusRequest -Description "Update user status"
    }
}

function Test-BulkOperationsEndpoints {
    Write-Info "`n🔄 Testing Bulk Operations Endpoints..."

    # Test bulk activate users
    $bulkRequest = @{
        operationType = "ACTIVATE_USERS"
        targetIds = @(1, 2, 3)
        reason = "Admin bulk activation"
    }
    Test-AdminEndpoint -Method "POST" -Endpoint "/api/admin/bulk-operations" -Body $bulkRequest -Description "Bulk activate users"

    # Test bulk approve technicians
    $bulkApproveRequest = @{
        operationType = "APPROVE_TECHNICIANS"
        targetIds = @(2, 3)
        reason = "Admin bulk technician approval"
    }
    Test-AdminEndpoint -Method "POST" -Endpoint "/api/admin/bulk-operations" -Body $bulkApproveRequest -Description "Bulk approve technicians"
}

function Test-ReportingEndpoints {
    Write-Info "`n📈 Testing Reporting Endpoints..."

    # Calculate date range (last 30 days)
    $endDate = (Get-Date).ToString("yyyy-MM-dd")
    $startDate = (Get-Date).AddDays(-30).ToString("yyyy-MM-dd")

    # Test system reports
    Test-AdminEndpoint -Method "GET" -Endpoint "/api/admin/reports/system?reportType=MONTHLY`&startDate=$startDate`&endDate=$endDate" -Description "Generate monthly system report"

    # Test user activity report
    Test-AdminEndpoint -Method "GET" -Endpoint "/api/admin/reports/users?startDate=$startDate`&endDate=$endDate" -Description "Generate user activity report"

    # Test revenue report
    Test-AdminEndpoint -Method "GET" -Endpoint "/api/admin/reports/revenue?startDate=$startDate`&endDate=$endDate" -Description "Generate revenue report"

    # Test analytics trends (last 30 days)
    Test-AdminEndpoint -Method "GET" -Endpoint "/api/admin/analytics/trends?days=30" -Description "Get 30-day analytics trends"

    # Test analytics trends (last 7 days)
    Test-AdminEndpoint -Method "GET" -Endpoint "/api/admin/analytics/trends?days=7" -Description "Get 7-day analytics trends"
}

function Test-MaintenanceEndpoints {
    Write-Info "`n🔧 Testing Maintenance Endpoints..."

    # Test system backup
    Test-AdminEndpoint -Method "POST" -Endpoint "/api/admin/maintenance/backup" -Description "Initiate system backup"

    # Test system cleanup
    Test-AdminEndpoint -Method "POST" -Endpoint "/api/admin/maintenance/cleanup" -Description "Perform system cleanup"
}

function Test-ErrorHandling {
    Write-Info "`n🚨 Testing Error Handling..."

    # Test non-existent user
    try {
        Invoke-RestMethod -Uri "$Global:BaseUrl/api/admin/users/99999" -Method GET -Headers $Global:Headers
        Write-Warning "Expected 404 for non-existent user, but got success"
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -eq 404) {
            Write-Success "✅ Correct 404 error for non-existent user"
        } else {
            Write-Warning "Expected 404, got $($_.Exception.Response.StatusCode.value__)"
        }
    }

    # Test invalid bulk operation
    $invalidBulkRequest = @{
        operationType = "INVALID_OPERATION"
        targetIds = @(1, 2)
    }
    try {
        Invoke-RestMethod -Uri "$Global:BaseUrl/api/admin/bulk-operations" -Method POST -Body ($invalidBulkRequest | ConvertTo-Json) -Headers $Global:Headers -ContentType "application/json"
        Write-Warning "Expected error for invalid bulk operation, but got success"
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -eq 400) {
            Write-Success "✅ Correct 400 error for invalid bulk operation"
        } else {
            Write-Warning "Expected 400, got $($_.Exception.Response.StatusCode.value__)"
        }
    }
}

function Show-TestSummary {
    Write-Host "`n" + "="*60 -ForegroundColor Blue
    Write-Host "🎯 ADMIN API TEST SUMMARY" -ForegroundColor Blue
    Write-Host "="*60 -ForegroundColor Blue
    
    $totalTests = $Global:TestResults.Passed + $Global:TestResults.Failed
    $successRate = if ($totalTests -gt 0) { [math]::Round(($Global:TestResults.Passed / $totalTests) * 100, 2) } else { 0 }
    
    Write-Host "📊 Total Tests: $totalTests" -ForegroundColor White
    Write-Success "✅ Passed: $($Global:TestResults.Passed)"
    
    if ($Global:TestResults.Failed -gt 0) {
        Write-Error "❌ Failed: $($Global:TestResults.Failed)"
        Write-Host "`n🚨 Failed Tests:" -ForegroundColor Red
        foreach ($error in $Global:TestResults.Errors) {
            Write-Host "   • $($error.Test)" -ForegroundColor Red
            Write-Host "     Endpoint: $($error.Endpoint)" -ForegroundColor Gray
            Write-Host "     Error: $($error.Error)" -ForegroundColor Gray
        }
    }
    
    Write-Host "`n📈 Success Rate: $successRate%" -ForegroundColor $(if ($successRate -eq 100) { "Green" } elseif ($successRate -ge 80) { "Yellow" } else { "Red" })
    
    if ($successRate -eq 100) {
        Write-Success "`n🎉 All admin API tests passed successfully!"
    } elseif ($successRate -ge 80) {
        Write-Warning "`n⚠️  Most admin API tests passed, but some issues detected."
    } else {
        Write-Error "`n💥 Significant issues detected in admin API tests."
    }
    
    Write-Host "`n" + "="*60 -ForegroundColor Blue
}

# =====================================================================
# MAIN EXECUTION
# =====================================================================

Write-Host "🚀 Fix4Home Admin API Test Suite" -ForegroundColor Magenta
Write-Host "Base URL: $BaseUrl" -ForegroundColor Gray
Write-Host "Verbose: $($Verbose.IsPresent)" -ForegroundColor Gray

# Setup authentication
Setup-Authentication

# Run all test categories
Test-DashboardEndpoints
Test-UserManagementEndpoints
Test-BulkOperationsEndpoints
Test-ReportingEndpoints
Test-MaintenanceEndpoints
Test-ErrorHandling

# Show final summary
Show-TestSummary

# Exit with appropriate code
exit $(if ($Global:TestResults.Failed -eq 0) { 0 } else { 1 }) 