# CONSULTATION & QUOTATION SYSTEM IMPLEMENTATION

## TỔNG QUAN
Hệ thống **Consultation & Quotation** cho phép thợ gửi tư vấn và báo giá cho các tin đăng dịch vụ, và khách hàng có thể chấp nhận hoặc từ chối các đề xuất này.

---

## CẤU TRÚC THÀNH PHẦN

### 1. ENUM - ConsultationStatus
```java
public enum ConsultationStatus {
    PENDING,    // Chờ phản hồi từ khách hàng
    ACCEPTED,   // Được khách hàng chấp nhận
    REJECTED    // Bị khách hàng từ chối
}
```

### 2. ENTITY - Consultation
**Vị trí:** `src/main/java/com/fix4home/fix4home/model/entity/Consultation.java`

**Các trường chính:**
- `Long id` - ID tư vấn
- `ServicePost servicePost` - Tin đăng liên quan
- `User technician` - Thợ gửi tư vấn
- `String proposal` - Đề xuất chi tiết
- `BigDecimal quotedPrice` - Giá báo
- `String notes` - Ghi chú bổ sung
- `ConsultationStatus status` - Trạng thái
- `LocalDateTime submittedAt` - Thời gian gửi
- `LocalDateTime respondedAt` - Thời gian phản hồi

**Business Methods:**
- `isPending()`, `isAccepted()`, `isRejected()`
- `canBeModified()` - Kiểm tra có thể chỉnh sửa
- `accept()`, `reject()` - Cập nhật trạng thái

**Constraints:**
- Unique constraint: (service_post_id, technician_id)
- Check constraint: quoted_price >= 0

### 3. DTOs

#### CreateConsultationRequest
```java
{
    "servicePostId": Long,
    "proposal": String (10-2000 chars),
    "quotedPrice": BigDecimal (>= 0),
    "notes": String (max 1000 chars)
}
```

#### ConsultationDTO
```java
{
    "id": Long,
    "servicePostId": Long,
    "servicePost": ServicePostSummaryDTO,
    "technicianId": Long,
    "technicianName": String,
    "technicianPhone": String,
    "technicianEmail": String,
    "technicianRating": Double,
    "proposal": String,
    "quotedPrice": BigDecimal,
    "notes": String,
    "status": ConsultationStatus,
    "submittedAt": LocalDateTime,
    "respondedAt": LocalDateTime,
    "isPending": Boolean,
    "isAccepted": Boolean,
    "isRejected": Boolean,
    "canBeModified": Boolean
}
```

#### UpdateConsultationStatusRequest
```java
{
    "status": ConsultationStatus,
    "rejectionReason": String (optional)
}
```

### 4. REPOSITORY - ConsultationRepository
**Vị trí:** `src/main/java/com/fix4home/fix4home/repository/ConsultationRepository.java`

**Các query methods chính:**
- `findByTechnicianOrderBySubmittedAtDesc(User technician)`
- `findByServicePostOrderBySubmittedAtDesc(ServicePost servicePost)`
- `findByServicePostAndTechnician(ServicePost, User)` - Kiểm tra duplicate
- `findPendingConsultationsByServicePost(ServicePost)`
- `findAcceptedConsultationsByServicePost(ServicePost)`
- `findConsultationsByCustomer(User customer)`
- `countByTechnician(User)`, `countByServicePost(ServicePost)`
- `findConsultationsByPriceRange(BigDecimal min, BigDecimal max)`

### 5. SERVICE - ConsultationService
**Vị trí:** `src/main/java/com/fix4home/fix4home/service/ConsultationService.java`

#### Technician Operations
- `submitConsultation(CreateConsultationRequest)` - Gửi tư vấn
- `getMyConsultations()` - Xem tư vấn đã gửi
- `getMyConsultationsWithPagination(...)` - Phân trang
- `getMyConsultationsByStatus(ConsultationStatus)` - Lọc theo trạng thái

#### Customer Operations  
- `getConsultationsForServicePost(Long servicePostId)` - Xem tư vấn cho tin đăng
- `getConsultationsForServicePostWithPagination(...)` - Phân trang
- `updateConsultationStatus(Long id, UpdateConsultationStatusRequest)` - Accept/Reject
- `getMyConsultationsAsCustomer()` - Xem tư vấn đã nhận
- `getMyConsultationsAsCustomerWithPagination(...)` - Phân trang

#### Shared Operations
- `getConsultationById(Long id)` - Xem chi tiết (có kiểm tra quyền)

#### Admin Operations
- `getAllConsultations()` - Xem tất cả tư vấn
- `getAllConsultationsWithPagination(...)` - Phân trang

