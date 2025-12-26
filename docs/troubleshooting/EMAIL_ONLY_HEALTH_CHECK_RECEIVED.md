# 🔍 Vấn Đề: Sendmail Chỉ Nhận Được Health Check, Không Nhận Request Gửi Email

## 📋 Mô Tả Vấn Đề

**Triệu chứng:**
- ✅ Sendmail microservice nhận được health check requests (`GET /health`)
- ❌ Sendmail **KHÔNG nhận được** requests gửi email (`POST /generate-activation`)
- Backend log hiển thị lỗi: `Failed to send activation email. Please try again later.`

## 🔍 Nguyên Nhân Có Thể

### 1. Circuit Breaker Đã Mở (OPEN State) ⚠️ **NGUYÊN NHÂN PHỔ BIẾN NHẤT**

**Flow:**
```
sendActivationLink()
  → @CircuitBreaker annotation intercept
  → Circuit breaker state = OPEN
  → Fallback method được gọi NGAY LẬP TỨC
  → KHÔNG gửi request đến email service
  → Email service không nhận được request
```

**Log sẽ có:**
```
INFO - === START sendActivationLink: email=xxx@example.com, action=registration, userId=123 ===
INFO - Circuit breaker state before call: OPEN, Failure rate: 100.0%
ERROR - ⚠️ CIRCUIT BREAKER IS OPEN - Request will be blocked and fallback will be called!
ERROR - ⚠️ This means email service requests are being blocked. Email service will NOT receive the request.
ERROR - === CIRCUIT BREAKER ACTIVATED ===
ERROR - Circuit breaker activated for email service. Fallback triggered for email: xxx@example.com
```

**Tại sao circuit breaker mở:**
- Email service đã fail nhiều lần trước đó
- Failure rate > 50% (threshold)
- Circuit breaker tự động mở để bảo vệ hệ thống

**Giải pháp:**
1. **Đợi 30 giây** - Circuit breaker sẽ tự động chuyển sang HALF_OPEN state
2. **Reset circuit breaker** - Restart backend
3. **Kiểm tra email service** - Đảm bảo email service hoạt động tốt

---

### 2. Health Check Fail → Exception Trước Khi Gửi

**Flow:**
```
sendActivationLink()
  → isServiceHealthy() = false
  → checkServiceHealth() = false
  → throw RuntimeException("Email service is unavailable")
  → KHÔNG đến được dòng gửi request
```

**Log sẽ có:**
```
INFO - === START sendActivationLink: email=xxx@example.com, action=registration, userId=123 ===
INFO - Email service URL: http://localhost:8200, Service healthy: false
WARN - Email service is unhealthy, performing health check before sending to: xxx@example.com
INFO - Health check result: false
ERROR - Email service is down, cannot send activation link to: xxx@example.com
ERROR - RuntimeException when sending activation email to: xxx. Exception type: RuntimeException, Cause: null, Message: Email service is unavailable
```

**Giải pháp:**
- Kiểm tra email service có chạy không: `curl http://localhost:8200/health`
- Kiểm tra network connection

---

### 3. Exception Xảy Ra Trước Khi Gửi Request

**Có thể do:**
- API key null/empty
- Request body preparation fail
- Network issue

**Log sẽ có:**
```
ERROR - Exception type: {type}, Cause: {cause}, Message: {message}
```

---

## 🛠️ Cách Debug

### Bước 1: Kiểm Tra Circuit Breaker State

Sau khi restart với code mới, log sẽ hiển thị:

```bash
# Tìm log circuit breaker
grep -i "circuit breaker state\|CIRCUIT BREAKER IS OPEN" logs/fix4home.log | tail -20
```

**Nếu thấy:**
```
INFO - Circuit breaker state before call: OPEN
ERROR - ⚠️ CIRCUIT BREAKER IS OPEN
```
→ **Đây là nguyên nhân!** Circuit breaker đã mở, request bị block.

### Bước 2: Kiểm Tra Log "Sending activation link request"

```bash
# Tìm log gửi request
grep -i "Sending activation link request\|START sendActivationLink" logs/fix4home.log | tail -20
```

**Nếu KHÔNG thấy log "Sending activation link request":**
- Request không được gửi
- Có thể do circuit breaker hoặc exception trước đó

**Nếu thấy log "Sending activation link request":**
- Request đã được gửi
- Vấn đề có thể là timeout hoặc email service không phản hồi

### Bước 3: Kiểm Tra Health Check

```bash
# Tìm log health check
grep -i "health check\|service healthy\|service is down" logs/fix4home.log | tail -20
```

---

## 🔧 Giải Pháp

### Giải Pháp 1: Reset Circuit Breaker

**Cách 1: Đợi tự động (30 giây)**
- Circuit breaker sẽ tự động chuyển sang HALF_OPEN sau 30 giây
- Sau đó sẽ thử lại request

**Cách 2: Restart Backend**
- Restart backend sẽ reset circuit breaker về CLOSED state

**Cách 3: Force Reset (nếu có endpoint)**
- Có thể tạo endpoint để reset circuit breaker manually (nếu cần)

