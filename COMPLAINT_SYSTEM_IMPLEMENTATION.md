# COMPLAINT SYSTEM IMPLEMENTATION
## Fix4Home Backend - Hệ Thống Khiếu Nại

---

## 📋 TỔNG QUAN

Hệ thống khiếu nại (Complaint System) cho phép khách hàng và thợ sửa chữa có thể khiếu nại lẫn nhau khi có tranh chấp trong quá trình thực hiện dịch vụ. Admin sẽ có vai trò điều tra và giải quyết các khiếu nại.

### ✅ Trạng thái triển khai: **HOÀN THÀNH**

---

## 🏗️ KIẾN TRÚC HỆ THỐNG

### 1. Database Layer
- **Migration**: `V008__Create_Complaints_Table.sql`
- **Entity**: `Complaint.java`
- **Repository**: `ComplaintRepository.java`

### 2. Business Logic Layer
- **Service**: `ComplaintService.java`
- **DTOs**: `ComplaintDTO`, `CreateComplaintRequest`, `ResolveComplaintRequest`, `ComplaintStatsDTO`
- **Enums**: `ComplaintStatus`, cập nhật `ServiceRequestStatus`

### 3. API Layer
- **Controller**: `ComplaintController.java`
- **Security**: Role-based access control
- **Validation**: Business rules và input validation

### 4. Exception Handling
- `ComplaintNotFoundException`
- `ComplaintAlreadyExistsException`
- `ComplaintResolutionException`

---

## 📊 DATABASE SCHEMA

### Table: `complaints`

```sql
CREATE TABLE complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_request_id BIGINT NOT NULL,
    complainant_id BIGINT NOT NULL,      -- Người khiếu nại
    accused_id BIGINT NOT NULL,          -- Người bị khiếu nại
    reason VARCHAR(200) NOT NULL,        -- Lý do khiếu nại
    description TEXT NOT NULL,           -- Mô tả chi tiết
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    admin_response TEXT,                 -- Phản hồi từ admin
    resolved_by BIGINT,                 -- Admin xử lý
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at DATETIME,
    
    -- Foreign Keys
    FOREIGN KEY (service_request_id) REFERENCES service_requests(id),
    FOREIGN KEY (complainant_id) REFERENCES users(id),
    FOREIGN KEY (accused_id) REFERENCES users(id),
    FOREIGN KEY (resolved_by) REFERENCES users(id),
    
    -- Business Constraints
    UNIQUE KEY unique_complaint_per_service_request (service_request_id, complainant_id),
    CHECK (complainant_id != accused_id),
    CHECK (status IN ('PENDING', 'INVESTIGATING', 'RESOLVED', 'REJECTED'))
);
```

### Indexes for Performance
```sql
-- Primary indexes
INDEX idx_complaints_service_request (service_request_id)
INDEX idx_complaints_complainant (complainant_id)
INDEX idx_complaints_accused (accused_id)
INDEX idx_complaints_status (status)
INDEX idx_complaints_created_at (created_at)

-- Composite indexes
INDEX idx_complaints_service_request_status (service_request_id, status)
INDEX idx_complaints_status_created_at (status, created_at)
```

---

## 🔄 BUSINESS WORKFLOW

### 1. File Complaint (Tạo khiếu nại)
```
Customer/Technician → Files complaint → ServiceRequest status = COMPLAINING
```

### 2. Admin Investigation (Điều tra)
```
Admin → Start Investigation → Complaint status = INVESTIGATING
```

### 3. Resolution (Giải quyết)
```
Admin → Resolve/Reject → Complaint status = RESOLVED/REJECTED
                     → ServiceRequest status = COMPLAITED
```

---

## 📝 ENUMS

### ComplaintStatus
```java
public enum ComplaintStatus {
    PENDING,        // Chờ xử lý
    INVESTIGATING,  // Đang điều tra
    RESOLVED,       // Đã giải quyết
    REJECTED        // Từ chối
}
```

