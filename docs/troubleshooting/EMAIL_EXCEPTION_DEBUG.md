# 🔍 Debug: Email Exception Wrapped Issue

## 📋 Vấn Đề

Từ log (dòng 796-1013), ta thấy:
- ❌ **Exception bị wrap thành `BusinessValidationException`**
- ❌ **Exception gốc không được log đầy đủ**
- ❌ **Stack trace dài nhưng không thấy exception gốc ở đầu**

**Log hiện tại:**
```
WARN [ExceptionHandlerExceptionResolver] - Resolved [BusinessValidationException: Failed to send activation email. Please try again later.]
```

## 🔍 Phân Tích

### Exception Flow:

1. **EmailVerificationService.sendActivationLink()** 
   - Có thể throw: `RuntimeException` (ResourceAccessException, HttpServerErrorException, etc.)

2. **ActivationTokenService.generateActivationTokenInternal()**
   - Catch exception và wrap thành `BusinessValidationException`
   - **Vấn đề:** Exception gốc không được log đầy đủ

3. **AuthService.register()**
   - Catch và wrap lại thành `BusinessValidationException`
   - **Vấn đề:** Exception gốc bị mất

### Các Exception Có Thể Xảy Ra:

1. **ResourceAccessException** (Connection timeout, connection refused)
   - `SocketTimeoutException` - Request timeout > 15 giây
   - `ConnectException` - Cannot connect to email service

2. **HttpServerErrorException** (500, 502, 503, etc.)
   - Email service trả về lỗi server

3. **HttpClientErrorException** (400, 401, 403, 429, etc.)
   - Bad request, unauthorized, rate limit, etc.

## 🛠️ Giải Pháp Đã Áp Dụng

### 1. Cải Thiện Logging trong ActivationTokenService

**Trước:**
```java
catch (Exception e) {
    log.error("Exception sending activation email to: {}. Error: {}", email, e.getMessage(), e);
    throw new BusinessValidationException("Failed to send activation email: " + e.getMessage());
}
```

**Sau:**
```java
catch (RuntimeException e) {
    log.error("RuntimeException when sending activation email to: {}. Exception type: {}, Cause: {}, Message: {}", 
             email, 
             e.getClass().getSimpleName(),
             e.getCause() != null ? e.getCause().getClass().getSimpleName() : "null",
             e.getMessage(), 
             e);
    throw new BusinessValidationException("Failed to send activation email: " + e.getMessage());
}
catch (Exception e) {
    log.error("Unexpected exception when sending activation email to: {}. Exception type: {}, Cause: {}, Message: {}", 
             email,
             e.getClass().getSimpleName(),
             e.getCause() != null ? e.getCause().getClass().getSimpleName() : "null",
             e.getMessage(), 
             e);
    throw new BusinessValidationException("Failed to send activation email: " + e.getMessage());
}
```

### 2. Cải Thiện Logging trong AuthService

**Trước:**
```java
catch (Exception e) {
    log.error("Exception sending verification email to: {}", savedUser.getEmail(), e);
    throw new BusinessValidationException("Failed to send activation email. Please try again later.");
}
```

**Sau:**
```java
catch (Exception e) {
    log.error("Exception sending verification email to: {}. Exception type: {}, Cause: {}, Message: {}", 
             savedUser.getEmail(),
             e.getClass().getSimpleName(),
             e.getCause() != null ? e.getCause().getClass().getSimpleName() : "null",
             e.getMessage(), 
             e);
    throw new BusinessValidationException("Failed to send activation email. Please try again later.");
}
```

## 📊 Log Pattern Sau Khi Cập Nhật

Sau khi restart với code mới, log sẽ hiển thị:

### Nếu là ResourceAccessException (Timeout):
```
ERROR - RuntimeException when sending activation email to: xxx@example.com. Exception type: RuntimeException, Cause: SocketTimeoutException, Message: Email service unavailable. Cannot connect to http://localhost:8200
ERROR - ResourceAccessException details - Cause: SocketTimeoutException, Message: Read timed out
```

### Nếu là HttpServerErrorException:
```
ERROR - RuntimeException when sending activation email to: xxx@example.com. Exception type: RuntimeException, Cause: HttpServerErrorException, Message: Email service error: 500 INTERNAL_SERVER_ERROR
ERROR - Server error when sending activation link to xxx@example.com: 500 INTERNAL_SERVER_ERROR - {...}
```

### Nếu là HttpClientErrorException:
```
ERROR - Failed to send activation email to: xxx@example.com. Response message: Rate limit exceeded
```

## 🔧 Cách Debug

### 1. Tìm Exception Gốc trong Log

```bash
# Tìm tất cả exception liên quan đến email
grep -i "exception.*email\|runtimeexception.*email\|resourceaccess\|sockettimeout" logs/fix4home.log | tail -50

# Tìm exception type và cause
grep -i "Exception type:\|Cause:" logs/fix4home.log | tail -20
```

### 2. Kiểm Tra Email Service

```bash
# Test connection
curl -v http://localhost:8200/health

# Test với timeout
timeout 20 curl -X POST http://localhost:8200/generate-activation \
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

### 3. Kiểm Tra Timeout Configuration

File: `src/main/java/com/fix4home/fix4home/config/RestTemplateConfig.java`
```java
.setResponseTimeout(Timeout.ofSeconds(15))  // 15 giây
```

Nếu email service xử lý > 15 giây → sẽ timeout

## 🎯 Next Steps

1. **Restart backend** với code đã cập nhật
2. **Test lại registration** và xem log chi tiết
3. **Tìm exception gốc** trong log:
   ```bash
   grep -A 10 "Exception type:" logs/fix4home.log | tail -30
   ```
4. **Xác định nguyên nhân:**
   - Nếu `SocketTimeoutException` → Tăng timeout hoặc optimize email service
   - Nếu `ConnectException` → Email service không chạy hoặc network issue
   - Nếu `HttpServerErrorException` → Email service có lỗi nội bộ
   - Nếu `HttpClientErrorException` → Bad request, unauthorized, rate limit, etc.

## 📝 Log Examples

### Example 1: Timeout
```
ERROR - RuntimeException when sending activation email to: user@example.com. Exception type: RuntimeException, Cause: SocketTimeoutException, Message: Email service unavailable. Cannot connect to http://localhost:8200
ERROR - ResourceAccessException details - Cause: SocketTimeoutException, Message: Read timed out
```

**Giải pháp:** Tăng timeout hoặc optimize email service

### Example 2: Connection Refused
```
ERROR - RuntimeException when sending activation email to: user@example.com. Exception type: RuntimeException, Cause: ConnectException, Message: Email service unavailable. Cannot connect to http://localhost:8200
ERROR - ResourceAccessException details - Cause: ConnectException, Message: Connection refused
```

**Giải pháp:** Kiểm tra email service có chạy không

### Example 3: Server Error
```
ERROR - RuntimeException when sending activation email to: user@example.com. Exception type: RuntimeException, Cause: HttpServerErrorException, Message: Email service error: 500 INTERNAL_SERVER_ERROR
ERROR - Server error when sending activation link to user@example.com: 500 INTERNAL_SERVER_ERROR - {"error": "Internal server error"}
```

**Giải pháp:** Kiểm tra log của email service

## 🔗 Related Files

- `src/main/java/com/fix4home/fix4home/service/EmailVerificationService.java`
- `src/main/java/com/fix4home/fix4home/service/ActivationTokenService.java`
- `src/main/java/com/fix4home/fix4home/service/AuthService.java`
- `src/main/java/com/fix4home/fix4home/config/RestTemplateConfig.java`

