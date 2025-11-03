# FIX4HOME COMPLETE API DOCUMENTATION

## Table of Contents
1. [API Overview](#api-overview)
2. [Authentication & Authorization](#authentication--authorization)
3. [Standard Response Format](#standard-response-format)
4. [HTTP Status Codes](#http-status-codes)
5. [API Endpoints](#api-endpoints)
   - [Authentication](#authentication)
   - [Customer Management](#customer-management)
   - [Technician Management](#technician-management)
   - [Service Management](#service-management)
   - [Service Request Management](#service-request-management)
   - [Payment Management](#payment-management)
   - [Feedback Management](#feedback-management)
   - [Notification Management](#notification-management)
   - [Admin Management](#admin-management)
   - [System Testing](#system-testing)

---

## API Overview

**Base URL:** `http://localhost:8080/api/v1`
**Version:** v1
**Content-Type:** `application/json`
**Authentication:** JWT Bearer Token

All API endpoints follow RESTful conventions with consistent `/api/v1/` versioning.

---

## Authentication & Authorization

### Authentication Types
- **Public:** No authentication required
- **Authenticated:** Valid JWT token required
- **Role-based:** Specific role(s) required

### Roles
- `CUSTOMER` - End users who request services
- `TECHNICIAN` - Service providers
- `ADMIN` - System administrators

### Authorization Patterns
- `HAS_CUSTOMER_ROLE` - Customer only
- `HAS_TECHNICIAN_ROLE` - Technician only  
- `HAS_ADMIN_ROLE` - Admin only
- `HAS_ANY_ROLE` - Any authenticated user
- `HAS_CUSTOMER_OR_ADMIN_ROLE` - Customer or Admin
- `HAS_TECHNICIAN_OR_ADMIN_ROLE` - Technician or Admin

---

## Standard Response Format

All API responses follow this consistent format:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | OK - Successful operation |
| 201 | Created - Resource created successfully |
| 204 | No Content - Successful operation with no content |
| 400 | Bad Request - Invalid request data |
| 401 | Unauthorized - Authentication required |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource not found |
| 409 | Conflict - Resource already exists |
| 500 | Internal Server Error - Server error |

---

# API Endpoints

## Authentication

### POST /api/v1/auth/register
**Description:** Register new user account  
**Authorization:** Public  
**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "fullName": "John Doe",
  "phoneNumber": "+1234567890",
  "role": "CUSTOMER"
}
```
**Response:** `201 Created`
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "role": "CUSTOMER"
    }
  }
}
```

### POST /api/v1/auth/login
**Description:** User login  
**Authorization:** Public  
**Request Body:**
```json
{
  "usernameOrEmail": "john@example.com",
  "password": "SecurePass123"
}
```
**Response:** `200 OK`

### POST /api/v1/auth/logout
**Description:** User logout  
**Authorization:** Authenticated  
**Response:** `200 OK`

---

## Customer Management

### GET /api/v1/customers/profile
**Description:** Get current customer's profile  
**Authorization:** HAS_CUSTOMER_ROLE  
**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Profile retrieved successfully",
  "data": {
    "userId": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "phoneNumber": "+1234567890",
    "gender": "MALE",
    "dateOfBirth": "1990-01-15",
    "createdAt": "2024-01-01T00:00:00Z"
  }
}
```

### PUT /api/v1/customers/profile
**Description:** Update current customer's profile  
**Authorization:** HAS_CUSTOMER_ROLE  
**Request Body:**
```json
{
  "fullName": "John Doe Updated",
  "phoneNumber": "+0987654321",
  "gender": "MALE",
  "dateOfBirth": "1990-01-15"
}
```

### GET /api/v1/customers/{userId}/profile
**Description:** Get customer profile by ID (Admin only)  
**Authorization:** HAS_ADMIN_ROLE  
**Parameters:** `userId` (path)

### GET /api/v1/customers
**Description:** Get all customers (Admin only)  
**Authorization:** HAS_ADMIN_ROLE

### GET /api/v1/customers/paginated
**Description:** Get customers with pagination (Admin only)  
**Authorization:** HAS_ADMIN_ROLE  
**Query Parameters:**
- `page` (default: 0)
- `size` (default: 10)
- `sortBy` (default: "username")
- `sortDir` (default: "asc")

### Customer Addresses

### GET /api/v1/customers/addresses
**Description:** Get current customer's addresses  
**Authorization:** HAS_CUSTOMER_ROLE

### POST /api/v1/customers/addresses
**Description:** Create new address  
**Authorization:** HAS_CUSTOMER_ROLE  
**Request Body:**
```json
{
  "addressLine": "123 Main Street",
  "ward": "Ward 1",
  "district": "District 1", 
  "city": "Ho Chi Minh City",
  "isDefault": true
}
```

### GET /api/v1/customers/addresses/{addressId}
**Description:** Get address by ID  
**Authorization:** HAS_CUSTOMER_ROLE

### PUT /api/v1/customers/addresses/{addressId}
**Description:** Update address  
**Authorization:** HAS_CUSTOMER_ROLE

### DELETE /api/v1/customers/addresses/{addressId}
**Description:** Delete address  
**Authorization:** HAS_CUSTOMER_ROLE

### GET /api/v1/customers/{userId}/addresses
**Description:** Get customer addresses (Admin only)  
**Authorization:** HAS_ADMIN_ROLE

---

## Technician Management

### GET /api/v1/technicians/active
**Description:** Get all active technicians  
**Authorization:** Public

### GET /api/v1/technicians/search
**Description:** Search technicians  
**Authorization:** Public  
**Query Parameters:**
- `keyword` - Search keyword
- `skillIds` - Comma-separated skill IDs

### GET /api/v1/technicians/by-rating
**Description:** Get technicians sorted by rating  
**Authorization:** Public

### GET /api/v1/technicians/{userId}
**Description:** Get technician profile by user ID  
**Authorization:** Public

### GET /api/v1/technicians/{userId}/skills
**Description:** Get technician skills  
**Authorization:** Public

### GET /api/v1/technicians/me
**Description:** Get current technician's profile  
**Authorization:** HAS_TECHNICIAN_ROLE

### PUT /api/v1/technicians/me
**Description:** Update current technician's profile  
**Authorization:** HAS_TECHNICIAN_ROLE

### GET /api/v1/technicians/me/skills
**Description:** Get current technician's skills  
**Authorization:** HAS_TECHNICIAN_ROLE

### PUT /api/v1/technicians/me/skills
**Description:** Update current technician's skills  
**Authorization:** HAS_TECHNICIAN_ROLE  
**Request Body:**
```json
{
  "skillIds": [1, 2, 3]
}
```

### Admin Technician Management

### GET /api/v1/technicians/paginated
**Description:** Get technicians with pagination (Admin)  
**Authorization:** HAS_ADMIN_ROLE

### GET /api/v1/technicians/pending
**Description:** Get pending technician approvals  
**Authorization:** HAS_ADMIN_ROLE

### PUT /api/v1/technicians/{userId}
**Description:** Update technician profile (Admin)  
**Authorization:** HAS_ADMIN_ROLE

### PUT /api/v1/technicians/{userId}/approve
**Description:** Approve technician registration  
**Authorization:** HAS_ADMIN_ROLE

### PUT /api/v1/technicians/{userId}/reject
**Description:** Reject technician registration  
**Authorization:** HAS_ADMIN_ROLE

### Skills Management

### GET /api/v1/technicians/skills
**Description:** Get all skills  
**Authorization:** Public

### GET /api/v1/technicians/skills/search
**Description:** Search skills  
**Authorization:** Public

### POST /api/v1/technicians/skills
**Description:** Create new skill (Admin)  
**Authorization:** HAS_ADMIN_ROLE

### DELETE /api/v1/technicians/skills/{skillId}
**Description:** Delete skill (Admin)  
**Authorization:** HAS_ADMIN_ROLE

---

## Service Management

### GET /api/v1/services
**Description:** Get all services  
**Authorization:** Public

### POST /api/v1/services
**Description:** Create new service (Admin)  
**Authorization:** HAS_ADMIN_ROLE  
**Request Body:**
```json
{
  "name": "Plumbing Repair",
  "description": "Professional plumbing repair services",
  "basePrice": 50.00,
  "estimatedDuration": 120,
  "isActive": true
}
```

### GET /api/v1/services/admin/all
**Description:** Get all services (Admin view)  
**Authorization:** HAS_ADMIN_ROLE

### GET /api/v1/services/paginated
**Description:** Get services with pagination  
**Authorization:** Public

### GET /api/v1/services/{id}
**Description:** Get service by ID  
**Authorization:** Public

### GET /api/v1/services/search
**Description:** Search services  
**Authorization:** Public

### PUT /api/v1/services/{id}
**Description:** Update service (Admin)  
**Authorization:** HAS_ADMIN_ROLE

### DELETE /api/v1/services/{id}
**Description:** Soft delete service (Admin)  
**Authorization:** HAS_ADMIN_ROLE

### DELETE /api/v1/services/{id}/hard
**Description:** Hard delete service (Admin)  
**Authorization:** HAS_ADMIN_ROLE

### PATCH /api/v1/services/{id}/toggle-status
**Description:** Toggle service active status (Admin)  
**Authorization:** HAS_ADMIN_ROLE

### GET /api/v1/services/active
**Description:** Get only active services  
**Authorization:** Public

### GET /api/v1/services/health
**Description:** Service management health check  
**Authorization:** Public

---

## Service Request Management

### POST /api/v1/service-requests
**Description:** Create new service request  
**Authorization:** HAS_CUSTOMER_ROLE  
**Request Body:**
```json
{
  "serviceId": 1,
  "addressId": 1,
  "description": "Kitchen sink is leaking",
  "preferredDate": "2024-01-20",
  "preferredTimeSlot": "MORNING"
}
```

### GET /api/v1/service-requests/my
**Description:** Get current customer's service requests  
**Authorization:** HAS_CUSTOMER_ROLE

### GET /api/v1/service-requests/{id}
**Description:** Get service request by ID  
**Authorization:** HAS_ANY_ROLE

### PUT /api/v1/service-requests/{id}/cancel
**Description:** Cancel service request  
**Authorization:** HAS_CUSTOMER_OR_ADMIN_ROLE

### Technician Service Request Operations

### GET /api/v1/service-requests/available
**Description:** Get available service requests for technicians  
**Authorization:** HAS_TECHNICIAN_ROLE

### GET /api/v1/service-requests/my-assignments
**Description:** Get technician's assigned requests  
**Authorization:** HAS_TECHNICIAN_ROLE

### PUT /api/v1/service-requests/{id}/accept
**Description:** Accept service request assignment  
**Authorization:** HAS_TECHNICIAN_ROLE

### PUT /api/v1/service-requests/{id}/decline
**Description:** Decline service request assignment  
**Authorization:** HAS_TECHNICIAN_ROLE

### PUT /api/v1/service-requests/{id}/start
**Description:** Start working on service request  
**Authorization:** HAS_TECHNICIAN_ROLE

### PUT /api/v1/service-requests/{id}/complete
**Description:** Mark service request as completed  
**Authorization:** HAS_TECHNICIAN_ROLE  
**Request Body:**
```json
{
  "completionNotes": "Sink repaired successfully",
  "actualCost": 75.00
}
```

### Admin Service Request Operations

### GET /api/v1/service-requests/status/{status}
**Description:** Get requests by status (Admin)  
**Authorization:** HAS_ADMIN_ROLE

### PUT /api/v1/service-requests/{id}/assign
**Description:** Assign technician to request (Admin)  
**Authorization:** HAS_ADMIN_ROLE

### PUT /api/v1/service-requests/{id}/status
**Description:** Update request status (Admin)  
**Authorization:** HAS_ADMIN_ROLE

### GET /api/v1/service-requests/stats
**Description:** Get service request statistics (Admin)  
**Authorization:** HAS_ADMIN_ROLE

---

## Payment Management

### GET /api/v1/payments/methods
**Description:** Get available payment methods  
**Authorization:** Public

### POST /api/v1/payments/create
**Description:** Create payment for service request  
**Authorization:** HAS_CUSTOMER_ROLE  
**Request Body:**
```json
{
  "serviceRequestId": 1,
  "paymentMethod": "CREDIT_CARD",
  "amount": 75.00
}
```

### Customer Payment Operations

### GET /api/v1/payments/my
**Description:** Get current customer's payments  
**Authorization:** HAS_CUSTOMER_ROLE

### GET /api/v1/payments/my/pending
**Description:** Get current customer's pending payments  
**Authorization:** HAS_CUSTOMER_ROLE

### GET /api/v1/payments/my/stats
**Description:** Get current customer's payment statistics  
**Authorization:** HAS_CUSTOMER_ROLE

### Technician Payment Operations

### GET /api/v1/payments/technician/my
**Description:** Get current technician's earnings  
**Authorization:** HAS_TECHNICIAN_ROLE

### GET /api/v1/payments/technician/my/stats
**Description:** Get current technician's earning statistics  
**Authorization:** HAS_TECHNICIAN_ROLE

### Admin Payment Operations

### GET /api/v1/payments/admin/all
**Description:** Get all payments (Admin)  
**Authorization:** HAS_ADMIN_ROLE

### GET /api/v1/payments/admin/failed
**Description:** Get failed payments (Admin)  
**Authorization:** HAS_ADMIN_ROLE

### GET /api/v1/payments/admin/stats
**Description:** Get payment statistics (Admin)  
**Authorization:** HAS_ADMIN_ROLE

---

## Feedback Management

### GET /api/v1/feedbacks/public
**Description:** Get public feedback for technicians  
**Authorization:** Public

### POST /api/v1/feedbacks/create
**Description:** Create feedback for completed service  
**Authorization:** HAS_CUSTOMER_ROLE  
**Request Body:**
```json
{
  "serviceRequestId": 1,
  "rating": 5,
  "comment": "Excellent service, very professional"
}
```

### Customer Feedback Operations

### GET /api/v1/feedbacks/my
**Description:** Get current customer's feedback  
**Authorization:** HAS_CUSTOMER_ROLE

### GET /api/v1/feedbacks/my/{feedbackId}
**Description:** Get specific feedback by ID  
**Authorization:** HAS_CUSTOMER_ROLE

### Technician Feedback Operations

### GET /api/v1/feedbacks/technician/my
**Description:** Get feedback for current technician  
**Authorization:** HAS_TECHNICIAN_ROLE

### GET /api/v1/feedbacks/technician/my/unreplied
**Description:** Get unreplied feedback for current technician  
**Authorization:** HAS_TECHNICIAN_ROLE

### PUT /api/v1/feedbacks/{feedbackId}/reply
**Description:** Reply to feedback  
**Authorization:** HAS_TECHNICIAN_ROLE

### GET /api/v1/feedbacks/technician/my/stats
**Description:** Get current technician's feedback statistics  
**Authorization:** HAS_TECHNICIAN_ROLE

### Admin Feedback Operations

### GET /api/v1/feedbacks/admin/all
**Description:** Get all feedback (Admin)  
**Authorization:** HAS_ADMIN_ROLE

### GET /api/v1/feedbacks/admin/rating/{rating}
**Description:** Get feedback by rating (Admin)  
**Authorization:** HAS_ADMIN_ROLE

### GET /api/v1/feedbacks/admin/unreplied
**Description:** Get unreplied feedback (Admin)  
**Authorization:** HAS_ADMIN_ROLE

### GET /api/v1/feedbacks/admin/search
**Description:** Search feedback (Admin)  
**Authorization:** HAS_ADMIN_ROLE

### GET /api/v1/feedbacks/admin/stats
**Description:** Get feedback statistics (Admin)  
**Authorization:** HAS_ADMIN_ROLE

---

## Notification Management

### GET /api/v1/notifications/my
**Description:** Get current user's notifications  
**Authorization:** HAS_ANY_ROLE

### GET /api/v1/notifications/{notificationId}
**Description:** Get notification by ID  
**Authorization:** HAS_ANY_ROLE

### GET /api/v1/notifications/unread-count
**Description:** Get unread notification count  
**Authorization:** HAS_ANY_ROLE

### GET /api/v1/notifications/search
**Description:** Search notifications  
**Authorization:** HAS_ANY_ROLE

### GET /api/v1/notifications/recent
**Description:** Get recent notifications  
**Authorization:** HAS_ANY_ROLE

### PUT /api/v1/notifications/{notificationId}/read
**Description:** Mark notification as read  
**Authorization:** HAS_ANY_ROLE

### PUT /api/v1/notifications/mark
**Description:** Mark multiple notifications  
**Authorization:** HAS_ANY_ROLE

### PUT /api/v1/notifications/mark-all-read
**Description:** Mark all notifications as read  
**Authorization:** HAS_ANY_ROLE

### DELETE /api/v1/notifications/{notificationId}
**Description:** Delete notification  
**Authorization:** HAS_ANY_ROLE

### DELETE /api/v1/notifications/read
**Description:** Delete all read notifications  
**Authorization:** HAS_ANY_ROLE

### GET /api/v1/notifications/stats
**Description:** Get notification statistics  
**Authorization:** HAS_ANY_ROLE

### Admin Notification Operations

### POST /api/v1/notifications/create
**Description:** Create notification (Admin)  
**Authorization:** HAS_ADMIN_ROLE

### POST /api/v1/notifications/bulk
**Description:** Create bulk notifications (Admin)  
**Authorization:** HAS_ADMIN_ROLE

### GET /api/v1/notifications/admin/all
**Description:** Get all notifications (Admin)  
**Authorization:** HAS_ADMIN_ROLE

### GET /api/v1/notifications/admin/stats
**Description:** Get system notification statistics (Admin)  
**Authorization:** HAS_ADMIN_ROLE

### DELETE /api/v1/notifications/admin/cleanup/{daysOld}
**Description:** Cleanup old notifications (Admin)  
**Authorization:** HAS_ADMIN_ROLE

---

## Admin Management

### Dashboard & Overview

### GET /api/v1/admin/dashboard
**Description:** Get system overview dashboard  
**Authorization:** HAS_ADMIN_ROLE  
**Response:** Complete system metrics and statistics

### User Management

### GET /api/v1/admin/users
**Description:** Get all users with pagination and filtering  
**Authorization:** HAS_ADMIN_ROLE  
**Query Parameters:**
- `page` (default: 0)
- `size` (default: 10)
- `sortBy` (default: "createdAt")
- `sortDir` (default: "desc")
- `role` (filter by role)

### GET /api/v1/admin/users/{userId}
**Description:** Get user details by ID  
**Authorization:** HAS_ADMIN_ROLE

### PUT /api/v1/admin/users/{userId}/status
**Description:** Update user status  
**Authorization:** HAS_ADMIN_ROLE  
**Request Body:**
```json
{
  "status": "SUSPENDED",
  "reason": "Terms violation"
}
```

### DELETE /api/v1/admin/users/{userId}
**Description:** Delete user account  
**Authorization:** HAS_ADMIN_ROLE

### Bulk Operations

### POST /api/v1/admin/bulk-operations
**Description:** Execute bulk operations  
**Authorization:** HAS_ADMIN_ROLE  
**Request Body:**
```json
{
  "operationType": "BULK_STATUS_UPDATE",
  "targetIds": [1, 2, 3],
  "parameters": {
    "status": "INACTIVE"
  }
}
```

### Reporting

### GET /api/v1/admin/reports/system
**Description:** Generate comprehensive system report  
**Authorization:** HAS_ADMIN_ROLE  
**Query Parameters:**
- `reportType` (default: "MONTHLY")
- `startDate` (required)
- `endDate` (required)

### GET /api/v1/admin/reports/users
**Description:** Get user activity report  
**Authorization:** HAS_ADMIN_ROLE

### GET /api/v1/admin/reports/revenue
**Description:** Get revenue report  
**Authorization:** HAS_ADMIN_ROLE

### System Health & Monitoring

### GET /api/v1/admin/system/health
**Description:** Get system health status  
**Authorization:** HAS_ADMIN_ROLE

### GET /api/v1/admin/system/stats
**Description:** Get quick system statistics  
**Authorization:** HAS_ADMIN_ROLE

### Advanced Operations

### POST /api/v1/admin/maintenance/backup
**Description:** Initiate system backup  
**Authorization:** HAS_ADMIN_ROLE

### POST /api/v1/admin/maintenance/cleanup
**Description:** Perform system cleanup  
**Authorization:** HAS_ADMIN_ROLE

### GET /api/v1/admin/analytics/trends
**Description:** Get business analytics trends  
**Authorization:** HAS_ADMIN_ROLE  
**Query Parameters:**
- `days` (default: 30)

### GET /api/v1/admin/performance/metrics
**Description:** Get system performance metrics  
**Authorization:** HAS_ADMIN_ROLE

---

## System Testing

### GET /api/v1/test/health
**Description:** System health check  
**Authorization:** Public

### GET /api/v1/test/database
**Description:** Database connectivity test  
**Authorization:** Public

### GET /api/v1/test/customer
**Description:** Customer role test endpoint  
**Authorization:** HAS_CUSTOMER_ROLE

### GET /api/v1/test/technician
**Description:** Technician role test endpoint  
**Authorization:** HAS_TECHNICIAN_ROLE

### GET /api/v1/test/admin
**Description:** Admin role test endpoint  
**Authorization:** HAS_ADMIN_ROLE

### GET /api/v1/test/any-role
**Description:** Any authenticated user test endpoint  
**Authorization:** HAS_ANY_ROLE

---

## API Testing Information

### Postman Collection
- Complete Postman collection available: `Fix4Home_Complete_Postman_Collection.json`
- All endpoints include proper examples and test scenarios
- Environment variables for easy configuration

### PowerShell Test Suite
- Automated test suite: `complete_api_test_suite.ps1`
- Covers all endpoints with authentication flow
- Includes success and error scenarios

### Health Check Endpoints
- `/api/v1/test/health` - Overall system health
- `/api/v1/test/database` - Database connectivity
- `/api/v1/services/health` - Service management health
- `/api/v1/customers/health` - Customer management health

---

## Notes

1. **API Versioning:** All endpoints use `/api/v1/` for future compatibility
2. **Authentication:** JWT tokens must be included in `Authorization: Bearer <token>` header
3. **Pagination:** Most list endpoints support pagination with `page`, `size`, `sortBy`, `sortDir` parameters
4. **Search:** Search functionality available for users, services, notifications, and feedback
5. **Role-based Access:** Proper authorization enforced on all protected endpoints
6. **Error Handling:** Consistent error responses with appropriate HTTP status codes
7. **Documentation:** All endpoints include OpenAPI/Swagger documentation

Last Updated: January 2024 