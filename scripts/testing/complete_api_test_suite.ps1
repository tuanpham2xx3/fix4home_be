# ===============================================================
# Fix4Home Complete API Testing Suite
# ===============================================================
# Author: Fix4Home Development Team
# Description: Comprehensive test suite for all Fix4Home APIs
# Version: 1.0
# ===============================================================

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Mode = "FULL",
    [switch]$Help
)

# ANSI Color Codes
$Colors = @{
    Red = "`e[31m"; Green = "`e[32m"; Yellow = "`e[33m"
    Blue = "`e[34m"; Purple = "`e[35m"; Cyan = "`e[36m"
    White = "`e[37m"; Reset = "`e[0m"; Bold = "`e[1m"
}

# Global test state
$Global:TestResults = @{ Total = 0; Passed = 0; Failed = 0 }
$Global:Tokens = @{ Customer = ""; Technician = ""; Admin = "" }
$Global:EntityIds = @{ ServiceId = 0; AddressId = 0; ServiceRequestId = 0 }

# ===============================================================
# UTILITY FUNCTIONS
# ===============================================================

function Write-Header($Title, $Color = "Cyan") {
    $line = "=" * 80
    Write-Host ""
    Write-Host "$($Colors[$Color])$line$($Colors.Reset)"
    Write-Host "$($Colors[$Color])$($Colors.Bold) $Title $($Colors.Reset)"
    Write-Host "$($Colors[$Color])$line$($Colors.Reset)"
}

function Write-Success($Message) {
    Write-Host "$($Colors.Green)✅ $Message$($Colors.Reset)"
    $Global:TestResults.Passed++
}

function Write-Error($Message) {
    Write-Host "$($Colors.Red)❌ $Message$($Colors.Reset)"
    $Global:TestResults.Failed++
}

function Write-Info($Message) {
    Write-Host "$($Colors.Blue)ℹ️  $Message$($Colors.Reset)"
}

function Write-TestCase($TestName, $Description) {
    Write-Host ""
    Write-Host "$($Colors.Purple)🧪 TEST: $TestName$($Colors.Reset)"
    Write-Host "$($Colors.White)   $Description$($Colors.Reset)"
    $Global:TestResults.Total++
}

function Test-ApiEndpoint($Method, $Endpoint, $Body = $null, $Headers = @{}, $Description, $ExpectedStatus = 200) {
    try {
        $params = @{
            Uri = "$BaseUrl$Endpoint"
            Method = $Method
            Headers = $Headers
            ContentType = "application/json"
            TimeoutSec = 30
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json -Depth 10)
        }
        
        Write-Info "$Method $Endpoint"
        $response = Invoke-RestMethod @params
        
        Write-Success "$Description - Status: 200/201"
        return @{ Success = $true; Data = $response; StatusCode = 200 }
        
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.Value__
        $errorMessage = $_.Exception.Message
        
        if ($statusCode -eq $ExpectedStatus) {
            Write-Success "$Description - Expected Status: $statusCode"
            return @{ Success = $true; Data = $null; StatusCode = $statusCode }
        } else {
            Write-Error "$Description - Error: $statusCode - $errorMessage"
            return @{ Success = $false; Data = $null; StatusCode = $statusCode }
        }
    }
}

# ===============================================================
# TEST FUNCTIONS
# ===============================================================

function Test-HealthCheck {
    Write-Header "HEALTH CHECK TESTS"
    
    Write-TestCase "CONNECTIVITY" "Testing basic API connectivity"
    $healthResult = Test-ApiEndpoint "GET" "/api/v1/test/health" -Description "Health Check"
    
    if (-not $healthResult.Success) {
        Write-Error "Cannot connect to API server. Please ensure the server is running."
        exit 1
    }
    
    Test-ApiEndpoint "GET" "/api/v1/test/database" -Description "Database Check"
}

