# 🔍 Phân Tích Cụ Thể: Lỗi "Failed to send activation email. Please try again later."

## 📋 Lỗi Trong Log

```
WARN [ExceptionHandlerExceptionResolver] - Resolved [BusinessValidationException: Business validation failed: Failed to send activation email. Please try again later.]
```

## 🔍 Phân Tích Exception Flow

### Exception Chain:

1. **EmailVerificationService.sendActivationLink()** 
   - Gọi `POST http://localhost:8200/generate-activation`
   - Có thể throw: `RuntimeException` với các nguyên nhân:
     - `ResourceAccessException` (Connection timeout, connection refused)
     - `HttpServerErrorException` (500, 502, 503)
     - `HttpClientErrorException` (400, 401, 403, 429)

2. **ActivationTokenService.generateActivationTokenInternal()**
   - Catch exception từ `EmailVerificationService`
   - Wrap thành `BusinessValidationException`
   - Log: `RuntimeException when sending activation email to: {email}. Exception type: {type}, Cause: {cause}, Message: {message}`

3. **AuthService.register()**
   - Catch `BusinessValidationException` từ `ActivationTokenService`
   - Re-throw hoặc wrap lại
   - Log: `BusinessValidationException when sending verification email to: {email}. Message: {message}`
   - **Nếu catch `Exception`:** Wrap thành `BusinessValidationException: Failed to send activation email. Please try again later.`

4. **ExceptionHandlerExceptionResolver**
   - Catch và resolve exception
   - Log: `WARN - Resolved [BusinessValidationException: ...]`

## 🎯 Cách Tìm Exception Gốc

### Bước 1: Tìm Log Trước Đó

Log exception gốc sẽ xuất hiện **TRƯỚC** dòng 1001. Tìm trong log:

```bash
# Tìm log ERROR trước dòng 1001
grep -B 50 "Failed to send activation email. Please try again later" logs/fix4home.log | grep -i "error\|exception\|runtimeexception\|resourceaccess"

# Hoặc tìm theo timestamp (10:29:27)
grep "2025-12-26 10:29:" logs/fix4home.log | grep -i "error\|exception"
```

### Bước 2: Tìm Log Chi Tiết

Với code đã cập nhật, log sẽ hiển thị:

```
ERROR - RuntimeException when sending activation email to: xxx@example.com. 
        Exception type: RuntimeException, 
        Cause: SocketTimeoutException, 
        Message: Email service unavailable. Cannot connect to http://localhost:8200
```

Hoặc:

```
ERROR - BusinessValidationException when sending activation email to: xxx@example.com. 
        Message: Failed to send activation email: Email service is temporarily unavailable
```

### Bước 3: Tìm Log Từ EmailVerificationService

```bash
# Tìm log từ EmailVerificationService
grep -i "sending activation link request\|email service response\|cannot connect\|resourceaccess\|sockettimeout" logs/fix4home.log | tail -20
```

## 🔍 Các Nguyên Nhân Cụ Thể

### 1. Connection Refused (Email Service Không Chạy)

**Exception gốc:**
```
ResourceAccessException: Cannot connect to email service at http://localhost:8200
Cause: ConnectException: Connection refused
```

**Log sẽ có:**
```
ERROR - Cannot connect to email service at http://localhost:8200 for email: xxx. Error: Connection refused
ERROR - ResourceAccessException details - Cause: ConnectException, Message: Connection refused
ERROR - RuntimeException when sending activation email to: xxx. Exception type: RuntimeException, Cause: ConnectException, Message: Email service unavailable. Cannot connect to http://localhost:8200
```

**Giải pháp:**
```bash
# Kiểm tra email service có chạy không
curl http://localhost:8200/health

# Nếu không có response → Khởi động email service
```

---

### 2. Timeout (Email Service Xử Lý Chậm)

**Exception gốc:**
```
ResourceAccessException: Cannot connect to email service at http://localhost:8200
Cause: SocketTimeoutException: Read timed out
```

**Log sẽ có:**
```
ERROR - Cannot connect to email service at http://localhost:8200 for email: xxx. Error: Read timed out
ERROR - ResourceAccessException details - Cause: SocketTimeoutException, Message: Read timed out
ERROR - RuntimeException when sending activation email to: xxx. Exception type: RuntimeException, Cause: SocketTimeoutException, Message: Email service unavailable. Cannot connect to http://localhost:8200
```

**Giải pháp:**
- Tăng timeout trong `RestTemplateConfig.java` (từ 15s → 30s)
- Hoặc optimize email service để xử lý nhanh hơn

---

### 3. Email Service Trả Về Lỗi (500, 502, 503)

**Exception gốc:**
```
HttpServerErrorException: 500 INTERNAL_SERVER_ERROR
```

**Log sẽ có:**
```
ERROR - Server error when sending activation link to xxx: 500 INTERNAL_SERVER_ERROR - {...}
ERROR - RuntimeException when sending activation email to: xxx. Exception type: RuntimeException, Cause: HttpServerErrorException, Message: Email service error: 500 INTERNAL_SERVER_ERROR
```

