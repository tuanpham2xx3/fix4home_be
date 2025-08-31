# CẬP NHẬT CODEBASE CẦN BỔ SUNG

## TỔNG QUAN PHÂN TÍCH
Sau khi so sánh codebase hiện tại với yêu cầu trong `api_v0.4.md` và `flow_v0.4.md`, có **7 tính năng chính bị thiếu** cần bổ sung để đáp ứng đầy đủ yêu cầu nghiệp vụ.

---

## 1. HỆ THỐNG ĐĂNG TIN DỊCH VỤ (SERVICE POSTS)

### Vấn đề hiện tại:
- Hệ thống chỉ có booking trực tiếp (ServiceRequest)
- Thiếu cơ chế đăng tin để thợ tự chọn việc

### Cần bổ sung:

#### A. Entity mới:
```java
// ServicePost.java
@Entity
public class ServicePost {
    private Long id;
    private Long customerId;
    private String title;
    private String description;
    private String address;
    private BigDecimal estimatedBudget;
    private LocalDateTime preferredTime;
    private ServicePostType type; // URGENT, CONSULTATION
    private ServicePostStatus status; // POSTED, ASSIGNED, COMPLETED, CANCELLED
    private Long serviceId;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Integer maxTechnicians; // Số thợ tối đa có thể nhận
}

// ServicePostType.java (enum)
URGENT,      // Cần gấp
CONSULTATION // Tư vấn trước

// ServicePostStatus.java (enum)  
POSTED,      // Đã đăng
IN_PROGRESS, // Đang thực hiện
ASSIGNED,    // Đã giao thợ
COMPLETED,   // Hoàn thành
CANCELLED    // Đã hủy
```

#### B. Controller mới:
```java
@RestController
@RequestMapping("/api/v1/service-posts")
public class ServicePostController {
    // POST /api/v1/service-posts - Khách hàng đăng tin
    // GET /api/v1/service-posts - Thợ xem danh sách tin
    // GET /api/v1/service-posts/{id} - Chi tiết tin
    // PUT /api/v1/service-posts/{id}/assign - Thợ nhận tin
    // PUT /api/v1/service-posts/{id}/status - Cập nhật trạng thái
}
```

---

## 2. HỆ THỐNG TƯ VẤN VÀ BÁO GIÁ

### Vấn đề hiện tại:
- Thiếu hoàn toàn workflow tư vấn và báo giá

### Cần bổ sung:

#### A. Entity mới:
```java
// Consultation.java
@Entity
public class Consultation {
    private Long id;
    private Long servicePostId;
    private Long technicianId;
    private String proposal;           // Đề xuất của thợ
    private BigDecimal quotedPrice;    // Giá báo
    private String notes;              // Ghi chú
    private ConsultationStatus status; // PENDING, ACCEPTED, REJECTED
    private LocalDateTime submittedAt;
    private LocalDateTime respondedAt;
}

// ConsultationStatus.java (enum)
PENDING,   // Chờ phản hồi
ACCEPTED,  // Được chấp nhận  
REJECTED   // Bị từ chối
```

#### B. Endpoints mới cần thêm:
```java
// POST /api/v1/consultations - Thợ gửi tư vấn
// GET /api/v1/consultations/my-proposals - Thợ xem đề xuất của mình
// GET /api/v1/consultations/post/{postId} - Khách hàng xem tư vấn cho tin
// PUT /api/v1/consultations/{id}/accept - Khách hàng chấp nhận
// PUT /api/v1/consultations/{id}/reject - Khách hàng từ chối
```

---

## 3. TRẠNG THÁI ONLINE/OFFLINE VÀ VỊ TRÍ

### Vấn đề hiện tại:
- Không theo dõi trạng thái hoạt động của thợ
- Thiếu thông tin vị trí để matching

### Cần cập nhật:

#### A. Mở rộng TechnicianProfile:
```java
// Thêm vào TechnicianProfile.java
private Boolean isOnline = false;
private LocalDateTime lastSeenAt;
private Double currentLatitude;
private Double currentLongitude;
private String currentAddress;
private Integer workingRadius; // Bán kính làm việc (km)
```

#### B. Endpoints mới:
```java
// PUT /api/v1/technicians/status - Cập nhật online/offline
// PUT /api/v1/technicians/location - Cập nhật vị trí
// GET /api/v1/technicians/nearby - Tìm thợ gần
```

---

## 4. HỆ THỐNG KHIẾU NẠI (COMPLAINT)

### Vấn đề hiện tại:
- Không có workflow xử lý tranh chấp
- Thiếu trạng thái COMPLAINING/COMPLAITED

### Cần bổ sung:

#### A. Entity mới:
```java
// Complaint.java
@Entity
public class Complaint {
    private Long id;
    private Long serviceRequestId;
    private Long complainantId;    // Người khiếu nại
    private Long accusedId;        // Người bị khiếu nại
    private String reason;         // Lý do khiếu nại
    private String description;    // Mô tả chi tiết
    private ComplaintStatus status;
    private String adminResponse;  // Phản hồi từ admin
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}

// ComplaintStatus.java (enum)
PENDING,    // Chờ xử lý
INVESTIGATING, // Đang điều tra
RESOLVED,   // Đã giải quyết
REJECTED    // Từ chối
```

