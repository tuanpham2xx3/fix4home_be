# 🔧 Troubleshooting: Lỗi Gửi Activation Email

## 📋 Mô Tả Lỗi

**Lỗi trong log:**
```
WARN [o.s.w.s.m.m.a.ExceptionHandlerExceptionResolver] - Resolved [com.fix4home.fix4home.exception.BusinessValidationException: Business validation failed: Failed to send activation email. Please try again later.]
```

**Khi nào xảy ra:**
- Khi user đăng ký tài khoản mới
- Hệ thống cố gắng gửi email kích hoạt tài khoản
- Email service không thể gửi email thành công

---

## 🔍 Nguyên Nhân Có Thể

### 1. Email Service Không Chạy

**Triệu chứng:**
- Lỗi `ResourceAccessException` hoặc `ConnectException`
- Log: "Cannot connect to email service at http://localhost:8200"

**Kiểm tra:**
```bash
# Kiểm tra email service có đang chạy không
curl http://localhost:8200/health
# hoặc
netstat -an | grep 8200
```

**Giải pháp:**
- Khởi động email service (N8N hoặc email microservice)
- Kiểm tra port 8200 có bị chiếm bởi service khác không
- Kiểm tra firewall/network rules

---

### 2. Email Service URL Không Đúng

**Triệu chứng:**
- Lỗi connection timeout
- Log: "Cannot connect to email service"

**Kiểm tra:**
File `application.properties`:
```properties
email.verification.service.url=http://localhost:8200
```

**Giải pháp:**
- Kiểm tra URL trong `application.properties` có đúng không
- Nếu email service chạy trên server khác, cập nhật URL
- Kiểm tra environment variable `EMAIL_VERIFICATION_SERVICE_URL` (nếu có)

---

### 3. API Key Không Đúng

**Triệu chứng:**
- Lỗi `401 Unauthorized` hoặc `403 Forbidden`
- Log: "Client error when sending activation link"

**Kiểm tra:**
File `application.properties`:
```properties
email.verification.service.api-key=${EMAIL_VERIFICATION_API_KEY:fix4home_prod_123abc456def789}
```

**Giải pháp:**
- Kiểm tra API key trong `application.properties` có khớp với email service không
- Kiểm tra environment variable `EMAIL_VERIFICATION_API_KEY`
- Liên hệ admin để lấy API key đúng

---

### 4. Email Service Trả Về Lỗi

**Triệu chứng:**
- Lỗi `HttpServerErrorException` (500, 502, 503, etc.)
- Log: "Server error when sending activation link"

**Nguyên nhân:**
- Email service đang gặp lỗi nội bộ
- Email service quá tải
- Database của email service có vấn đề

**Giải pháp:**
- Kiểm tra log của email service
- Kiểm tra health check của email service: `GET http://localhost:8200/health`
- Restart email service nếu cần

---

### 5. Rate Limiting

**Triệu chứng:**
- Lỗi `429 Too Many Requests`
- Log: "Too many activation emails sent"

**Nguyên nhân:**
- Gửi quá nhiều email trong thời gian ngắn
- Vượt quá giới hạn rate limit

**Giải pháp:**
- Đợi một lúc rồi thử lại
- Kiểm tra cấu hình rate limit trong email service
- Giảm số lần thử gửi email

---

### 6. Network/Firewall Issue

**Triệu chứng:**
- Connection timeout
- Lỗi `SocketTimeoutException`

**Giải pháp:**
- Kiểm tra network connection giữa backend và email service
- Kiểm tra firewall rules
- Kiểm tra proxy settings (nếu có)

---

## 🛠️ Cách Khắc Phục

### Bước 1: Kiểm Tra Email Service

```bash
# Kiểm tra email service có chạy không
# Lưu ý: Health check không cần API key
curl -X GET http://localhost:8200/health

# Nếu không có response, email service không chạy
```

### Bước 2: Kiểm Tra Configuration

File `src/main/resources/application.properties`:
```properties
# Email Verification Service Configuration
email.verification.service.url=http://localhost:8200
email.verification.service.api-key=${EMAIL_VERIFICATION_API_KEY:fix4home_prod_123abc456def789}
email.activation.frontend.base-url=${FRONTEND_BASE_URL:https://fe.iceteadev.site}
```

### Bước 3: Kiểm Tra Logs

Xem log chi tiết trong `logs/fix4home.log`:
```bash
# Tìm lỗi liên quan đến email
grep -i "email\|activation" logs/fix4home.log | tail -50

# Tìm lỗi connection
grep -i "cannot connect\|connection\|timeout" logs/fix4home.log | tail -50
```

