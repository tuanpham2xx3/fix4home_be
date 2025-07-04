# Fix4Home Postman Collection Guide

## 📋 Tổng quan

Fix4Home Postman Collection được chia thành 2 files:

1. **`Fix4Home_Complete_Postman_Collection.json`** - File chính với System Health & Authentication
2. **`Fix4Home_Postman_Extended.json`** - File bổ sung với Services & Customer Management

## 🚀 Cách sử dụng

### Bước 1: Import Collections
1. Mở Postman
2. Click **Import** 
3. Import cả 2 files JSON
4. Hoặc copy nội dung từ `Fix4Home_Postman_Extended.json` vào file chính

### Bước 2: Thiết lập Environment
1. Tạo Environment mới: `Fix4Home API`
2. Thêm biến `baseUrl` = `http://localhost:8080`
3. Các biến khác sẽ được tự động set trong quá trình test

### Bước 3: Test Workflow
Chạy requests theo thứ tự:

#### 🔍 1. System Health
```
GET /api/test/health
```
- Test server đang chạy
- Validate response time < 1000ms

#### 🔐 2. Authentication Flow
```
1. Register Customer → Tự động save customerToken
2. Register Technician → Tự động save technicianToken  
3. Register Admin → Tự động save adminToken
4. Login Customer → Set authToken cho các request tiếp theo
5. Login Technician → Set techToken
6. Login Admin → Set adminToken  
7. Invalid Login Test → Test security
```

#### 🛠️ 3. Services Management (Admin)
```
1. Get All Services → List tất cả services
2. Create Service → Tạo service mới (Admin only)
3. Get Service by ID → Chi tiết service 
4. Update Service → Cập nhật service (Admin only)
5. Delete Service → Xóa service (Admin only)
```

#### 👤 4. Customer Management
```
1. Get Customer Profile → Profile hiện tại
2. Update Customer Profile → Cập nhật thông tin
3. Get Customer Addresses → List địa chỉ
4. Create Address → Thêm địa chỉ mới
5. Update Address → Sửa địa chỉ
```

## 🧪 Test Features

### Auto Token Management
- Tự động lưu tokens sau registration/login
- Sử dụng collection variables để share tokens
- Auto-switch giữa customer/technician/admin tokens

### Comprehensive Test Scripts
```javascript
// Ví dụ test script
pm.test('Registration successful', function () {
    pm.response.to.have.status(201);
});

pm.test('Response contains token', function () {
    const response = pm.response.json();
    pm.expect(response.data).to.have.property('token');
    
    // Auto save token
    if (response.data && response.data.token) {
        pm.collectionVariables.set('authToken', response.data.token);
    }
});
```

### Validation Tests
- ✅ Status code validation
- ✅ Response structure validation  
- ✅ Data type validation
- ✅ Business logic validation
- ✅ Error handling validation

## 📊 Collection Variables

| Variable | Description | Auto-Set |
|----------|-------------|----------|
| `baseUrl` | API base URL | Manual |
| `authToken` | Current user token | ✅ |
| `customerToken` | Customer token | ✅ |
| `technicianToken` | Technician token | ✅ |
| `adminToken` | Admin token | ✅ |
| `currentUserId` | Current user ID | ✅ |
| `serviceId` | Test service ID | ✅ |
| `addressId` | Test address ID | ✅ |
| `serviceRequestId` | Test request ID | ✅ |
| `paymentId` | Test payment ID | ✅ |
| `feedbackId` | Test feedback ID | ✅ |

## 🔄 Workflow Testing

### Complete User Journey
1. **Setup**: Health check
2. **Registration**: Create all user types
3. **Authentication**: Login as customer
4. **Profile**: Update customer profile
5. **Address**: Add customer address  
6. **Service**: Admin creates service
7. **Booking**: Customer creates service request
8. **Assignment**: Admin assigns technician
9. **Payment**: Customer makes payment
10. **Feedback**: Customer leaves feedback
11. **Notifications**: Check notifications

### Role-Based Testing
- **Customer**: Profile, Address, Service Requests, Payments, Feedback
- **Technician**: Profile, Skills, Assigned Requests, Status Updates
- **Admin**: User Management, Service Management, System Overview

## 🛡️ Security Testing

### Authentication Tests
- Valid login credentials
- Invalid login credentials  
- Token expiration handling
- Role-based access control

### Authorization Tests
- Customer accessing admin endpoints (should fail)
- Technician accessing customer data (should fail)
- Unauthenticated requests (should fail)

## 📈 Performance Testing

### Response Time Checks
```javascript
pm.test('Response time is acceptable', function () {
    pm.expect(pm.response.responseTime).to.be.below(1000);
});
```

### Load Testing
- Run collection with multiple iterations
- Test concurrent requests
- Monitor performance metrics

## 🔧 Advanced Usage

### Environment Switching
```javascript
// Dynamic environment switching
const env = pm.environment.get("ENVIRONMENT");
if (env === "production") {
    pm.collectionVariables.set("baseUrl", "https://api.fix4home.com");
} else {
    pm.collectionVariables.set("baseUrl", "http://localhost:8080");
}
```

### Custom Test Scripts
```javascript
// Custom validation
pm.test('Business logic validation', function () {
    const response = pm.response.json();
    
    // Check service price is positive
    if (response.data && response.data.price) {
        pm.expect(response.data.price).to.be.above(0);
    }
    
    // Check email format
    if (response.data && response.data.email) {
        pm.expect(response.data.email).to.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/);
    }
});
```

## 📝 Sections Overview

### ✅ Completed Sections
1. **System Health** - Server status checks
2. **Authentication & Authorization** - Full auth flow with detailed tests
3. **Services Management** - Complete CRUD operations
4. **Customer Management** - Profile and address management

### 🚧 To be Added
5. **Technician Management** - Profile, skills, assignments
6. **Service Requests** - Complete booking workflow
7. **Payments** - Payment processing and tracking
8. **Feedback** - Rating and review system
9. **Notifications** - Notification management
10. **Admin Dashboard** - System overview and management

## 🎯 Testing Strategy

### Unit Testing
- Test individual endpoints
- Validate request/response formats
- Check error handling

### Integration Testing  
- Test complete workflows
- Validate data flow between endpoints
- Check business logic

### End-to-End Testing
- Test complete user journeys
- Validate system behavior
- Performance and security testing

## 🔍 Troubleshooting

### Common Issues
1. **401 Unauthorized** - Check token validity
2. **403 Forbidden** - Check user role permissions
3. **404 Not Found** - Verify endpoint URLs
4. **500 Server Error** - Check server logs

### Debug Tips
- Use Postman Console for debugging
- Check collection variables values
- Verify request headers and body
- Test endpoints individually first

## 📚 Next Steps

1. Hoàn thành các sections còn lại
2. Thêm newman scripts cho CI/CD
3. Tạo automated test suite
4. Thiết lập performance benchmarks
5. Tích hợp với monitoring tools

---

**Note**: Collection này được thiết kế để test comprehensive và validate toàn bộ Fix4Home API. Sử dụng theo thứ tự được đề xuất để đảm bảo data consistency. 