#### B. Cập nhật ServiceRequestStatus:
```java
// Thêm vào enum ServiceRequestStatus
COMPLAINING,  // Đang khiếu nại
COMPLAITED   // Đã khiếu nại xong
```

---

## 5. HỆ THỐNG CHAT/TIN NHẮN

### Vấn đề hiện tại:
- Thiếu hoàn toàn tính năng nhắn tin real-time

### Cần bổ sung:

#### A. Entities mới:
```java
// Conversation.java
@Entity
public class Conversation {
    private Long id;
    private Long serviceRequestId;
    private Long customerId;
    private Long technicianId;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private Boolean isActive = true;
}

// Message.java  
@Entity
public class Message {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String content;
    private MessageType type; // TEXT, IMAGE, LOCATION
    private Boolean isRead = false;
    private LocalDateTime sentAt;
}
```

#### B. WebSocket Controller:
```java
@Controller
public class ChatController {
    @MessageMapping("/chat.send")
    @SendTo("/topic/conversation/{conversationId}")
    public MessageDTO sendMessage(SendMessageRequest request);
    
    @MessageMapping("/chat.typing")
    @SendTo("/topic/conversation/{conversationId}/typing")
    public TypingIndicatorDTO typing(TypingRequest request);
}
```

---

## 6. QUY TRÌNH PHÊ DUYỆT THỢ

### Vấn đề hiện tại:
- Thiếu workflow phê duyệt thợ mới
- User được tạo trực tiếp với role TECHNICIAN

### Cần cập nhật:

#### A. Mở rộng UserStatus:
```java
// Thêm vào enum UserStatus
PENDING_APPROVAL,  // Chờ phê duyệt
REJECTED          // Bị từ chối
```

#### B. Cập nhật TechnicianProfile:
```java
// Thêm fields
private String verificationDocuments; // Link tài liệu xác minh
private String rejectionReason;       // Lý do từ chối
private LocalDateTime approvedAt;
private Long approvedBy;              // Admin phê duyệt
```

---

## 7. TÌM KIẾM NÂNG CAO

### Vấn đề hiện tại:
- Chỉ có tìm kiếm cơ bản
- Thiếu filter theo địa điểm, giá, rating

### Cần bổ sung:

#### A. Search DTOs:
```java
// ServiceSearchRequest.java
public class ServiceSearchRequest {
    private String keyword;
    private String location;
    private Double latitude;
    private Double longitude;
    private Integer radius;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double minRating;
    private List<Long> skillIds;
    private Boolean availableNow;
}
```

#### B. Elasticsearch Integration (tuỳ chọn):
```xml
<!-- Thêm vào pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

---

## KẾ HOẠCH TRIỂN KHAI

### Giai đoạn 1 - Cốt lõi (2-3 tuần):
1. ✅ Service Posts System
2. ✅ Consultation & Quotation  
3. ✅ Complaint System

### Giai đoạn 2 - Quan trọng (2-3 tuần):
4. ✅ Online/Offline Status
5. ✅ Technician Approval Process
6. ✅ Advanced Search

### Giai đoạn 3 - Bổ sung (1-2 tuần):
7. ✅ Chat/Messaging System

---

## LƯU Ý QUAN TRỌNG

### Database Migration:
```sql
-- Cần tạo các bảng mới
CREATE TABLE service_posts (...);
CREATE TABLE consultations (...);
CREATE TABLE complaints (...);
CREATE TABLE conversations (...);
CREATE TABLE messages (...);

-- Cập nhật bảng hiện có
ALTER TABLE technician_profiles ADD COLUMN is_online BOOLEAN DEFAULT FALSE;
ALTER TABLE technician_profiles ADD COLUMN current_latitude DOUBLE;
-- ... thêm các cột khác
```

### Security Updates:
```java
// Cập nhật SecurityConfig.java
.requestMatchers("/api/v1/service-posts/**").hasAnyRole("CUSTOMER", "TECHNICIAN")
.requestMatchers("/api/v1/consultations/**").hasAnyRole("CUSTOMER", "TECHNICIAN")  
.requestMatchers("/api/v1/complaints/**").hasAnyRole("CUSTOMER", "TECHNICIAN", "ADMIN")
.requestMatchers("/chat/**").hasAnyRole("CUSTOMER", "TECHNICIAN")
```

### Frontend Updates Needed:
- Real-time WebSocket connection cho chat
- Geolocation API cho tracking vị trí
- Push notifications cho tin nhắn mới
- UI cho service posts và consultation workflow
- Admin panel cho complaint management

---

## TỔNG KẾT

Codebase hiện tại đã có **nền tảng vững chắc** với:
- ✅ Authentication & Authorization
- ✅ User Management  
- ✅ Service Management
- ✅ Payment Integration
- ✅ Basic ServiceRequest workflow

Cần bổ sung **7 tính năng chính** để đáp ứng đầy đủ yêu cầu nghiệp vụ của nền tảng Fix4Home. Ưu tiên triển khai theo 3 giai đoạn để đảm bảo tính ổn định và khả năng mở rộng.