**Validation Logic:**
- Kiểm tra service post khả dụng cho tư vấn
- Kiểm tra thợ không gửi duplicate consultation
- Kiểm tra quyền sở hữu service post
- Kiểm tra trạng thái có thể chỉnh sửa

### 6. CONTROLLER - ConsultationController
**Vị trí:** `src/main/java/com/fix4home/fix4home/controller/ConsultationController.java`

**Base Path:** `/api/v1/consultations`

---

## API ENDPOINTS

### TECHNICIAN ENDPOINTS

#### 1. Gửi tư vấn
```http
POST /api/v1/consultations
Authorization: Bearer <technician_token>
Content-Type: application/json

{
    "servicePostId": 1,
    "proposal": "Tôi có thể sửa chữa điện cho bạn với kinh nghiệm 5 năm...",
    "quotedPrice": 500000,
    "notes": "Bao gồm vật liệu và bảo hành 6 tháng"
}
```

#### 2. Xem tư vấn đã gửi
```http
GET /api/v1/consultations/my-proposals
Authorization: Bearer <technician_token>
```

#### 3. Xem tư vấn đã gửi (phân trang)
```http
GET /api/v1/consultations/my-proposals/paginated?page=0&size=10&sortBy=submittedAt&sortDir=desc
Authorization: Bearer <technician_token>
```

#### 4. Lọc tư vấn theo trạng thái
```http
GET /api/v1/consultations/my-proposals/by-status?status=PENDING
Authorization: Bearer <technician_token>
```

### CUSTOMER ENDPOINTS

#### 5. Xem tư vấn cho tin đăng
```http
GET /api/v1/consultations/post/{servicePostId}
Authorization: Bearer <customer_token>
```

#### 6. Xem tư vấn cho tin đăng (phân trang)
```http
GET /api/v1/consultations/post/{servicePostId}/paginated?page=0&size=10
Authorization: Bearer <customer_token>
```

#### 7. Chấp nhận tư vấn
```http
PUT /api/v1/consultations/{id}/accept
Authorization: Bearer <customer_token>
```

#### 8. Từ chối tư vấn
```http
PUT /api/v1/consultations/{id}/reject?rejectionReason=Giá quá cao
Authorization: Bearer <customer_token>
```

#### 9. Xem tư vấn đã nhận
```http
GET /api/v1/consultations/my-received
Authorization: Bearer <customer_token>
```

#### 10. Xem tư vấn đã nhận (phân trang)
```http
GET /api/v1/consultations/my-received/paginated?page=0&size=10
Authorization: Bearer <customer_token>
```

### SHARED ENDPOINTS

#### 11. Xem chi tiết tư vấn
```http
GET /api/v1/consultations/{id}
Authorization: Bearer <token>
```

### ADMIN ENDPOINTS

#### 12. Xem tất cả tư vấn
```http
GET /api/v1/consultations/admin/all
Authorization: Bearer <admin_token>
```

#### 13. Xem tất cả tư vấn (phân trang)
```http
GET /api/v1/consultations/admin/all/paginated?page=0&size=20
Authorization: Bearer <admin_token>
```

#### 14. Health check
```http
GET /api/v1/consultations/health
```

---

## DATABASE MIGRATION

### V007__Create_Consultations_Table.sql
**Vị trí:** `src/main/resources/db/migration/V007__Create_Consultations_Table.sql`

```sql
CREATE TABLE consultations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_post_id BIGINT NOT NULL,
    technician_id BIGINT NOT NULL,
    proposal TEXT NOT NULL,
    quoted_price DECIMAL(12,2) NOT NULL,
    notes TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at DATETIME,
    
    CONSTRAINT fk_consultations_service_post 
        FOREIGN KEY (service_post_id) REFERENCES service_posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_consultations_technician 
        FOREIGN KEY (technician_id) REFERENCES users(id) ON DELETE CASCADE,
        
    UNIQUE KEY unique_consultation_per_technician (service_post_id, technician_id),
    
    -- Indexes for performance
    INDEX idx_consultations_service_post (service_post_id),
    INDEX idx_consultations_technician (technician_id),
    INDEX idx_consultations_status (status),
    INDEX idx_consultations_submitted_at (submitted_at),
    INDEX idx_consultations_service_post_status (service_post_id, status),
    INDEX idx_consultations_technician_status (technician_id, status)
);

-- Constraints
ALTER TABLE consultations 
ADD CONSTRAINT chk_consultations_status 
    CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED'));

ALTER TABLE consultations 
ADD CONSTRAINT chk_consultations_quoted_price 
    CHECK (quoted_price >= 0);
```

