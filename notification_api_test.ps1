# ==============================================
# NOTIFICATION API TESTING SCRIPT
# ==============================================
# Tests all notification endpoints for Fix4Home platform
# Covers user notifications, admin operations, and statistics

# Configuration
$baseUrl = "http://localhost:8080"
$contentType = "application/json"

# Colors for output
$GREEN = "Green"
$RED = "Red"
$YELLOW = "Yellow"
$CYAN = "Cyan"

# Test data storage
$Global:adminToken = ""
$Global:customerToken = ""
$Global:technicianToken = ""
$Global:testNotificationId = ""

function Write-TestHeader($title) {
    Write-Host "`n================================================" -ForegroundColor $CYAN
    Write-Host " $title" -ForegroundColor $CYAN
    Write-Host "================================================" -ForegroundColor $CYAN
}

function Write-Success($message) {
    Write-Host "✅ $message" -ForegroundColor $GREEN
}

function Write-Error($message) {
    Write-Host "❌ $message" -ForegroundColor $RED
}

function Write-Info($message) {
    Write-Host "ℹ️  $message" -ForegroundColor $YELLOW
}

function Test-ApiCall($method, $url, $body = $null, $token = $null, $expectedStatus = 200) {
    try {
        $headers = @{ "Content-Type" = $contentType }
        if ($token) {
            $headers["Authorization"] = "Bearer $token"
        }

        $params = @{
            Uri = "$baseUrl$url"
            Method = $method
            Headers = $headers
        }

        if ($body -and ($method -eq "POST" -or $method -eq "PUT")) {
            $params["Body"] = $body
        }

        $response = Invoke-RestMethod @params
        
        if ($response.success) {
            Write-Success "$method $url - Status: Success"
            return $response
        } else {
            Write-Error "$method $url - API Error: $($response.message)"
            return $null
        }
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq $expectedStatus) {
            Write-Success "$method $url - Expected Status: $statusCode"
            return @{ success = $true; expectedError = $true }
        } else {
            Write-Error "$method $url - HTTP Error: $statusCode - $($_.Exception.Message)"
            return $null
        }
    }
}

function Setup-TestData {
    Write-TestHeader "SETTING UP TEST DATA"
    
    # Create admin user
    Write-Info "Creating admin user..."
    $adminData = @{
        username = "admin_notification"
        email = "admin.notification@fix4home.com"
        password = "Admin123!"
        role = "ADMIN"
    } | ConvertTo-Json

    $adminRegister = Test-ApiCall "POST" "/api/v1/auth/register" $adminData
    if (-not $adminRegister) {
        Write-Info "Admin user might already exist, trying login..."
    }

    # Login admin
    $adminLogin = @{
        usernameOrEmail = "admin_notification"
        password = "Admin123!"
    } | ConvertTo-Json

    $adminLoginResponse = Test-ApiCall "POST" "/api/v1/auth/login" $adminLogin
    if ($adminLoginResponse) {
        $Global:adminToken = $adminLoginResponse.data.token
        Write-Success "Admin logged in successfully"
    } else {
        Write-Error "Failed to login admin"
        return $false
    }

    # Create customer user
    Write-Info "Creating customer user..."
    $customerData = @{
        username = "customer_notification"
        email = "customer.notification@fix4home.com"
        password = "Customer123!"
        role = "CUSTOMER"
    } | ConvertTo-Json

    $customerRegister = Test-ApiCall "POST" "/api/v1/auth/register" $customerData
    if (-not $customerRegister) {
        Write-Info "Customer user might already exist, trying login..."
    }

    # Login customer
    $customerLogin = @{
        usernameOrEmail = "customer_notification"
        password = "Customer123!"
    } | ConvertTo-Json

    $customerLoginResponse = Test-ApiCall "POST" "/api/v1/auth/login" $customerLogin
    if ($customerLoginResponse) {
        $Global:customerToken = $customerLoginResponse.data.token
        Write-Success "Customer logged in successfully"
    } else {
        Write-Error "Failed to login customer"
        return $false
    }

    return $true
}