function Setup-Authentication {
    Write-Header "AUTHENTICATION SETUP"
    
    # Register test customer
    $customerData = @{
        username = "test_customer_suite"
        password = "Test123@"
        email = "customer.suite@test.com"
        phoneNumber = "0901234567"
        role = "CUSTOMER"
        fullName = "Test Customer Suite"
    }
    
    Write-TestCase "USER_REGISTRATION" "Registering test customer"
    Test-ApiEndpoint "POST" "/api/v1/auth/register" $customerData -Description "Register Customer" -ExpectedStatus 201
    
    # Register test technician
    $technicianData = @{
        username = "test_technician_suite"
        password = "Test123@"
        email = "technician.suite@test.com"
        phoneNumber = "0901234568"
        role = "TECHNICIAN"
        fullName = "Test Technician Suite"
    }
    
    Write-TestCase "USER_REGISTRATION" "Registering test technician"
    Test-ApiEndpoint "POST" "/api/v1/auth/register" $technicianData -Description "Register Technician" -ExpectedStatus 201
    
    # Login customer
    Write-TestCase "AUTHENTICATION" "Logging in users"
    $customerLogin = @{
        usernameOrEmail = "test_customer_suite"
        password = "Test123@"
    }
    
    $customerResult = Test-ApiEndpoint "POST" "/api/v1/auth/login" $customerLogin -Description "Customer Login"
    if ($customerResult.Success -and $customerResult.Data) {
        $Global:Tokens.Customer = $customerResult.Data.data.accessToken
        Write-Success "Customer token obtained"
    }
    
    # Login technician
    $technicianLogin = @{
        usernameOrEmail = "test_technician_suite"
        password = "Test123@"
    }
    
    $technicianResult = Test-ApiEndpoint "POST" "/api/v1/auth/login" $technicianLogin -Description "Technician Login"
    if ($technicianResult.Success -and $technicianResult.Data) {
        $Global:Tokens.Technician = $technicianResult.Data.data.accessToken
        Write-Success "Technician token obtained"
    }
    
    # Login admin
    $adminLogin = @{
        usernameOrEmail = "admin"
        password = "admin123"
    }
    
    $adminResult = Test-ApiEndpoint "POST" "/api/v1/auth/login" $adminLogin -Description "Admin Login"
    if ($adminResult.Success -and $adminResult.Data) {
        $Global:Tokens.Admin = $adminResult.Data.data.accessToken
        Write-Success "Admin token obtained"
    }
}

function Test-Services {
    Write-Header "SERVICE API TESTS"
    
    $adminHeaders = @{ "Authorization" = "Bearer $($Global:Tokens.Admin)" }
    
    Write-TestCase "SERVICE_PUBLIC" "Testing public service endpoints"
    Test-ApiEndpoint "GET" "/api/v1/services" -Description "Get All Services (Public)"
    Test-ApiEndpoint "GET" "/api/v1/services/active" -Description "Get Active Services"
    Test-ApiEndpoint "GET" "/api/v1/services/health" -Description "Service Health Check"
    Test-ApiEndpoint "GET" "/api/v1/services/paginated?page=0&size=5" -Description "Get Services with Pagination"
    Test-ApiEndpoint "GET" "/api/v1/services/search?keyword=test" -Description "Search Services"
    
    Write-TestCase "SERVICE_ADMIN" "Testing admin service operations"
    $serviceData = @{
        name = "Test Plumbing Service Suite"
        description = "Professional plumbing service for comprehensive testing"
        basePrice = 150.00
    }
    
    $serviceResult = Test-ApiEndpoint "POST" "/api/v1/services" $serviceData $adminHeaders -Description "Create Service" -ExpectedStatus 201
    if ($serviceResult.Success -and $serviceResult.Data) {
        $Global:EntityIds.ServiceId = $serviceResult.Data.data.id
        Write-Success "Service created with ID: $($Global:EntityIds.ServiceId)"
        
        # Test service operations
        Test-ApiEndpoint "GET" "/api/v1/services/$($Global:EntityIds.ServiceId)" -Description "Get Service by ID"
        
        $updateData = @{
            description = "Updated service description"
            basePrice = 175.00
        }
        Test-ApiEndpoint "PUT" "/api/v1/services/$($Global:EntityIds.ServiceId)" $updateData $adminHeaders -Description "Update Service"
    }
    
    Write-TestCase "SERVICE_SECURITY" "Testing service security"
    Test-ApiEndpoint "POST" "/api/v1/services" $serviceData -Description "Unauthorized Service Creation" -ExpectedStatus 401
}

function Test-Customers {
    Write-Header "CUSTOMER API TESTS"
    
    $customerHeaders = @{ "Authorization" = "Bearer $($Global:Tokens.Customer)" }
    $adminHeaders = @{ "Authorization" = "Bearer $($Global:Tokens.Admin)" }
    
    Write-TestCase "CUSTOMER_PROFILE" "Testing customer profile management"
    Test-ApiEndpoint "GET" "/api/v1/customers/profile" -Headers $customerHeaders -Description "Get Customer Profile"
    
    $profileUpdate = @{
        fullName = "Updated Test Customer Suite"
        phoneNumber = "+84987654321"
        gender = "MALE"
        dob = "1990-01-15"
    }
    Test-ApiEndpoint "PUT" "/api/v1/customers/profile" $profileUpdate $customerHeaders -Description "Update Customer Profile"
    
    Write-TestCase "CUSTOMER_ADDRESS" "Testing address management"
    $addressData = @{
        recipientName = "Test Customer Suite"
        recipientPhone = "0901234567"
        addressLine = "123 Test Street, Suite Building"
        ward = "Test Ward"
        district = "Test District"
        city = "Ho Chi Minh City"
        latitude = 10.7769
        longitude = 106.7009
    }
    
    $addressResult = Test-ApiEndpoint "POST" "/api/v1/customers/addresses" $addressData $customerHeaders -Description "Create Customer Address" -ExpectedStatus 201
    if ($addressResult.Success -and $addressResult.Data) {
        $Global:EntityIds.AddressId = $addressResult.Data.data.id
        Write-Success "Address created with ID: $($Global:EntityIds.AddressId)"
    }
    
    Test-ApiEndpoint "GET" "/api/v1/customers/addresses" -Headers $customerHeaders -Description "Get Customer Addresses"
    
    Write-TestCase "CUSTOMER_ADMIN" "Testing admin customer operations"
    Test-ApiEndpoint "GET" "/api/v1/customers" -Headers $adminHeaders -Description "Admin Get All Customers"
}

