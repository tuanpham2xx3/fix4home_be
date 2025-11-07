# Fix4Home API - Quick Reference Card

## 🚀 Base URLs

| Environment | URL |
|------------|-----|
| Local | `http://localhost:8100/api/v1` |
| Staging | `https://staging-api.fix4home.com/api/v1` |
| Production | `https://api.fix4home.com/api/v1` |

## 🔐 Authentication

### Register
```http
POST /auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "role": "CUSTOMER",
  "fullName": "John Doe",
  "phone": "0123456789"
}
```

### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

### Use Token
```http
Authorization: Bearer YOUR_JWT_TOKEN
```

## 👥 User Roles

| Role | Description | Permissions |
|------|-------------|-------------|
| **CUSTOMER** | End users requesting services | Create requests, manage profile, payments, feedback |
| **TECHNICIAN** | Service providers | Accept jobs, manage profile, receive payments |
| **ADMIN** | System administrators | Full access to all resources |

## 📋 Common Endpoints

### 🛠️ Services
```http
GET    /services              # List all services
GET    /services/{id}         # Get service details
POST   /services              # Create service (Admin)
PUT    /services/{id}         # Update service (Admin)
DELETE /services/{id}         # Delete service (Admin)
```

### 🎫 Service Requests
```http
GET    /service-requests                    # List my requests
POST   /service-requests                    # Create request
GET    /service-requests/{id}               # Get request details
PUT    /service-requests/{id}/cancel        # Cancel request
PUT    /service-requests/{id}/accept        # Accept (Technician)
PUT    /service-requests/{id}/complete      # Complete (Technician)
```

### 👤 Customer Profile
```http
GET    /customers/profile            # Get my profile
PUT    /customers/profile            # Update profile
GET    /customers/addresses          # List addresses
POST   /customers/addresses          # Add address
PUT    /customers/addresses/{id}     # Update address
DELETE /customers/addresses/{id}     # Delete address
```

### 🔧 Technician Profile
```http
GET    /technicians/profile          # Get my profile
PUT    /technicians/profile          # Update profile
GET    /technicians/active           # List active technicians
GET    /technicians/search           # Search technicians
GET    /technicians/skills           # List skills
```

### 📝 Service Posts
```http
GET    /service-posts                      # List posts
POST   /service-posts                      # Create post
GET    /service-posts/{id}                 # Get post details
GET    /service-posts/{id}/responses       # Get responses
POST   /service-posts/{id}/responses       # Submit response (Technician)
```

### 💰 Payments
```http
POST   /payments                # Create payment
GET    /payments/history        # Payment history
GET    /payments/methods        # Available methods
```

### ⭐ Feedback
```http
POST   /feedback                     # Create feedback
GET    /feedback/service/{id}        # Get service feedback
GET    /feedbacks/public             # Public feedbacks
GET    /feedbacks/my                 # My feedbacks
```

### 🔔 Notifications
```http
GET    /notifications/my                    # My notifications
GET    /notifications/unread-count          # Unread count
PUT    /notifications/{id}/read             # Mark as read
DELETE /notifications/{id}                  # Delete notification
```

### 💬 Chat
```http
GET    /conversations                           # List conversations
GET    /conversations/{id}/messages             # Get messages
POST   /conversations/{id}/messages             # Send message
```

### 🚨 Complaints
```http
GET    /complaints                  # List complaints
POST   /complaints                  # Create complaint
GET    /complaints/{id}             # Get complaint details
PUT    /complaints/{id}/resolve     # Resolve (Admin)
```

### 👑 Admin
```http
GET    /admin/dashboard             # Dashboard overview
GET    /admin/users                 # List all users
PUT    /admin/users/status          # Update user status
GET    /admin/system/health         # System health
GET    /admin/reports/system        # System reports
```

## 🔍 Query Parameters

### Pagination
```http
GET /endpoint?page=0&size=10&sortBy=id&sortDir=asc
```

### Filtering
```http
GET /service-requests?status=PENDING
GET /technicians?skillIds=1,2,3
```

### Search
```http
GET /technicians/search?keyword=plumber
GET /notifications/search?keyword=payment
```

## 📊 Response Format

### Success Response
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { /* response data */ },
  "timestamp": "2024-11-03T10:30:00Z"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2024-11-03T10:30:00Z"
}
```

## 🚦 HTTP Status Codes

| Code | Meaning | Description |
|------|---------|-------------|
| 200 | OK | Success |
| 201 | Created | Resource created |
| 400 | Bad Request | Invalid input |
| 401 | Unauthorized | Not authenticated |
| 403 | Forbidden | No permission |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource exists |
| 500 | Server Error | Internal error |

## 🎯 Request Examples

### Create Service Request
```bash
curl -X POST "http://localhost:8100/api/v1/service-requests" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "serviceId": 1,
    "addressId": 1,
    "description": "Leaking pipe needs repair",
    "scheduledTime": "2024-11-05T10:00:00Z"
  }'
```

### Create Payment
```bash
curl -X POST "http://localhost:8100/api/v1/payments" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "serviceRequestId": 1,
    "amount": 300000,
    "paymentMethod": "CREDIT_CARD"
  }'
```

### Submit Feedback
```bash
curl -X POST "http://localhost:8100/api/v1/feedback" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "serviceRequestId": 1,
    "rating": 5,
    "comment": "Excellent service!"
  }'
```

## 💡 Tips & Tricks

### 1. Testing Authentication
```bash
# Store token in variable
TOKEN=$(curl -X POST "http://localhost:8100/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}' \
  | jq -r '.data.token')

# Use token in subsequent requests
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8100/api/v1/customers/profile"
```

### 2. Pagination Example
```javascript
async function getAllServices() {
  let page = 0;
  let allServices = [];
  let hasMore = true;

  while (hasMore) {
    const response = await fetch(
      `/services?page=${page}&size=50`,
      { headers: { Authorization: `Bearer ${token}` } }
    );
    const data = await response.json();
    
    allServices = [...allServices, ...data.data.content];
    hasMore = page < data.data.totalPages - 1;
    page++;
  }

  return allServices;
}
```

### 3. Error Handling
```javascript
async function apiCall(url, options) {
  try {
    const response = await fetch(url, options);
    const data = await response.json();
    
    if (!response.ok) {
      throw new Error(data.message || 'API call failed');
    }
    
    return data;
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
}
```

## 🔗 Useful Links

- **Swagger UI**: [Open swagger-ui.html](./swagger-ui.html)
- **ReDoc**: [Open redoc.html](./redoc.html)
- **API Tester**: [Open test-api.html](./test-api.html)
- **Frontend Integration**: [Read guide](./FRONTEND_INTEGRATION.md)
- **Full Documentation**: [Read README](./README.md)

## 📞 Support

For help with the API:
1. Check [API Documentation](../api/README_API.md)
2. Review [Postman Collection](../collections/)
3. Test in [Swagger UI](./swagger-ui.html)
4. Contact backend team

---

**Version**: 1.0.0 | **Last Updated**: November 2024