---

## SECURITY & VALIDATION

### Role-based Access Control
- **TECHNICIAN:** Có thể gửi tư vấn, xem tư vấn của mình
- **CUSTOMER:** Có thể xem tư vấn cho tin đăng của mình, accept/reject
- **ADMIN:** Có thể xem tất cả tư vấn trong hệ thống

### Input Validation
- **Proposal:** 10-2000 ký tự
- **Quoted Price:** >= 0, max 10 số nguyên + 2 số thập phân
- **Notes:** Max 1000 ký tự
- **Service Post ID:** Phải tồn tại và khả dụng

### Business Validation
- Thợ không thể gửi 2 tư vấn cho cùng 1 tin đăng
- Chỉ có thể accept/reject tư vấn ở trạng thái PENDING
- Chỉ chủ sở hữu tin đăng mới có thể accept/reject
- Service post phải ở trạng thái POSTED hoặc RESPONSES_RECEIVED

---

## WORKFLOW HOẠT ĐỘNG

### 1. Thợ gửi tư vấn
```
1. Thợ xem danh sách tin đăng khả dụng
2. Chọn tin đăng muốn tư vấn
3. Gửi POST request với proposal và quoted price
4. Hệ thống validate và lưu tư vấn với status PENDING
```

### 2. Khách hàng xem và phản hồi
```
1. Khách hàng xem danh sách tư vấn cho tin đăng
2. Đọc chi tiết từng tư vấn
3. Chọn accept hoặc reject tư vấn
4. Hệ thống cập nhật status và responded_at
```

### 3. Luồng nghiệp vụ
```
Service Post (POSTED) 
    ↓
Technician submits Consultation (PENDING)
    ↓
Customer reviews Consultations
    ↓
Customer accepts/rejects → Status updated
    ↓
If accepted → Can proceed to create Service Request
```

---

## RESPONSE EXAMPLES

### Success Response
```json
{
    "success": true,
    "message": "Consultation submitted successfully",
    "data": {
        "id": 123,
        "servicePostId": 1,
        "servicePost": {
            "id": 1,
            "title": "Sửa chữa điện",
            "serviceName": "Điện lạnh",
            "address": "123 Nguyễn Văn A, Q1, HCM"
        },
        "technicianId": 456,
        "technicianName": "Nguyễn Văn B",
        "technicianPhone": "0901234567",
        "technicianEmail": "technician@email.com",
        "technicianRating": 4.5,
        "proposal": "Tôi có thể sửa chữa điện cho bạn...",
        "quotedPrice": 500000,
        "notes": "Bao gồm vật liệu",
        "status": "PENDING",
        "submittedAt": "2024-01-15T10:30:00",
        "respondedAt": null,
        "isPending": true,
        "isAccepted": false,
        "isRejected": false,
        "canBeModified": true
    }
}
```

### Error Response
```json
{
    "success": false,
    "message": "You have already submitted a consultation for this service post",
    "timestamp": "2024-01-15T10:30:00"
}
```

---

## TESTING

### Unit Tests (Khuyến nghị)
- ConsultationServiceTest
- ConsultationControllerTest  
- ConsultationRepositoryTest

### Integration Tests (Khuyến nghị)
- Full workflow từ submit → accept/reject
- Permission testing
- Validation testing

### Test Data
```sql
-- Service Posts cần tồn tại trước
-- Users với role TECHNICIAN và CUSTOMER cần tồn tại

INSERT INTO consultations (service_post_id, technician_id, proposal, quoted_price, notes, status)
VALUES 
(1, 4, 'Tôi có thể sửa chữa ổ cắm điện...', 180000, 'Bao gồm vật liệu', 'PENDING'),
(1, 5, 'Với kinh nghiệm 5 năm...', 220000, 'Bảo hành 6 tháng', 'PENDING'),
(2, 6, 'Lắp đặt máy lạnh 1.5HP...', 450000, 'Bảo hành 2 năm', 'ACCEPTED');
```

---

## KẾT LUẬN

Hệ thống **Consultation & Quotation** đã được triển khai đầy đủ với:

✅ **Database Schema** - Bảng consultations với đầy đủ constraints  
✅ **Entity & DTOs** - Mapping đầy đủ với validation  
✅ **Repository Layer** - Các query methods tối ưu  
✅ **Service Layer** - Business logic đầy đủ với validation  
✅ **Controller Layer** - REST APIs với security  
✅ **Role-based Access** - Phân quyền theo vai trò  
✅ **Error Handling** - Xử lý lỗi đầy đủ  

Hệ thống sẵn sàng tích hợp với Service Posts và hoạt động trong môi trường production. 