**Giải pháp:**
- Kiểm tra log của email service
- Kiểm tra database của email service
- Restart email service

---

### 4. Unauthorized (API Key Không Đúng)

**Exception gốc:**
```
HttpClientErrorException: 401 Unauthorized
```

**Log sẽ có:**
```
ERROR - Client error when sending activation link to xxx: 401 UNAUTHORIZED - {...}
ERROR - RuntimeException when sending activation email to: xxx. Exception type: RuntimeException, Cause: HttpClientErrorException, Message: Email service error: 401 UNAUTHORIZED
```

**Giải pháp:**
- Kiểm tra API key trong `application.properties`
- Kiểm tra API key có đúng với email service không

---

### 5. Rate Limit (429 Too Many Requests)

**Exception gốc:**
```
HttpClientErrorException: 429 Too Many Requests
```

**Log sẽ có:**
```
ERROR - Client error when sending activation link to xxx: 429 TOO_MANY_REQUESTS - {...}
```

**Response từ email service:**
```json
{
  "success": false,
  "message": "Too many requests. Please wait 60 seconds before resending.",
  "can_resend": true,
  "next_resend_at": 1703568000,
  "send_count": 3,
  "max_sends": 3
}
```

**Giải pháp:**
- Đợi một lúc rồi thử lại
- Kiểm tra `next_resend_at` trong response

---

### 6. Circuit Breaker Mở

**Exception gốc:**
```
RuntimeException: Email service is temporarily unavailable. Please try again later.
```

**Log sẽ có:**
```
ERROR - Circuit breaker activated for email service. Fallback triggered for email: xxx
ERROR - RuntimeException when sending activation email to: xxx. Exception type: RuntimeException, Cause: null, Message: Email service is temporarily unavailable. Please try again later.
```

**Giải pháp:**
- Đợi 30 giây (circuit breaker sẽ tự động đóng lại)
- Kiểm tra email service có hoạt động không

---

## 🛠️ Cách Debug Cụ Thể

### 1. Xem Log File Trực Tiếp

```bash
# Xem log xung quanh dòng 1001
sed -n '950,1003p' logs/fix4home.log

# Hoặc xem log theo timestamp
grep "2025-12-26 10:29:" logs/fix4home.log
```

### 2. Tìm Tất Cả Exception Liên Quan

```bash
# Tìm tất cả exception về email trong khoảng thời gian đó
grep "2025-12-26 10:29:" logs/fix4home.log | grep -i "error\|exception\|email" | head -30
```

### 3. Enable Debug Logging

Thêm vào `application.properties`:

```properties
# Enable detailed HTTP client logging
logging.level.org.springframework.web.client.RestTemplate=DEBUG
logging.level.org.apache.http=DEBUG
logging.level.com.fix4home.fix4home.service.EmailVerificationService=DEBUG
logging.level.com.fix4home.fix4home.service.ActivationTokenService=DEBUG
```

Sau đó restart và test lại, log sẽ hiển thị chi tiết hơn.

---

## 📊 Summary: Exception Flow Diagram

```
EmailVerificationService.sendActivationLink()
    ↓
[Exception xảy ra: ResourceAccessException/HttpServerErrorException/etc.]
    ↓
ActivationTokenService.generateActivationTokenInternal()
    ↓ catch RuntimeException
[Log: RuntimeException when sending activation email...]
    ↓ wrap
BusinessValidationException: Failed to send activation email: {message}
    ↓
AuthService.register()
    ↓ catch BusinessValidationException
[Log: BusinessValidationException when sending verification email...]
    ↓ re-throw
BusinessValidationException: Failed to send activation email. Please try again later.
    ↓
ExceptionHandlerExceptionResolver
    ↓
[Log: WARN - Resolved [BusinessValidationException: ...]]
```

---

## 🎯 Next Steps

1. **Xem log file trực tiếp** để tìm exception gốc:
   ```bash
   sed -n '950,1003p' logs/fix4home.log
   ```

2. **Tìm log ERROR trước dòng 1001:**
   ```bash
   grep -B 50 "Failed to send activation email. Please try again later" logs/fix4home.log | grep ERROR
   ```

3. **Enable debug logging** và test lại để có log chi tiết hơn

4. **Test email service trực tiếp:**
   ```bash
   curl -X POST http://localhost:8200/generate-activation \
     -H "Content-Type: application/json" \
     -H "x-api-key: fix4home_prod_123abc456def789" \
     -d '{"email":"test@example.com","action":"registration","system":"Fix4Home","customData":{"user_id":"123","action":"registration"}}'
   ```

---

## 🔗 Related Documents

- `docs/troubleshooting/EMAIL_SERVICE_ERROR_TROUBLESHOOTING.md` - Troubleshooting chung
- `docs/troubleshooting/EMAIL_EXCEPTION_DEBUG.md` - Debug exceptions
- `docs/troubleshooting/EMAIL_SERVICE_CONNECTION_TEST.md` - Test connection
- `docs/api/EMAIL_SERVICE_ENDPOINTS.md` - Tổng hợp endpoints

