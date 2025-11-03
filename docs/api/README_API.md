# FIX4HOME API QUICK START GUIDE

## 📚 Documentation Files

### Primary Documentation
- **`API_COMPLETE_DOCUMENTATION.md`** - Complete API reference with 113+ endpoints
- **`README_API.md`** (this file) - Quick start guide
- **`.document/API.md`** - Legacy documentation (deprecated, use for migration reference only)

### Testing Resources
- **`Fix4Home_Complete_Postman_Collection.json`** - Postman collection with all endpoints
- **`complete_api_test_suite.ps1`** - PowerShell automation test suite
- **`POSTMAN_COLLECTION_GUIDE.md`** - Guide for using Postman collection

---

## 🚀 Quick Start

### Base URL
```
http://localhost:8080/api/v1
```

### API Version
All endpoints use `/api/v1/` versioning for future compatibility.

---

## 🔐 Authentication

### 1. Register New User
```bash
POST /api/v1/auth/register
```
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

### 2. Login
```bash
POST /api/v1/auth/login
```
```json
{
  "usernameOrEmail": "john@example.com",
  "password": "SecurePass123"
}
```

### 3. Use JWT Token
Include in all authenticated requests:
```
Authorization: Bearer <your-jwt-token>
```

---

## 👥 User Roles & Permissions

| Role | Description | Example Endpoints |
|------|-------------|-------------------|
| **CUSTOMER** | End users requesting services | `/api/v1/customers/*`, `/api/v1/service-requests/*` |
| **TECHNICIAN** | Service providers | `/api/v1/technicians/*`, `/api/v1/service-requests/*` |
| **ADMIN** | System administrators | `/api/v1/admin/*`, all endpoints |

---

## 📋 Common Use Cases

### Customer Flow
1. **Register/Login** → Get JWT token
2. **Create Address** → `POST /api/v1/customers/addresses`
3. **Browse Services** → `GET /api/v1/services`
4. **Create Service Request** → `POST /api/v1/service-requests`
5. **Track Request** → `GET /api/v1/service-requests/my`
6. **Make Payment** → `POST /api/v1/payments/create`
7. **Leave Feedback** → `POST /api/v1/feedbacks/create`

### Technician Flow
1. **Register/Login** → Get JWT token
2. **Update Profile** → `PUT /api/v1/technicians/me`
3. **Set Skills** → `PUT /api/v1/technicians/me/skills`
4. **Browse Available Jobs** → `GET /api/v1/service-requests/available`
5. **Accept Assignment** → `PUT /api/v1/service-requests/{id}/accept`
6. **Complete Work** → `PUT /api/v1/service-requests/{id}/complete`
7. **View Earnings** → `GET /api/v1/payments/technician/my`

### Admin Flow
1. **Login** → Get JWT token
2. **View Dashboard** → `GET /api/v1/admin/dashboard`
3. **Manage Users** → `GET /api/v1/admin/users`
4. **Monitor System** → `GET /api/v1/admin/system/health`
5. **Generate Reports** → `GET /api/v1/admin/reports/system`

---

## 🛠️ Testing Your Setup

### Health Check Endpoints
```bash
# System health
GET /api/v1/test/health

# Database connectivity  
GET /api/v1/test/database

# Role-based tests (require authentication)
GET /api/v1/test/customer    # Customer role
GET /api/v1/test/technician  # Technician role  
GET /api/v1/test/admin       # Admin role
GET /api/v1/test/any-role    # Any authenticated user
```

### Using Postman
1. Import `Fix4Home_Complete_Postman_Collection.json`
2. Set environment variable `baseUrl` to `http://localhost:8080`
3. Run authentication requests to get JWT tokens
4. Test other endpoints with proper authorization

### Using PowerShell
```powershell
# Run complete test suite
.\complete_api_test_suite.ps1

# This will test all endpoints with proper authentication flow
```

---

## 📊 API Categories Overview

| Category | Count | Purpose |
|----------|-------|---------|
| **Authentication** | 3 | User registration, login, logout |
| **Customer Management** | 11 | Customer profiles, addresses |
| **Technician Management** | 16 | Technician profiles, skills |
| **Service Management** | 12 | Service catalog management |
| **Service Requests** | 12 | Request lifecycle management |
| **Payment Management** | 9 | Payment processing, tracking |
| **Feedback Management** | 13 | Reviews and ratings |
| **Notification Management** | 16 | System notifications |
| **Admin Management** | 15+ | System administration |
| **System Testing** | 6 | Health checks, testing |

**Total: 113+ endpoints**

---

## 🔄 Response Format

All API responses follow this consistent structure:

### Success Response
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    // Response data here
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## 🚦 HTTP Status Codes

| Code | Meaning | Description |
|------|---------|-------------|
| 200 | OK | Successful operation |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid request data |
| 401 | Unauthorized | Authentication required |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource already exists |
| 500 | Internal Server Error | Server error |

---

## 🔍 Advanced Features

### Pagination
Most list endpoints support pagination:
```bash
GET /api/v1/customers/paginated?page=0&size=10&sortBy=username&sortDir=asc
```

### Search & Filtering
```bash
# Search technicians
GET /api/v1/technicians/search?keyword=plumber&skillIds=1,2,3

# Filter service requests by status
GET /api/v1/service-requests/status/PENDING

# Search notifications
GET /api/v1/notifications/search?keyword=payment
```

### Bulk Operations (Admin)
```bash
POST /api/v1/admin/bulk-operations
```
```json
{
  "operationType": "BULK_STATUS_UPDATE",
  "targetIds": [1, 2, 3],
  "parameters": {
    "status": "INACTIVE"
  }
}
```

---

## 🐛 Troubleshooting

### Common Issues

1. **401 Unauthorized**
   - Check JWT token in Authorization header
   - Ensure token hasn't expired
   - Verify role permissions

2. **404 Not Found**
   - Verify endpoint URL (use `/api/v1/` not `/api/`)
   - Check if resource exists
   - Confirm path parameters

3. **403 Forbidden**
   - Check user role permissions
   - Verify authorization patterns in documentation

4. **500 Internal Server Error**
   - Check server logs
   - Verify database connectivity
   - Validate request payload

### Getting Help

1. **Check Documentation:** `API_COMPLETE_DOCUMENTATION.md`
2. **Test with Postman:** Use provided collection
3. **Run Health Checks:** `/api/v1/test/health`
4. **Check Server Logs:** Application console output

---

## 📈 Performance Tips

1. **Use Pagination:** For large datasets
2. **Implement Caching:** For frequently accessed data
3. **Optimize Queries:** Use search parameters effectively
4. **Monitor Health:** Regular health check calls
5. **Batch Operations:** Use bulk endpoints when available

---

## 🔄 Migration from Legacy API

If you're migrating from old API paths:

| Old Path | New Path |
|----------|----------|
| `/api/users` | `/api/v1/auth/*` |
| `/api/customer-profiles` | `/api/v1/customers/*` |
| `/api/technician-profiles` | `/api/v1/technicians/*` |
| `/api/services` | `/api/v1/services/*` |
| `/api/service-requests` | `/api/v1/service-requests/*` |
| `/api/feedbacks` | `/api/v1/feedbacks/*` |
| `/api/payments` | `/api/v1/payments/*` |
| `/api/notifications` | `/api/v1/notifications/*` |

---

## 📞 Support

For technical support or questions:
- Check `API_COMPLETE_DOCUMENTATION.md` for detailed information
- Use Postman collection for testing
- Run PowerShell test suite for comprehensive validation

---

**Last Updated:** January 2024  
**API Version:** v1  
**Total Endpoints:** 113+ 