### ServiceRequestStatus (Updated)
```java
public enum ServiceRequestStatus {
    PENDING,
    ASSIGNED,
    IN_PROGRESS,
    DONE,
    CANCELLED,
    COMPLAINING,    // Đang khiếu nại
    COMPLAITED      // Đã xử lý khiếu nại xong
}
```

---

## 🌐 API ENDPOINTS

### Customer & Technician Operations

#### Tạo khiếu nại
```http
POST /api/v1/complaints
Authorization: Bearer {token}
Content-Type: application/json

{
    "serviceRequestId": 123,
    "accusedId": 456,
    "reason": "Thợ không đến đúng giờ hẹn",
    "description": "Thợ hẹn 8h sáng nhưng đến 10h mới tới..."
}
```

#### Xem khiếu nại của tôi
```http
GET /api/v1/complaints/my
Authorization: Bearer {token}
```

#### Xem khiếu nại chống lại tôi
```http
GET /api/v1/complaints/against-me
Authorization: Bearer {token}
```

#### Xem chi tiết khiếu nại
```http
GET /api/v1/complaints/{id}
Authorization: Bearer {token}
```

### Admin Operations

#### Xem tất cả khiếu nại (có phân trang)
```http
GET /api/v1/complaints?page=0&size=10&sortBy=createdAt&sortDir=desc
Authorization: Bearer {admin-token}
```

#### Lọc khiếu nại theo trạng thái
```http
GET /api/v1/complaints/status/{status}
Authorization: Bearer {admin-token}
```

#### Xem khiếu nại đang chờ xử lý
```http
GET /api/v1/complaints/pending
Authorization: Bearer {admin-token}
```

#### Xem khiếu nại khẩn cấp (>48h)
```http
GET /api/v1/complaints/urgent
Authorization: Bearer {admin-token}
```

#### Bắt đầu điều tra
```http
PUT /api/v1/complaints/{id}/investigate
Authorization: Bearer {admin-token}
```

#### Giải quyết khiếu nại
```http
PUT /api/v1/complaints/{id}/resolve
Authorization: Bearer {admin-token}
Content-Type: application/json

{
    "status": "RESOLVED",
    "adminResponse": "Sau khi điều tra, chúng tôi thấy..."
}
```

#### Thống kê khiếu nại
```http
GET /api/v1/complaints/stats
Authorization: Bearer {admin-token}
```

---

## 📋 DTOs

### CreateComplaintRequest
```java
{
    "serviceRequestId": Long,
    "accusedId": Long,
    "reason": String (10-200 chars),
    "description": String (20-2000 chars)
}
```

### ComplaintDTO
```java
{
    "id": Long,
    "serviceRequest": ServiceRequestSummaryDTO,
    "complainant": UserSummaryDTO,
    "accused": UserSummaryDTO,
    "reason": String,
    "description": String,
    "status": ComplaintStatus,
    "adminResponse": String,
    "resolvedBy": UserSummaryDTO,
    "createdAt": LocalDateTime,
    "resolvedAt": LocalDateTime,
    "canBeModified": Boolean,
    "canBeInvestigated": Boolean,
    "canBeResolved": Boolean
}
```

### ResolveComplaintRequest
```java
{
    "status": ComplaintStatus, // RESOLVED or REJECTED
    "adminResponse": String (required)
}
```

### ComplaintStatsDTO
```java
{
    "totalComplaints": Long,
    "pendingComplaints": Long,
    "investigatingComplaints": Long,
    "resolvedComplaints": Long,
    "rejectedComplaints": Long,
    "todayComplaints": Long,
    "thisWeekComplaints": Long,
    "thisMonthComplaints": Long,
    "averageResolutionTimeHours": Double,
    "resolutionRate": Double
}
```

---

## 🛡️ SECURITY & VALIDATION

### Role-based Access Control
```java
// Customer & Technician operations
@PreAuthorize("hasRole('CUSTOMER') or hasRole('TECHNICIAN')")

// Admin operations  
@PreAuthorize("hasRole('ADMIN')")
```