function Test-AdminNotificationOperations {
    Write-TestHeader "ADMIN NOTIFICATION OPERATIONS"
    
    if (-not $Global:adminToken) {
        Write-Error "Admin token not available"
        return
    }

    # 1. Create single notification
    Write-Info "Testing: Create notification for customer..."
    
    # Get customer user ID first
    $customerUserResponse = Test-ApiCall "GET" "/api/v1/customers/me" $null $Global:customerToken
    if (-not $customerUserResponse) {
        Write-Error "Failed to get customer user info"
        return
    }
    $customerUserId = $customerUserResponse.data.id

    $createNotificationData = @{
        userId = $customerUserId
        title = "Welcome to Fix4Home!"
        message = "Welcome to our platform! We are excited to help you with all your home repair needs."
        category = "WELCOME"
        priority = "MEDIUM"
    } | ConvertTo-Json

    $createResponse = Test-ApiCall "POST" "/api/notifications/create" $createNotificationData $Global:adminToken 201
    if ($createResponse -and $createResponse.data) {
        $Global:testNotificationId = $createResponse.data.id
        Write-Success "Notification created with ID: $($Global:testNotificationId)"
    }

    # 2. Create bulk notification for all customers
    Write-Info "Testing: Create bulk notification for CUSTOMER role..."
    $bulkNotificationData = @{
        targetRole = "CUSTOMER"
        title = "System Maintenance Notice"
        message = "We will be performing system maintenance on Sunday from 2 AM to 4 AM. Sorry for any inconvenience."
        category = "SYSTEM"
        priority = "HIGH"
    } | ConvertTo-Json

    $bulkResponse = Test-ApiCall "POST" "/api/notifications/bulk" $bulkNotificationData $Global:adminToken 201
    if ($bulkResponse) {
        Write-Success "Bulk notifications created: $($bulkResponse.data.Count) notifications"
    }

    # 3. Get all notifications (admin view)
    Write-Info "Testing: Get all notifications (admin)..."
    $allNotificationsResponse = Test-ApiCall "GET" "/api/notifications/admin/all?page=0&size=10" $null $Global:adminToken
    if ($allNotificationsResponse) {
        Write-Success "Retrieved all notifications: $($allNotificationsResponse.data.totalElements) total"
    }

    # 4. Get system notification statistics
    Write-Info "Testing: Get system notification statistics..."
    $systemStatsResponse = Test-ApiCall "GET" "/api/notifications/admin/stats" $null $Global:adminToken
    if ($systemStatsResponse) {
        Write-Success "System stats - Total: $($systemStatsResponse.data.totalNotifications), Unread: $($systemStatsResponse.data.unreadNotifications)"
    }

    # 5. Cleanup old notifications (test with 0 days to cleanup nothing)
    Write-Info "Testing: Cleanup old notifications..."
    $cleanupResponse = Test-ApiCall "DELETE" "/api/notifications/admin/cleanup/365" $null $Global:adminToken
    if ($cleanupResponse) {
        Write-Success "Cleanup completed: $($cleanupResponse.data) notifications removed"
    }
}

function Test-UserNotificationOperations {
    Write-TestHeader "USER NOTIFICATION OPERATIONS"
    
    if (-not $Global:customerToken) {
        Write-Error "Customer token not available"
        return
    }

    # 1. Get my notifications
    Write-Info "Testing: Get my notifications..."
    $myNotificationsResponse = Test-ApiCall "GET" "/api/notifications/my?page=0&size=10" $null $Global:customerToken
    if ($myNotificationsResponse) {
        Write-Success "Retrieved notifications: $($myNotificationsResponse.data.totalElements) total, $($myNotificationsResponse.data.numberOfElements) in this page"
    }

    # 2. Get unread count
    Write-Info "Testing: Get unread notification count..."
    $unreadCountResponse = Test-ApiCall "GET" "/api/notifications/unread-count" $null $Global:customerToken
    if ($unreadCountResponse) {
        Write-Success "Unread notifications count: $($unreadCountResponse.data)"
    }

    # 3. Get notification by ID
    if ($Global:testNotificationId) {
        Write-Info "Testing: Get notification by ID..."
        $notificationByIdResponse = Test-ApiCall "GET" "/api/notifications/$($Global:testNotificationId)" $null $Global:customerToken
        if ($notificationByIdResponse) {
            Write-Success "Retrieved notification: $($notificationByIdResponse.data.title)"
        }
    }

    # 4. Search notifications
    Write-Info "Testing: Search notifications..."
    $searchResponse = Test-ApiCall "GET" "/api/notifications/search?keyword=welcome&page=0&size=5" $null $Global:customerToken
    if ($searchResponse) {
        Write-Success "Search completed: $($searchResponse.data.totalElements) notifications found"
    }

    # 5. Get recent notifications
    Write-Info "Testing: Get recent notifications (last 7 days)..."
    $recentResponse = Test-ApiCall "GET" "/api/notifications/recent?days=7" $null $Global:customerToken
    if ($recentResponse) {
        Write-Success "Recent notifications: $($recentResponse.data.Count) notifications"
    }

    # 6. Get notification statistics
    Write-Info "Testing: Get user notification statistics..."
    $userStatsResponse = Test-ApiCall "GET" "/api/notifications/stats" $null $Global:customerToken
    if ($userStatsResponse) {
        Write-Success "User stats - Total: $($userStatsResponse.data.totalNotifications), Unread: $($userStatsResponse.data.unreadNotifications)"
    }
}

