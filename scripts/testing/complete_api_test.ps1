# ===============================================================
# Fix4Home Complete API Testing Suite
# ===============================================================
# Author: Fix4Home Development Team
# Description: Comprehensive test suite for all Fix4Home APIs
# Version: 1.0
# Date: 2024-12-23
# ===============================================================

param(
    [string]$Mode = "FULL",
    [string]$BaseUrl = "http://localhost:8080",
    [switch]$Help
)

# ANSI Color Codes for beautiful output
$Colors = @{
    Red    = "`e[31m"
    Green  = "`e[32m"
    Yellow = "`e[33m"
    Blue   = "`e[34m"
    Purple = "`e[35m"
    Cyan   = "`e[36m"
    White  = "`e[37m"
    Reset  = "`e[0m"
    Bold   = "`e[1m"
}

# Global configuration and test data
$Global:Config = @{
    BaseUrl = $BaseUrl
    Timeout = 30
    TestMode = $Mode.ToUpper()
}

$Global:TestData = @{
    Tokens = @{ Customer = ""; Technician = ""; Admin = "" }
    UserIds = @{ Customer = 0; Technician = 0; Admin = 0 }
    EntityIds = @{ ServiceId = 0; AddressId = 0; ServiceRequestId = 0 }
    TestResults = @{ Total = 0; Passed = 0; Failed = 0 }
}

# ===============================================================
# UTILITY FUNCTIONS
# ===============================================================

function Write-Header {
    param([string]$Title, [string]$Color = "Cyan")
    $line = "=" * 80
    Write-Host ""
    Write-Host "$($Colors[$Color])$line$($Colors.Reset)" 
    Write-Host "$($Colors[$Color])$($Colors.Bold) $Title $($Colors.Reset)"
    Write-Host "$($Colors[$Color])$line$($Colors.Reset)"
}

function Write-Success {
    param([string]$Message)
    Write-Host "$($Colors.Green)✅ $Message$($Colors.Reset)"
    $Global:TestData.TestResults.Passed++
}

function Write-Error {
    param([string]$Message)
    Write-Host "$($Colors.Red)❌ $Message$($Colors.Reset)"
    $Global:TestData.TestResults.Failed++
}

function Write-Info {
    param([string]$Message)
    Write-Host "$($Colors.Blue)ℹ️  $Message$($Colors.Reset)"
}

function Write-TestCase {
    param([string]$TestName, [string]$Description)
    Write-Host ""
    Write-Host "$($Colors.Purple)🧪 TEST CASE: $TestName$($Colors.Reset)"
    Write-Host "$($Colors.White)   Description: $Description$($Colors.Reset)"
    $Global:TestData.TestResults.Total++
}

function Invoke-ApiCall {
    param(
        [string]$Method,
        [string]$Endpoint,
        [hashtable]$Headers = @{},
        [object]$Body = $null,
        [string]$Description = "",
        [int]$ExpectedStatus = 200
    )
    
    try {
        $uri = "$($Global:Config.BaseUrl)$Endpoint"
        $params = @{
            Uri = $uri
            Method = $Method
            Headers = $Headers
            TimeoutSec = $Global:Config.Timeout
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json -Depth 10)
            $params.ContentType = "application/json"
        }
        
        Write-Info "$Method $Endpoint"
        $response = Invoke-RestMethod @params
        
        Write-Success "$Description - Status: 200/201"
        return @{ Success = $true; Data = $response; StatusCode = 200 }
        
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.Value__
        $errorBody = $_.Exception.Message
        
        if ($statusCode -eq $ExpectedStatus) {
            Write-Success "$Description - Expected Status: $statusCode"
            return @{ Success = $true; Data = $null; StatusCode = $statusCode }
        } else {
            Write-Error "$Description - Status: $statusCode - $errorBody"
            return @{ Success = $false; Data = $null; StatusCode = $statusCode }
        }
    }
}

# ===============================================================
# TEST SETUP FUNCTIONS
# ===============================================================

function Initialize-TestEnvironment {
    Write-Header "INITIALIZING TEST ENVIRONMENT"
    
    Write-Info "Base URL: $($Global:Config.BaseUrl)"
    Write-Info "Test Mode: $($Global:Config.TestMode)"
    
    # Test basic connectivity
    Write-TestCase "CONNECTIVITY" "Testing basic API connectivity"
    $healthResult = Invoke-ApiCall -Method "GET" -Endpoint "/api/v1/test/health" -Description "Health Check"
    
    if (-not $healthResult.Success) {
        Write-Error "Cannot connect to API server. Please ensure the server is running."
        exit 1
    }
    
    Invoke-ApiCall -Method "GET" -Endpoint "/api/v1/test/database" -Description "Database Check"
    Write-Success "Test environment initialized successfully"
}

