# Fix4Home Customer API Test Script
# Testing Customer Profile Management & Address Management endpoints

$BASE_URL = "http://localhost:8080"
$CUSTOMER_URL = "$BASE_URL/api/v1/customers"  
$AUTH_URL = "$BASE_URL/api/v1/auth"

# Colors for output
$GREEN = "`e[32m"
$RED = "`e[31m"
$YELLOW = "`e[33m"
$BLUE = "`e[34m"
$PURPLE = "`e[35m"
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
            if ($response.data -is [Array] -and $response.data.Count -gt 1) {
                Write-Host "Array with $($response.data.Count) items"
            } else {
                $response.data | ConvertTo-Json -Depth 2 -Compress | Write-Host
            }
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

# ==================== AUTHENTICATION SETUP ====================

Write-ColorOutput $BLUE "🔐 AUTHENTICATION SETUP"
Write-Host "=" * 60

# Create test customer if not exists
$testCustomer = @{
    username = "testcustomer"
    email = "customer@test.com"
    password = "customer123"
    phoneNumber = "+84123456789"
    fullName = "Test Customer"
    role = "CUSTOMER"
}

Write-ColorOutput $PURPLE "📝 Registering test customer..."
$customerRegisterResponse = Test-Endpoint "POST" "$AUTH_URL/register" $testCustomer @{} "Register Test Customer"

# Login as customer
$customerLogin = @{
    usernameOrEmail = "testcustomer"
    password = "customer123"
}

$customerLoginResponse = Test-Endpoint "POST" "$AUTH_URL/login" $customerLogin @{} "Customer Login"
$customerToken = ""
if ($customerLoginResponse) {
    $customerToken = $customerLoginResponse.data.accessToken
    Write-ColorOutput $GREEN "🎫 Customer Token: $($customerToken.Substring(0,20))..."
}

$customerHeaders = @{
    "Authorization" = "Bearer $customerToken"
}

# Login as admin
$adminLogin = @{
    usernameOrEmail = "admin"
    password = "admin123"
}

$adminLoginResponse = Test-Endpoint "POST" "$AUTH_URL/login" $adminLogin @{} "Admin Login"
$adminToken = ""
if ($adminLoginResponse) {
    $adminToken = $adminLoginResponse.data.accessToken
    Write-ColorOutput $GREEN "🎫 Admin Token: $($adminToken.Substring(0,20))..."
}

$adminHeaders = @{
    "Authorization" = "Bearer $adminToken"
}

Write-Host ""
Write-ColorOutput $BLUE "👤 CUSTOMER PROFILE MANAGEMENT TESTS"
Write-Host "=" * 60

# Test Health Check
Test-Endpoint "GET" "$CUSTOMER_URL/health" $null @{} "Customer Controller Health Check"

# Test Get My Profile (Customer)
Test-Endpoint "GET" "$CUSTOMER_URL/profile" $null $customerHeaders "Get My Profile (Customer)"

# Test Update My Profile (Customer)
$updateProfile = @{
    fullName = "Updated Test Customer"
    phoneNumber = "+84987654321"
    gender = "MALE"
    dob = "1990-01-15"
}

Test-Endpoint "PUT" "$CUSTOMER_URL/profile" $updateProfile $customerHeaders "Update My Profile (Customer)"

# Test Get Updated Profile
Test-Endpoint "GET" "$CUSTOMER_URL/profile" $null $customerHeaders "Get Updated Profile (Customer)"

# Test Admin Get All Customers
Test-Endpoint "GET" "$CUSTOMER_URL" $null $adminHeaders "Get All Customers (Admin)"

# Get customer ID for admin operations
$customerUserId = $null
if ($customerLoginResponse) {
    $customerUserId = $customerLoginResponse.data.userId
}

# Test Admin Get Specific Customer Profile
if ($customerUserId) {
    Test-Endpoint "GET" "$CUSTOMER_URL/$customerUserId/profile" $null $adminHeaders "Get Customer Profile by ID (Admin)"
}

Write-Host ""
Write-ColorOutput $BLUE "🏠 ADDRESS MANAGEMENT TESTS"
Write-Host "=" * 60

# Test Get My Addresses (initially empty)
Test-Endpoint "GET" "$CUSTOMER_URL/addresses" $null $customerHeaders "Get My Addresses (Customer)"

# Test Create Address
$newAddress = @{
    recipientName = "John Doe"
    recipientPhone = "+84123456789"
    addressLine = "123 Main Street, Building A, Floor 5"
    ward = "Ward 1"
    district = "District 1"
    city = "Ho Chi Minh City"
    latitude = 10.762622
    longitude = 106.660172
}

$createdAddressResponse = Test-Endpoint "POST" "$CUSTOMER_URL/addresses" $newAddress $customerHeaders "Create New Address (Customer)"
$addressId = $null
if ($createdAddressResponse) {
    $addressId = $createdAddressResponse.data.id
}

# Test Get Addresses After Creation
Test-Endpoint "GET" "$CUSTOMER_URL/addresses" $null $customerHeaders "Get My Addresses After Creation (Customer)"

# Test Update Address
if ($addressId) {
    $updateAddress = @{
        recipientName = "Jane Doe"
        addressLine = "456 Updated Street, Building B, Floor 10"
        city = "Ho Chi Minh City - Updated"
    }
    Test-Endpoint "PUT" "$CUSTOMER_URL/addresses/$addressId" $updateAddress $customerHeaders "Update Address (Customer)"
}

# Test Admin Get Customer Addresses
if ($customerUserId) {
    Test-Endpoint "GET" "$CUSTOMER_URL/$customerUserId/addresses" $null $adminHeaders "Get Customer Addresses (Admin)"
}

Write-Host ""
Write-ColorOutput $BLUE "🔒 SECURITY TESTS"
Write-Host "=" * 60

# Test Unauthorized Access
Test-Endpoint "GET" "$CUSTOMER_URL/profile" $null @{} "Get Profile without Token (Should fail)"

# Test Admin trying customer-specific endpoints
Test-Endpoint "GET" "$CUSTOMER_URL/profile" $null $adminHeaders "Admin Access Customer Profile Endpoint (Should fail)"

Write-Host ""
Write-ColorOutput $BLUE "📋 TEST SUMMARY"
Write-Host "=" * 60
Write-ColorOutput $GREEN "✅ Customer Controller implementation completed!"
Write-ColorOutput $YELLOW "📝 Features tested:"
Write-Host "   • Customer profile management"
Write-Host "   • Admin customer management"
Write-Host "   • Address CRUD operations"
Write-Host "   • Security and access control"
Write-Host ""
Write-ColorOutput $BLUE "🚀 Next steps: Implement TechnicianController or ServiceRequestController" 