### Giải Pháp 2: Kiểm Tra Email Service

```bash
# Test health check
curl http://localhost:8200/health

# Test send activation link
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

**Nếu test thành công:**
- Email service hoạt động tốt
- Vấn đề là circuit breaker đã mở từ lỗi trước đó

### Giải Pháp 3: Tạm Thời Disable Circuit Breaker (Development Only)

**⚠️ CHỈ DÙNG CHO DEVELOPMENT!**

Comment out `@CircuitBreaker` annotation:

```java
// @CircuitBreaker(name = "emailService", fallbackMethod = "sendActivationLinkFallback")
@Retry(name = "emailService")
public ActivationResponse sendActivationLink(String email, String action, Long userId) {
    // ...
}
```

**Lưu ý:** Không nên disable trong production!

---

## 📊 Circuit Breaker Configuration

File: `src/main/resources/application.properties`

```properties
# Circuit Breaker Configuration
resilience4j.circuitbreaker.instances.emailService.slidingWindowSize=10
resilience4j.circuitbreaker.instances.emailService.minimumNumberOfCalls=5
resilience4j.circuitbreaker.instances.emailService.failureRateThreshold=50
resilience4j.circuitbreaker.instances.emailService.waitDurationInOpenState=30s
```

**Giải thích:**
- `slidingWindowSize=10`: Xem xét 10 calls gần nhất
- `minimumNumberOfCalls=5`: Cần ít nhất 5 calls trước khi đánh giá
- `failureRateThreshold=50`: Mở circuit breaker nếu failure rate > 50%
- `waitDurationInOpenState=30s`: Đợi 30 giây trước khi thử lại (HALF_OPEN)

---

## 🎯 Log Pattern Sau Khi Cập Nhật

### Nếu Circuit Breaker Mở:
```
INFO - === START sendActivationLink: email=xxx@example.com, action=registration, userId=123 ===
INFO - Email service URL: http://localhost:8200, Service healthy: true
INFO - Circuit breaker state before call: OPEN, Failure rate: 100.0%, Successful calls: 0, Failed calls: 5
ERROR - ⚠️ CIRCUIT BREAKER IS OPEN - Request will be blocked and fallback will be called!
ERROR - ⚠️ This means email service requests are being blocked. Email service will NOT receive the request.
ERROR - === CIRCUIT BREAKER ACTIVATED ===
ERROR - Circuit breaker activated for email service. Fallback triggered for email: xxx@example.com
```

**→ Email service KHÔNG nhận được request vì circuit breaker block**

### Nếu Request Được Gửi:
```
INFO - === START sendActivationLink: email=xxx@example.com, action=registration, userId=123 ===
INFO - Circuit breaker state before call: CLOSED, Failure rate: 0.0%
INFO - Proceeding to send activation link - service is healthy
INFO - Sending activation link request to: http://localhost:8200/generate-activation for email: xxx@example.com, action: registration
INFO - Email service response status: 200 OK, body: {...}
```

**→ Email service NHẬN ĐƯỢC request**

---

## 🔍 Tại Sao Health Check Vẫn Hoạt Động?

**Health check KHÔNG qua circuit breaker:**
- Health check gọi trực tiếp `restTemplate.getForEntity()` 
- **KHÔNG có** `@CircuitBreaker` annotation
- Nên health check vẫn hoạt động ngay cả khi circuit breaker mở

**Gửi email CÓ circuit breaker:**
- Method `sendActivationLink()` có `@CircuitBreaker` annotation
- Khi circuit breaker mở → fallback được gọi → không gửi request

---

## 🎯 Next Steps

1. **Restart backend** với code đã cập nhật
2. **Test registration** và xem log:
   ```bash
   grep -i "circuit breaker state\|CIRCUIT BREAKER IS OPEN\|Sending activation link request" logs/fix4home.log | tail -30
   ```
3. **Xác định nguyên nhân:**
   - Nếu circuit breaker OPEN → Đợi 30 giây hoặc restart
   - Nếu health check fail → Kiểm tra email service
   - Nếu có exception → Xem log chi tiết

---

## 📝 Summary

**Vấn đề:** Sendmail chỉ nhận health check, không nhận request gửi email

**Nguyên nhân phổ biến:** Circuit breaker đã mở (OPEN state)

**Giải pháp:**
1. Đợi 30 giây (circuit breaker tự động đóng)
2. Restart backend (reset circuit breaker)
3. Kiểm tra email service hoạt động tốt

**Log sẽ hiển thị:**
- Circuit breaker state trước khi gửi request
- Cảnh báo nếu circuit breaker mở
- Chi tiết fallback nếu được gọi

---

## 🔗 Related Documents

- `docs/troubleshooting/EMAIL_REQUEST_NOT_SENT.md` - Debug request không được gửi
- `docs/troubleshooting/EMAIL_ERROR_SPECIFIC_ANALYSIS.md` - Phân tích lỗi cụ thể
- `docs/api/EMAIL_SERVICE_ENDPOINTS.md` - Tổng hợp endpoints