function Setup-TestUsers {
    Write-Header "SETTING UP TEST USERS"
    
    # Register Customer
    $customerData = @{
        username = "test_customer_main"
        password = "Test123@"
        email = "customer.main@fix4home.test"
        phoneNumber = "0901234567"
        role = "CUSTOMER"
        fullName = "Test Customer Main"
    }
    
    Write-TestCase "USER_REGISTRATION" "Registering test customer"
    Invoke-ApiCall -Method "POST" -Endpoint "/api/v1/auth/register" -Body $customerData -Description "Register Customer" -ExpectedStatus 201
    
    # Register Technician
    $technicianData = @{
        username = "test_technician_main"
        password = "Test123@"
        email = "technician.main@fix4home.test"
        phoneNumber = "0901234568"
        role = "TECHNICIAN"
        fullName = "Test Technician Main"
    }
    
    Write-TestCase "USER_REGISTRATION" "Registering test technician"
    Invoke-ApiCall -Method "POST" -Endpoint "/api/v1/auth/register" -Body $technicianData -Description "Register Technician" -ExpectedStatus 201
    
    # Login users
    $customerLogin = @{
        usernameOrEmail = "test_customer_main"
        password = "Test123@"
    }
    $customerLoginResult = Invoke-ApiCall -Method "POST" -Endpoint "/api/v1/auth/login" -Body $customerLogin -Description "Customer Login"
    if ($customerLoginResult.Success) {
        $Global:TestData.Tokens.Customer = $customerLoginResult.Data.data.accessToken
        $Global:TestData.UserIds.Customer = $customerLoginResult.Data.data.userId
        Write-Success "Customer token obtained"
    }
    
    $adminLogin = @{
        usernameOrEmail = "admin"
        password = "admin123"
    }
    $adminLoginResult = Invoke-ApiCall -Method "POST" -Endpoint "/api/v1/auth/login" -Body $adminLogin -Description "Admin Login"
    if ($adminLoginResult.Success) {
        $Global:TestData.Tokens.Admin = $adminLoginResult.Data.data.accessToken
        Write-Success "Admin token obtained"
    }
}

# ===============================================================
# API TEST FUNCTIONS
# ===============================================================

function Test-AuthenticationAPI {
    Write-Header "TESTING AUTHENTICATION API"
    
    Write-TestCase "AUTH_INVALID_LOGIN" "Testing invalid login"
    $invalidLogin = @{
        usernameOrEmail = "nonexistent"
        password = "wrongpassword"
    }
    Invoke-ApiCall -Method "POST" -Endpoint "/api/v1/auth/login" -Body $invalidLogin -Description "Invalid Login (should fail)" -ExpectedStatus 401
    
    Write-TestCase "AUTH_LOGOUT" "Testing logout functionality"
    Invoke-ApiCall -Method "POST" -Endpoint "/api/v1/auth/logout" -Description "Logout"
}

function Test-ServiceAPI {
    Write-Header "TESTING SERVICE API"
    
    $adminHeaders = @{ "Authorization" = "Bearer $($Global:TestData.Tokens.Admin)" }
    
    Write-TestCase "SERVICE_READ" "Testing service retrieval operations"
    Invoke-ApiCall -Method "GET" -Endpoint "/api/v1/services" -Description "Get All Services (Public)"
    Invoke-ApiCall -Method "GET" -Endpoint "/api/v1/services/active" -Description "Get Active Services"
    Invoke-ApiCall -Method "GET" -Endpoint "/api/v1/services/paginated?page=0&size=5" -Description "Get Services with Pagination"
    
    Write-TestCase "SERVICE_CREATE" "Testing service creation"
    $serviceData = @{
        name = "Test Plumbing Service"
        description = "Professional plumbing repair service for testing"
        basePrice = 150.00
    }
    $serviceResult = Invoke-ApiCall -Method "POST" -Endpoint "/api/v1/services" -Headers $adminHeaders -Body $serviceData -Description "Create Service" -ExpectedStatus 201
    if ($serviceResult.Success) {
        $Global:TestData.EntityIds.ServiceId = $serviceResult.Data.data.id
        Write-Success "Service created with ID: $($Global:TestData.EntityIds.ServiceId)"
    }
}