### Bước 4: Test Email Service Manually

```bash
# Test gửi activation email
# Lưu ý: baseUrl không cần gửi nữa - email service tự động dùng FRONTEND_URL từ config
curl -X POST http://localhost:8200/generate-activation \
  -H "Content-Type: application/json" \
  -H "x-api-key: fix4home_prod_123abc456def789" \
  -d '{
    "email": "test@example.com",
    "action": "registration",
    "system": "Fix4Home",
    "customData": {
      "user_id": "123",
      "action": "registration"
    }
  }'
```

---

## 🔄 Circuit Breaker & Retry

Hệ thống có cơ chế **Circuit Breaker** và **Retry**:

### Circuit Breaker
- Tự động mở khi email service fail nhiều lần
- Sau 30 giây sẽ thử lại (half-open state)
- Fallback method sẽ được gọi khi circuit breaker mở

### Retry
- Tự động retry 3 lần khi gặp lỗi connection
- Có exponential backoff (1s, 2s, 4s)

**Cấu hình trong `application.properties`:**
```properties
# Circuit Breaker
resilience4j.circuitbreaker.instances.emailService.slidingWindowSize=10
resilience4j.circuitbreaker.instances.emailService.failureRateThreshold=50
resilience4j.circuitbreaker.instances.emailService.waitDurationInOpenState=30s

# Retry
resilience4j.retry.instances.emailService.maxAttempts=3
resilience4j.retry.instances.emailService.waitDuration=1s
```

---

## 📊 Health Check

Hệ thống có health check tự động cho email service:

```java
// EmailServiceHealthMonitor chạy định kỳ
// Kiểm tra health mỗi 30 giây
```

**Kiểm tra health status:**
- Xem log: `Email service health check: HEALTHY` hoặc `UNHEALTHY`
- Nếu UNHEALTHY, email service sẽ không được sử dụng

---

## ⚠️ Lưu Ý Quan Trọng

1. **User Registration Fails**: Khi email service không hoạt động, user registration sẽ **FAIL** (theo thiết kế hiện tại)
   - User sẽ không được tạo trong database
   - User cần thử lại sau khi email service hoạt động

2. **Notification Vẫn Được Tạo**: Mặc dù registration fail, nhưng notification có thể đã được tạo (nếu user được save trước khi gửi email fail)

3. **Transaction Rollback**: Nếu email gửi fail, toàn bộ registration sẽ rollback (user không được tạo)

---

## 🎯 Giải Pháp Tạm Thời (Development)

Nếu đang development và không cần email service:

### Option 1: Disable Email Verification (Temporary)

Trong `AuthService.java`, comment out phần gửi email:
```java
// Send email verification for non-admin users
if (savedUser.getStatus() == UserStatus.PENDING_EMAIL_VERIFICATION) {
    // Temporarily disabled for development
    // try {
    //     ActivationTokenService.ActivationTokenResponse response = 
    //         activationTokenService.generateActivationToken(savedUser, "registration");
    //     ...
    // }
    log.warn("Email verification disabled for development");
}
```

**⚠️ CẢNH BÁO:** Chỉ dùng cho development, không dùng cho production!

### Option 2: Mock Email Service

Tạo một mock email service endpoint để test:
```java
@RestController
@RequestMapping("/mock-email")
public class MockEmailController {
    @PostMapping("/generate-activation")
    public ResponseEntity<Map> mockActivation(@RequestBody Map request) {
        // Always return success for testing
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Mock email sent");
        return ResponseEntity.ok(response);
    }
}
```

---

## 📞 Liên Hệ Support

Nếu vẫn gặp vấn đề sau khi thử các bước trên:

1. **Thu thập thông tin:**
   - Log file (`logs/fix4home.log`)
   - Configuration (`application.properties`)
   - Email service logs (nếu có)
   - Network connectivity test results

2. **Kiểm tra:**
   - Email service có đang chạy không?
   - API key có đúng không?
   - Network có kết nối được không?
   - Email service có trả về lỗi gì không?

3. **Liên hệ:**
   - Backend team để kiểm tra code
   - DevOps team để kiểm tra infrastructure
   - Email service team để kiểm tra email service

---

## 📚 Tham Khảo

- Email Service Configuration: `src/main/resources/application.properties`
- EmailVerificationService: `src/main/java/com/fix4home/fix4home/service/EmailVerificationService.java`
- ActivationTokenService: `src/main/java/com/fix4home/fix4home/service/ActivationTokenService.java`
- AuthService: `src/main/java/com/fix4home/fix4home/service/AuthService.java`