function Test-NotificationActions {
    Write-TestHeader "NOTIFICATION ACTIONS"
    
    if (-not $Global:customerToken -or -not $Global:testNotificationId) {
        Write-Error "Customer token or test notification ID not available"
        return
    }

    # 1. Mark specific notification as read
    Write-Info "Testing: Mark notification as read..."
    $markReadResponse = Test-ApiCall "PUT" "/api/notifications/$($Global:testNotificationId)/read" $null $Global:customerToken
    if ($markReadResponse) {
        Write-Success "Notification marked as read: $($markReadResponse.data.isRead)"
    }

    # 2. Mark all notifications as read
    Write-Info "Testing: Mark all notifications as read..."
    $markAllReadResponse = Test-ApiCall "PUT" "/api/notifications/mark-all-read" $null $Global:customerToken
    if ($markAllReadResponse) {
        Write-Success "All notifications marked as read: $($markAllReadResponse.data) notifications updated"
    }

    # 3. Mark multiple notifications using mark endpoint
    Write-Info "Testing: Mark notifications using bulk endpoint..."
    $markData = @{
        markAll = $true
        isRead = $false
        reason = "Testing bulk mark operation"
    } | ConvertTo-Json

    $markResponse = Test-ApiCall "PUT" "/api/notifications/mark" $markData $Global:customerToken
    if ($markResponse) {
        Write-Success "Bulk mark completed: $($markResponse.data) notifications marked as unread"
    }

    # 4. Delete read notifications
    Write-Info "Testing: Delete read notifications..."
    $deleteReadResponse = Test-ApiCall "DELETE" "/api/notifications/read" $null $Global:customerToken
    if ($deleteReadResponse) {
        Write-Success "Read notifications deleted: $($deleteReadResponse.data) notifications removed"
    }

    # 5. Delete specific notification
    Write-Info "Testing: Delete specific notification..."
    $deleteResponse = Test-ApiCall "DELETE" "/api/notifications/$($Global:testNotificationId)" $null $Global:customerToken
    if ($deleteResponse) {
        Write-Success "Notification deleted successfully"
    }
}

function Test-EdgeCases {
    Write-TestHeader "EDGE CASES & ERROR HANDLING"
    
    # 1. Access notification without authentication
    Write-Info "Testing: Access without authentication (should fail)..."
    Test-ApiCall "GET" "/api/notifications/my" $null $null 401

    # 2. Access non-existent notification
    Write-Info "Testing: Access non-existent notification (should fail)..."
    Test-ApiCall "GET" "/api/notifications/99999" $null $Global:customerToken 400

    # 3. Admin operations with customer token
    Write-Info "Testing: Admin operations with customer token (should fail)..."
    $invalidAdminData = @{
        userId = 1
        title = "Invalid admin operation"
        message = "This should fail"
    } | ConvertTo-Json
    Test-ApiCall "POST" "/api/notifications/create" $invalidAdminData $Global:customerToken 403

    # 4. Invalid create notification request
    Write-Info "Testing: Invalid create notification request (should fail)..."
    $invalidData = @{
        title = ""  # Empty title should fail validation
        message = "Test message"
    } | ConvertTo-Json
    Test-ApiCall "POST" "/api/notifications/create" $invalidData $Global:adminToken 400

    # 5. Search with empty keyword
    Write-Info "Testing: Search with empty keyword..."
    Test-ApiCall "GET" "/api/notifications/search?keyword=" $null $Global:customerToken

    Write-Success "Edge case testing completed"
}

function Run-AllTests {
    Write-TestHeader "NOTIFICATION API COMPREHENSIVE TESTING"
    
    Write-Info "Starting notification API testing..."
    Write-Info "Target URL: $baseUrl"
    
    # Setup test data
    if (-not (Setup-TestData)) {
        Write-Error "Failed to setup test data. Exiting."
        return
    }

    # Run test suites
    Test-AdminNotificationOperations
    Test-UserNotificationOperations
    Test-NotificationActions
    Test-EdgeCases
    
    Write-TestHeader "TESTING COMPLETED"
    Write-Success "All notification API tests completed!"
    Write-Info "Check the output above for detailed results."
}

# Run all tests
Run-AllTests 