function Test-CustomerAPI {
    Write-Header "TESTING CUSTOMER API"
    
    $customerHeaders = @{ "Authorization" = "Bearer $($Global:TestData.Tokens.Customer)" }
    
    Write-TestCase "CUSTOMER_PROFILE" "Testing customer profile management"
    Invoke-ApiCall -Method "GET" -Endpoint "/api/v1/customers/profile" -Headers $customerHeaders -Description "Get My Profile"
    
    $profileUpdate = @{
        fullName = "Updated Test Customer"
        phoneNumber = "+84987654321"
        gender = "MALE"
        dob = "1990-01-15"
    }
    Invoke-ApiCall -Method "PUT" -Endpoint "/api/v1/customers/profile" -Headers $customerHeaders -Body $profileUpdate -Description "Update My Profile"
    
    Write-TestCase "CUSTOMER_ADDRESS" "Testing address management"
    $addressData = @{
        recipientName = "Test Customer"
        recipientPhone = "0901234567"
        addressLine = "123 Test Street"
        ward = "Test Ward"
        district = "Test District"
        city = "Ho Chi Minh City"
    }
    $addressResult = Invoke-ApiCall -Method "POST" -Endpoint "/api/v1/customers/addresses" -Headers $customerHeaders -Body $addressData -Description "Create Address" -ExpectedStatus 201
    if ($addressResult.Success) {
        $Global:TestData.EntityIds.AddressId = $addressResult.Data.data.id
        Write-Success "Address created with ID: $($Global:TestData.EntityIds.AddressId)"
    }
}

function Test-SecurityAndValidation {
    Write-Header "TESTING SECURITY AND VALIDATION"
    
    Write-TestCase "UNAUTHORIZED_ACCESS" "Testing unauthorized access"
    Invoke-ApiCall -Method "GET" -Endpoint "/api/v1/customers/profile" -Description "Access without token (should fail)" -ExpectedStatus 401
    
    Write-TestCase "INVALID_DATA" "Testing input validation"
    $invalidService = @{
        name = ""
        basePrice = -100
    }
    $customerHeaders = @{ "Authorization" = "Bearer $($Global:TestData.Tokens.Customer)" }
    Invoke-ApiCall -Method "POST" -Endpoint "/api/v1/services" -Headers $customerHeaders -Body $invalidService -Description "Invalid service data (should fail)" -ExpectedStatus 400
}

# ===============================================================
# MAIN EXECUTION FUNCTIONS
# ===============================================================

function Run-AllTests {
    Write-Header "FIX4HOME COMPLETE API TEST SUITE" "Green"
    
    $startTime = Get-Date
    
    Initialize-TestEnvironment
    Setup-TestUsers
    Test-AuthenticationAPI
    Test-ServiceAPI
    Test-CustomerAPI
    Test-SecurityAndValidation
    
    $endTime = Get-Date
    $duration = $endTime - $startTime
    
    Write-Header "TEST EXECUTION SUMMARY" "Green"
    Write-Host ""
    Write-Host "$($Colors.Bold)📊 TEST RESULTS:$($Colors.Reset)"
    Write-Host "   Total Tests: $($Global:TestData.TestResults.Total)"
    Write-Host "   $($Colors.Green)✅ Passed: $($Global:TestData.TestResults.Passed)$($Colors.Reset)"
    Write-Host "   $($Colors.Red)❌ Failed: $($Global:TestData.TestResults.Failed)$($Colors.Reset)"
    Write-Host ""
    Write-Host "$($Colors.Bold)⏱️  Duration: $($duration.TotalSeconds) seconds$($Colors.Reset)"
    
    $successRate = if ($Global:TestData.TestResults.Total -gt 0) { 
        [Math]::Round(($Global:TestData.TestResults.Passed / $Global:TestData.TestResults.Total) * 100, 2) 
    } else { 0 }
    
    Write-Host "$($Colors.Bold)🎯 SUCCESS RATE: $successRate%$($Colors.Reset)"
    
    if ($Global:TestData.TestResults.Failed -eq 0) {
        Write-Host ""
        Write-Host "$($Colors.Green)$($Colors.Bold)🎉 ALL TESTS PASSED! FIX4HOME API IS READY! 🚀$($Colors.Reset)"
    }
    
    Write-Header "TEST EXECUTION COMPLETED" "Green"
}

function Show-Help {
    Write-Header "FIX4HOME API TEST SUITE - HELP" "Cyan"
    Write-Host "USAGE: .\complete_api_test.ps1 [options]"
    Write-Host ""
    Write-Host "OPTIONS:"
    Write-Host "  -Mode FULL      Run all comprehensive tests (default)"
    Write-Host "  -BaseUrl URL    Override default base URL (default: http://localhost:8080)"
    Write-Host "  -Help           Show this help message"
    Write-Host ""
    Write-Host "EXAMPLES:"
    Write-Host "  .\complete_api_test.ps1"
    Write-Host "  .\complete_api_test.ps1 -BaseUrl http://192.168.1.100:8080"
}

# ===============================================================
# SCRIPT ENTRY POINT
# ===============================================================

if ($Help) {
    Show-Help
    exit 0
}

$Global:Config.BaseUrl = $BaseUrl
$Global:Config.TestMode = $Mode.ToUpper()

Run-AllTests 