### Business Rules
- ✅ Không thể khiếu nại chính mình
- ✅ Chỉ một khiếu nại trên mỗi service request cho mỗi người
- ✅ Chỉ có thể khiếu nại khi service request đã hoàn thành (DONE)
- ✅ Chỉ người liên quan hoặc admin mới xem được chi tiết khiếu nại
- ✅ Chỉ admin mới có thể điều tra và giải quyết

### Input Validation
- **Reason**: 10-200 ký tự
- **Description**: 20-2000 ký tự
- **Admin Response**: Bắt buộc khi giải quyết
- **Status transitions**: Chỉ cho phép chuyển đổi hợp lệ

---

## 🔍 BUSINESS METHODS

### Complaint Entity Methods
```java
// Status checks
boolean isPending()
boolean isInvestigating() 
boolean isResolved()
boolean isRejected()

// Permission checks
boolean canBeModified()      // PENDING only
boolean canBeInvestigated()  // PENDING only
boolean canBeResolved()      // INVESTIGATING only

// State transitions
void startInvestigation(User admin)
void resolve(String adminResponse, User admin)
void reject(String adminResponse, User admin)
```

---

## 📈 ADMIN DASHBOARD FEATURES

### Thống kê tổng quan
- Tổng số khiếu nại
- Khiếu nại theo trạng thái
- Khiếu nại hôm nay/tuần này/tháng này
- Thời gian giải quyết trung bình
- Tỷ lệ giải quyết thành công

### Quản lý khiếu nại
- Xem danh sách có phân trang và sắp xếp
- Lọc theo trạng thái
- Xem khiếu nại khẩn cấp (>48h)
- Bắt đầu điều tra
- Giải quyết với phản hồi chi tiết

---

## 🧪 TESTING

### Integration Tests
- ✅ Create complaint workflow
- ✅ Admin investigation workflow  
- ✅ Resolution workflow
- ✅ Security and authorization tests
- ✅ Business rule validation tests
- ✅ Statistics generation tests

### Test Coverage
- Unit tests cho service layer
- Integration tests cho API endpoints
- Database constraint tests
- Security tests

---

## 🚀 DEPLOYMENT NOTES

### Database Migration
```bash
# Migration sẽ tự động chạy khi start application
# V008__Create_Complaints_Table.sql
```

### Security Configuration
```java
// Đã được cấu hình trong SecurityConfig.java
.requestMatchers("/api/v1/complaints/**").hasAnyRole("CUSTOMER", "TECHNICIAN", "ADMIN")
```

### Dependencies
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Spring Boot Starter Validation
- Lombok

---

## 📱 FRONTEND INTEGRATION

### Required UI Components
- Complaint form (tạo khiếu nại)
- Complaint list (danh sách khiếu nại)
- Complaint details (chi tiết khiếu nại)
- Admin dashboard (quản trị viên)
- Statistics charts (biểu đồ thống kê)

### API Integration Examples
```javascript
// Tạo khiếu nại
const createComplaint = async (complaintData) => {
    const response = await fetch('/api/v1/complaints', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(complaintData)
    });
    return response.json();
};

// Admin xem thống kê
const getComplaintStats = async () => {
    const response = await fetch('/api/v1/complaints/stats', {
        headers: {
            'Authorization': `Bearer ${adminToken}`
        }
    });
    return response.json();
};
```

---

## 🎯 KẾT LUẬN

Hệ thống khiếu nại đã được **triển khai hoàn chỉnh** với:

### ✅ Tính năng đầy đủ
- Tạo và quản lý khiếu nại
- Workflow điều tra và giải quyết
- Dashboard admin với thống kê chi tiết
- Security và validation hoàn chỉnh

### ✅ Chất lượng cao
- Code tuân thủ best practices
- Test coverage đầy đủ
- Database design tối ưu
- API documentation đầy đủ

### ✅ Sẵn sàng production
- Performance optimization
- Security hardening
- Error handling robust
- Monitoring và logging

Hệ thống có thể đáp ứng đầy đủ yêu cầu nghiệp vụ xử lý tranh chấp trong nền tảng Fix4Home.

---

**Tác giả**: Fix4Home Development Team  
**Ngày tạo**: 08/01/2025  
**Phiên bản**: 1.0.0 