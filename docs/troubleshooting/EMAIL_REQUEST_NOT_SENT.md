# 🔍 Debug: Email Service Không Nhận Được Request

## 📋 Vấn Đề

Khi user đăng ký và gửi yêu cầu về backend, **microservice không nhận được yêu cầu sendmail nào**.

**Triệu chứng:**
- Backend nhận được request đăng ký
- User được tạo trong database
- Activation token được tạo
- **NHƯNG** email service không nhận được request gửi email

---

## 🔍 Các Nguyên Nhân Có Thể

### 1. Health Check Fail → Exception Trước Khi Gửi Request

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
WARN - Email service is unhealthy, performing health check before sending to: xxx@example.com
ERROR - Email service is down, cannot send activation link to: xxx@example.com
ERROR - RuntimeException when sending activation email to: xxx. Exception type: RuntimeException, Cause: null, Message: Email service is unavailable
```

**Giải pháp:**
- Kiểm tra email service có chạy không: `curl http://localhost:8200/health`
- Kiểm tra health check có pass không

---

### 2. Circuit Breaker Đã Mở → Fallback Được Gọi

**Flow:**
```
sendActivationLink()
  → Circuit breaker đã mở (OPEN state)
  → Fallback method được gọi ngay lập tức
  → KHÔNG gửi request đến email service
```

**Log sẽ có:**
```
ERROR - === CIRCUIT BREAKER ACTIVATED ===
ERROR - Circuit breaker activated for email service. Fallback triggered for email: xxx@example.com
```

**Giải pháp:**
- Đợi 30 giây (circuit breaker sẽ tự động đóng lại)
- Hoặc reset circuit breaker manually
- Kiểm tra email service có hoạt động không

---

### 3. Exception Xảy Ra Trước Khi Gửi Request

**Có thể xảy ra ở:**
- Health check fail
- API key null/empty
- Request body preparation fail
- Network issue trước khi gửi

**Log sẽ có:**
```
ERROR - Exception type: {type}, Cause: {cause}, Message: {message}
```

---

### 4. Code Không Đến Được Dòng Gửi Request

**Có thể do:**
- Transaction rollback trước khi gửi email
- Exception trong quá trình tạo activation token
- Code path không đúng

---

## 🛠️ Cách Debug

### Bước 1: Kiểm Tra Log "Sending activation link request"

**Nếu KHÔNG thấy log này:**
- Request không được gửi
- Code không đến được dòng gửi request

```bash
# Tìm log "Sending activation link request"
grep -i "Sending activation link request\|START sendActivationLink\|About to call emailVerificationService" logs/fix4home.log | tail -20
```

**Nếu thấy log này:**
- Request đã được gửi
- Vấn đề có thể là timeout hoặc email service không phản hồi

---

### Bước 2: Kiểm Tra Health Check

```bash
# Tìm log health check
grep -i "health check\|service healthy\|service is down" logs/fix4home.log | tail -20

# Test health check manually
curl http://localhost:8200/health
```

---

### Bước 3: Kiểm Tra Circuit Breaker

```bash
# Tìm log circuit breaker
grep -i "circuit breaker\|fallback triggered" logs/fix4home.log | tail -20
```

---

### Bước 4: Kiểm Tra Exception Trước Khi Gửi

```bash
# Tìm tất cả exception liên quan
grep -i "exception.*email\|runtimeexception.*email\|error.*email" logs/fix4home.log | tail -30
```

---

## 📊 Log Pattern Sau Khi Cập Nhật Code

Với code đã cập nhật, log sẽ hiển thị:

### Nếu Request Được Gửi:
```
INFO - === START sendActivationLink: email=xxx@example.com, action=registration, userId=123 ===
INFO - Email service URL: http://localhost:8200, Service healthy: true
INFO - Proceeding to send activation link - service is healthy
INFO - Sending activation link request to: http://localhost:8200/generate-activation for email: xxx@example.com, action: registration
INFO - Email service response status: 200 OK, body: {...}
```

### Nếu Health Check Fail:
```
INFO - === START sendActivationLink: email=xxx@example.com, action=registration, userId=123 ===
INFO - Email service URL: http://localhost:8200, Service healthy: false
WARN - Email service is unhealthy, performing health check before sending to: xxx@example.com
INFO - Health check result: false
ERROR - Email service is down, cannot send activation link to: xxx@example.com
ERROR - RuntimeException when sending activation email to: xxx. Exception type: RuntimeException, Cause: null, Message: Email service is unavailable
```

### Nếu Circuit Breaker Mở:
```
INFO - === START sendActivationLink: email=xxx@example.com, action=registration, userId=123 ===
ERROR - === CIRCUIT BREAKER ACTIVATED ===
ERROR - Circuit breaker activated for email service. Fallback triggered for email: xxx@example.com
ERROR - Exception that triggered fallback: type=RuntimeException, cause=ResourceAccessException, message=...
```

---

## 🔧 Giải Pháp

### Giải Pháp 1: Kiểm Tra Email Service

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

### Giải Pháp 2: Reset Circuit Breaker

Nếu circuit breaker đã mở:
- Đợi 30 giây (circuit breaker sẽ tự động đóng)
- Hoặc restart backend

### Giải Pháp 3: Enable Debug Logging

Thêm vào `application.properties`:

```properties
# Enable detailed logging
logging.level.com.fix4home.fix4home.service.EmailVerificationService=DEBUG
logging.level.com.fix4home.fix4home.service.ActivationTokenService=DEBUG
logging.level.org.springframework.web.client.RestTemplate=DEBUG
```

---

## 🎯 Checklist Debug

- [ ] Log có hiển thị "=== START sendActivationLink" không?
- [ ] Log có hiển thị "Sending activation link request" không?
- [ ] Health check có pass không?
- [ ] Circuit breaker có mở không?
- [ ] Email service có chạy không?
- [ ] API key có đúng không?
- [ ] Network có kết nối được không?

---

## 📝 Test Flow

1. **Enable debug logging** trong `application.properties`
2. **Restart backend**
3. **Test registration** với user mới
4. **Xem log** để trace flow:
   ```bash
   grep -i "START sendActivationLink\|Sending activation link\|health check\|circuit breaker" logs/fix4home.log | tail -30
   ```
5. **Xác định điểm dừng** trong flow
6. **Giải quyết vấn đề** dựa trên log

---

## 🔗 Related Documents

- `docs/troubleshooting/EMAIL_SERVICE_CONNECTION_TEST.md` - Test connection
- `docs/troubleshooting/EMAIL_ERROR_SPECIFIC_ANALYSIS.md` - Phân tích lỗi cụ thể
- `docs/api/EMAIL_SERVICE_ENDPOINTS.md` - Tổng hợp endpoints