function Test-Technicians {
    Write-Header "TECHNICIAN API TESTS"
    
    $technicianHeaders = @{ "Authorization" = "Bearer $($Global:Tokens.Technician)" }
    $adminHeaders = @{ "Authorization" = "Bearer $($Global:Tokens.Admin)" }
    
    Write-TestCase "TECHNICIAN_PUBLIC" "Testing public technician endpoints"
    Test-ApiEndpoint "GET" "/api/v1/technicians/active" -Description "Get Active Technicians"
    Test-ApiEndpoint "GET" "/api/v1/technicians/search?keyword=test" -Description "Search Technicians"
    Test-ApiEndpoint "GET" "/api/v1/technicians/skills" -Description "Get All Skills"
    
    Write-TestCase "TECHNICIAN_PROFILE" "Testing technician profile management"
    Test-ApiEndpoint "GET" "/api/v1/technicians/me" -Headers $technicianHeaders -Description "Get My Technician Profile"
    
    $profileUpdate = @{
        fullName = "Updated Test Technician Suite"
        experience = "6+ years in comprehensive home repair services"
    }
    Test-ApiEndpoint "PUT" "/api/v1/technicians/me" $profileUpdate $technicianHeaders -Description "Update My Technician Profile"
    
    Write-TestCase "TECHNICIAN_ADMIN" "Testing admin technician operations"
    Test-ApiEndpoint "GET" "/api/v1/technicians" -Headers $adminHeaders -Description "Admin Get All Technicians"
}

function Test-ServiceRequests {
    Write-Header "SERVICE REQUEST API TESTS"
    
    $customerHeaders = @{ "Authorization" = "Bearer $($Global:Tokens.Customer)" }
    $technicianHeaders = @{ "Authorization" = "Bearer $($Global:Tokens.Technician)" }
    $adminHeaders = @{ "Authorization" = "Bearer $($Global:Tokens.Admin)" }
    
    Write-TestCase "SERVICE_REQUEST_CREATE" "Testing service request creation"
    
    if ($Global:EntityIds.ServiceId -gt 0 -and $Global:EntityIds.AddressId -gt 0) {
        $serviceRequestData = @{
            serviceId = $Global:EntityIds.ServiceId
            addressId = $Global:EntityIds.AddressId
            description = "Comprehensive test service request - urgent plumbing repair needed"
            scheduledTime = "2024-12-31T10:00:00"
        }
        
        $serviceRequestResult = Test-ApiEndpoint "POST" "/api/v1/service-requests" $serviceRequestData $customerHeaders -Description "Create Service Request" -ExpectedStatus 201
        if ($serviceRequestResult.Success -and $serviceRequestResult.Data) {
            $Global:EntityIds.ServiceRequestId = $serviceRequestResult.Data.data.id
            Write-Success "Service request created with ID: $($Global:EntityIds.ServiceRequestId)"
        }
    }
    
    Write-TestCase "SERVICE_REQUEST_WORKFLOW" "Testing service request workflow"
    Test-ApiEndpoint "GET" "/api/v1/service-requests/my" -Headers $customerHeaders -Description "Get My Service Requests"
    Test-ApiEndpoint "GET" "/api/v1/service-requests/available" -Headers $technicianHeaders -Description "Get Available Service Requests"
    
    Write-TestCase "SERVICE_REQUEST_ADMIN" "Testing admin service request operations"
    Test-ApiEndpoint "GET" "/api/v1/service-requests?page=0&size=10" -Headers $adminHeaders -Description "Admin Get All Service Requests"
    Test-ApiEndpoint "GET" "/api/v1/service-requests/stats" -Headers $adminHeaders -Description "Get Service Request Statistics"
}

