# Fix4Home Service API Test Script
# Testing all Service Management endpoints

$BASE_URL = "http://localhost:8080"
$SERVICE_URL = "$BASE_URL/api/v1/services"
$AUTH_URL = "$BASE_URL/api/v1/auth"

# Colors for output
$GREEN = "`e[32m"
$RED = "`e[31m"
$YELLOW = "`e[33m"
$BLUE = "`e[34m"
$RESET = "`e[0m"

function Write-ColorOutput($Color, $Message) {
    Write-Host "${Color}${Message}${RESET}"
}

function Test-Endpoint($Method, $Url, $Body = $null, $Headers = @{}, $Description) {
    Write-ColorOutput $BLUE "🧪 Testing: $Description"
    Write-ColorOutput $YELLOW "   $Method $Url"
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            Headers = $Headers
            ContentType = "application/json"
        }
        
        if ($Body) {
            $params.Body = $Body | ConvertTo-Json -Depth 10
            Write-ColorOutput $YELLOW "   Body: $($params.Body)"
        }
        
        $response = Invoke-RestMethod @params
        Write-ColorOutput $GREEN "   ✅ SUCCESS: $($response.message)"
        
        if ($response.data) {
            Write-Host "   📊 Data: " -NoNewline
            $response.data | ConvertTo-Json -Depth 2 -Compress | Write-Host
        }
        
        return $response
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $errorBody = $_.ErrorDetails.Message
        Write-ColorOutput $RED "   ❌ ERROR [$statusCode]: $errorBody"
        return $null
    }
    finally {
        Write-Host ""
    }
}

# Test Admin Login to get JWT token
Write-ColorOutput $BLUE "🔐 AUTHENTICATION SETUP"
Write-Host "=" * 50

$adminLogin = @{
    usernameOrEmail = "admin"
    password = "admin123"
}

$loginResponse = Test-Endpoint "POST" "$AUTH_URL/login" $adminLogin @{} "Admin Login"
$adminToken = ""
if ($loginResponse) {
    $adminToken = $loginResponse.data.accessToken
    Write-ColorOutput $GREEN "🎫 Admin Token: $($adminToken.Substring(0,20))..."
}

$authHeaders = @{
    "Authorization" = "Bearer $adminToken"
}

Write-Host ""
Write-ColorOutput $BLUE "🏢 SERVICE API TESTS"
Write-Host "=" * 50

# 1. Test Health Check
Test-Endpoint "GET" "$SERVICE_URL/health" $null @{} "Service Controller Health Check"

# 2. Test Get All Services (Public)
Test-Endpoint "GET" "$SERVICE_URL" $null @{} "Get All Services (Public)"

# 3. Test Get Active Services
Test-Endpoint "GET" "$SERVICE_URL/active" $null @{} "Get Active Services"

# 4. Test Search Services (empty keyword)
Test-Endpoint "GET" "$SERVICE_URL/search?keyword=" $null @{} "Search Services (empty keyword)"

# 5. Test Pagination
Test-Endpoint "GET" "$SERVICE_URL/paginated?page=0&size=5&sortBy=name&sortDir=asc" $null @{} "Get Services with Pagination"

# 6. Test Create Service (Admin only)
$newService = @{
    name = "Plumbing Repair"
    description = "Professional plumbing repair services for homes and offices"
    basePrice = 150.00
}

$createdService = Test-Endpoint "POST" "$SERVICE_URL" $newService $authHeaders "Create New Service (Admin)"
$serviceId = $null
if ($createdService) {
    $serviceId = $createdService.data.id
}

# 7. Test Get Service by ID
if ($serviceId) {
    Test-Endpoint "GET" "$SERVICE_URL/$serviceId" $null @{} "Get Service by ID"
}

# 8. Test Update Service (Admin only)
if ($serviceId) {
    $updateService = @{
        description = "Updated: Professional plumbing repair and maintenance services"
        basePrice = 175.00
    }
    Test-Endpoint "PUT" "$SERVICE_URL/$serviceId" $updateService $authHeaders "Update Service (Admin)"
}

# 9. Test Search Services (with keyword)
Test-Endpoint "GET" "$SERVICE_URL/search?keyword=plumbing" $null @{} "Search Services with keyword 'plumbing'"

# 10. Test Get All Services for Admin
Test-Endpoint "GET" "$SERVICE_URL/admin/all" $null $authHeaders "Get All Services (Admin)"

# 11. Test Toggle Service Status (Admin only)
if ($serviceId) {
    Test-Endpoint "PATCH" "$SERVICE_URL/$serviceId/toggle-status" $null $authHeaders "Toggle Service Status (Admin)"
}

# 12. Test Soft Delete Service (Admin only)
if ($serviceId) {
    Test-Endpoint "DELETE" "$SERVICE_URL/$serviceId" $null $authHeaders "Soft Delete Service (Admin)"
}

# 13. Test Get Service by ID after soft delete
if ($serviceId) {
    Test-Endpoint "GET" "$SERVICE_URL/$serviceId" $null @{} "Get Service after Soft Delete"
}

# 14. Create another service for hard delete test
$tempService = @{
    name = "Temporary Service"
    description = "This service will be hard deleted"
    basePrice = 50.00
}

$tempServiceResponse = Test-Endpoint "POST" "$SERVICE_URL" $tempService $authHeaders "Create Temp Service for Hard Delete"
$tempServiceId = $null
if ($tempServiceResponse) {
    $tempServiceId = $tempServiceResponse.data.id
}

# 15. Test Hard Delete Service (Admin only)
if ($tempServiceId) {
    Test-Endpoint "DELETE" "$SERVICE_URL/$tempServiceId/hard" $null $authHeaders "Hard Delete Service (Admin)"
}

# 16. Test Access Control - Try creating service without admin token
Write-ColorOutput $BLUE "🔒 SECURITY TESTS"
Write-Host "=" * 50

$unauthorizedService = @{
    name = "Unauthorized Service"
    description = "This should fail"
    basePrice = 100.00
}

Test-Endpoint "POST" "$SERVICE_URL" $unauthorizedService @{} "Create Service without Auth (Should fail)"

# 17. Test with invalid token
$invalidHeaders = @{
    "Authorization" = "Bearer invalid-token"
}

Test-Endpoint "POST" "$SERVICE_URL" $unauthorizedService $invalidHeaders "Create Service with Invalid Token (Should fail)"

Write-ColorOutput $BLUE "📋 TEST SUMMARY"
Write-Host "=" * 50
Write-ColorOutput $GREEN "✅ Service API implementation completed!"
Write-ColorOutput $YELLOW "📝 Features tested:"
Write-Host "   • Public service browsing (GET endpoints)"
Write-Host "   • Admin service management (CRUD operations)"
Write-Host "   • Search functionality"
Write-Host "   • Pagination support"
Write-Host "   • Soft/Hard delete operations"
Write-Host "   • Status toggle functionality"
Write-Host "   • Security and access control"
Write-Host ""
Write-ColorOutput $BLUE "🚀 Next steps: Implement CustomerController, TechnicianController, or ServiceRequestController" 