function Test-Security {
    Write-Header "SECURITY AND VALIDATION TESTS"
    
    Write-TestCase "UNAUTHORIZED_ACCESS" "Testing unauthorized access"
    Test-ApiEndpoint "GET" "/api/v1/customers/profile" -Description "Access without token" -ExpectedStatus 401
    
    Write-TestCase "CROSS_ROLE_ACCESS" "Testing cross-role access"
    $customerHeaders = @{ "Authorization" = "Bearer $($Global:Tokens.Customer)" }
    Test-ApiEndpoint "GET" "/api/v1/admin/dashboard" -Headers $customerHeaders -Description "Customer accessing admin endpoint" -ExpectedStatus 403
    
    Write-TestCase "INVALID_DATA" "Testing input validation"
    $invalidService = @{
        name = ""
        description = ""
        basePrice = -100
    }
    Test-ApiEndpoint "POST" "/api/v1/services" $invalidService $customerHeaders -Description "Invalid service data" -ExpectedStatus 400
    
    Write-TestCase "INVALID_LOGIN" "Testing invalid login"
    $invalidLogin = @{
        usernameOrEmail = "nonexistent"
        password = "wrongpassword"
    }
    Test-ApiEndpoint "POST" "/api/v1/auth/login" $invalidLogin -Description "Invalid login credentials" -ExpectedStatus 401
}

function Show-Help {
    Write-Header "FIX4HOME API TEST SUITE - HELP"
    Write-Host "USAGE: .\complete_api_test_suite.ps1 [options]"
    Write-Host ""
    Write-Host "OPTIONS:"
    Write-Host "  -BaseUrl URL    Override default base URL (default: http://localhost:8080)"
    Write-Host "  -Mode FULL      Run all tests (default)"
    Write-Host "  -Help           Show this help message"
    Write-Host ""
    Write-Host "EXAMPLES:"
    Write-Host "  .\complete_api_test_suite.ps1"
    Write-Host "  .\complete_api_test_suite.ps1 -BaseUrl http://192.168.1.100:8080"
    Write-Host ""
    Write-Host "REQUIREMENTS:"
    Write-Host "  - PowerShell 5.0 or later"
    Write-Host "  - Fix4Home backend server running"
    Write-Host "  - Network connectivity to the API server"
}

function Run-TestSuite {
    Write-Header "FIX4HOME COMPLETE API TEST SUITE" "Green"
    
    $startTime = Get-Date
    
    # Run all test categories
    Test-HealthCheck
    Setup-Authentication
    Test-Services
    Test-Customers
    Test-Technicians
    Test-ServiceRequests
    Test-Security
    
    # Generate final report
    $endTime = Get-Date
    $duration = $endTime - $startTime
    
    Write-Header "TEST EXECUTION SUMMARY" "Green"
    Write-Host ""
    Write-Host "$($Colors.Bold)📊 TEST RESULTS:$($Colors.Reset)"
    Write-Host "   Total Tests: $($Global:TestResults.Total)"
    Write-Host "   $($Colors.Green)✅ Passed: $($Global:TestResults.Passed)$($Colors.Reset)"
    Write-Host "   $($Colors.Red)❌ Failed: $($Global:TestResults.Failed)$($Colors.Reset)"
    Write-Host ""
    Write-Host "$($Colors.Bold)⏱️  Duration: $($duration.TotalSeconds) seconds$($Colors.Reset)"
    
    $successRate = if ($Global:TestResults.Total -gt 0) { 
        [Math]::Round(($Global:TestResults.Passed / $Global:TestResults.Total) * 100, 2) 
    } else { 0 }
    
    Write-Host "$($Colors.Bold)🎯 SUCCESS RATE: $successRate%$($Colors.Reset)"
    
    if ($Global:TestResults.Failed -eq 0) {
        Write-Host ""
        Write-Host "$($Colors.Green)$($Colors.Bold)🎉 ALL TESTS PASSED! FIX4HOME API IS READY! 🚀$($Colors.Reset)"
    } else {
        Write-Host ""
        Write-Host "$($Colors.Yellow)⚠️  Some tests failed. Please review the errors above.$($Colors.Reset)"
    }
    
    Write-Header "TEST EXECUTION COMPLETED" "Green"
}

# ===============================================================
# MAIN EXECUTION
# ===============================================================

if ($Help) {
    Show-Help
    exit 0
}

# Validate base URL format
if (-not $BaseUrl.StartsWith("http://") -and -not $BaseUrl.StartsWith("https://")) {
    Write-Error "Invalid BaseUrl format. Must start with http:// or https://"
    exit 1
}

Write-Host "$($Colors.Cyan)Fix4Home API Test Suite$($Colors.Reset)"
Write-Host "Base URL: $BaseUrl"
Write-Host "Mode: $Mode"
Write-Host ""

Run-TestSuite 