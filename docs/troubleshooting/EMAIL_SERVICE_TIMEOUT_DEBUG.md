# 🔍 Debug: Email Service Timeout Issue

## 📋 Vấn Đề

Từ log (dòng 167-282), ta thấy:
- ✅ User registration thành công
- ✅ Activation token được tạo trong database
- ❌ **KHÔNG thấy log "Sending activation link request"**
- ❌ Request có thể bị timeout hoặc exception không được log

## 🔍 Phân Tích

### Timeline từ log:
1. **10:13:50.654** - User registration bắt đầu
2. **10:13:50.833** - Insert user vào database
3. **10:13:50.854** - Insert customer_profile
4. **10:13:50.877** - Check rate limiting
5. **10:13:50.899** - Insert activation_token vào database
6. **10:14:15.762** - Health check (25 giây sau) - HEALTHY
7. **❌ KHÔNG thấy log gửi email**

### Nguyên nhân có thể:

1. **Request Timeout (15 giây)**
   - RestTemplate có `responseTimeout = 15 seconds`
   - Email service có thể xử lý chậm hoặc không phản hồi
   - Exception `SocketTimeoutException` có thể xảy ra

2. **Email Service Không Phản Hồi**
   - Email service có thể đang xử lý request nhưng không trả về response
   - Connection có thể bị drop

3. **Exception Không Được Log Đầy Đủ**
   - Có thể exception bị catch nhưng không log đầy đủ
   - Circuit breaker có thể đã mở và fallback được gọi

## 🛠️ Cách Debug

### 1. Kiểm Tra Log Chi Tiết

Sau khi cập nhật code, log sẽ hiển thị:
```
INFO  - Sending activation link request to: http://localhost:8200/generate-activation for email: xxx, action: registration
DEBUG - Request body: {...}
DEBUG - Request headers: x-api-key present: true
DEBUG - Calling email service at: http://localhost:8200/generate-activation
```

Nếu không thấy các log này, có thể:
- Code chưa đến được phần gửi request
- Exception xảy ra trước khi gửi request

### 2. Kiểm Tra Timeout

RestTemplate timeout configuration:
```java
// RestTemplateConfig.java
.setConnectTimeout(Timeout.ofSeconds(5))      // 5 giây để connect
.setResponseTimeout(Timeout.ofSeconds(15))     // 15 giây để nhận response
.setConnectionRequestTimeout(Timeout.ofSeconds(3)) // 3 giây để lấy connection từ pool
```

**Nếu email service xử lý > 15 giây → sẽ timeout**

### 3. Kiểm Tra Email Service

```bash
# Test email service có phản hồi nhanh không
time curl -X POST http://localhost:8200/generate-activation \
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

Nếu response time > 15 giây → cần tăng timeout hoặc optimize email service

### 4. Kiểm Tra Circuit Breaker

Circuit breaker có thể đã mở nếu:
- Email service fail nhiều lần
- Failure rate > 50%

Kiểm tra log:
```
WARN - Circuit breaker activated for email service
ERROR - Circuit breaker opened
```

### 5. Kiểm Tra Exception Logs

Sau khi cập nhật code, log sẽ hiển thị chi tiết exception:
```
ERROR - Cannot connect to email service at http://localhost:8200 for email: xxx. Error: ...
ERROR - ResourceAccessException details - Cause: SocketTimeoutException, Message: ...
ERROR - Exception type: ResourceAccessException, Cause: SocketTimeoutException
```

## 🔧 Giải Pháp

### Giải Pháp 1: Tăng Timeout (Tạm Thời)

Nếu email service xử lý chậm, có thể tăng timeout:

```java
// RestTemplateConfig.java
RequestConfig requestConfig = RequestConfig.custom()
    .setConnectTimeout(Timeout.ofSeconds(5))
    .setResponseTimeout(Timeout.ofSeconds(30))  // Tăng từ 15 → 30 giây
    .setConnectionRequestTimeout(Timeout.ofSeconds(3))
    .build();
```

**⚠️ Lưu ý:** Chỉ tăng timeout nếu thực sự cần thiết. Tốt hơn là optimize email service.

### Giải Pháp 2: Kiểm Tra Email Service Performance

1. **Kiểm tra log của email service:**
   - Xem email service có đang xử lý request không
   - Xem có lỗi gì trong email service không

2. **Kiểm tra database của email service:**
   - Có thể database query chậm
   - Có thể có deadlock

3. **Kiểm tra network:**
   - Latency giữa backend và email service
   - Firewall/proxy có block không

### Giải Pháp 3: Async Email Sending (Long-term)

Thay vì gửi email đồng bộ trong registration flow, có thể:
1. Tạo user và activation token
2. Return success response ngay
3. Gửi email bất đồng bộ (async)
4. User có thể resend activation email nếu cần

## 📊 Monitoring

### Metrics để theo dõi:

1. **Email Send Duration:**
   ```
   email.send.duration
   ```
   - Nếu > 15 giây → sẽ timeout

2. **Email Send Failures:**
   ```
   email.send.failure
   ```
   - Tăng khi có lỗi

3. **Circuit Breaker State:**
   - Kiểm tra circuit breaker có mở không

## 🎯 Next Steps

1. **Restart backend** với code đã cập nhật (có thêm log chi tiết)
2. **Test lại registration** và xem log chi tiết
3. **Kiểm tra email service** có đang xử lý request không
4. **Nếu timeout:** Tăng timeout hoặc optimize email service
5. **Nếu exception:** Xem log chi tiết để biết nguyên nhân

## 📝 Log Pattern để Tìm

Sau khi restart, tìm trong log:
```bash
# Tìm log gửi email
grep -i "Sending activation link request" logs/fix4home.log

# Tìm exception
grep -i "ResourceAccessException\|SocketTimeoutException\|Exception sending" logs/fix4home.log

# Tìm timeout
grep -i "timeout\|timed out" logs/fix4home.log
```

## 🔗 Related Files

- `src/main/java/com/fix4home/fix4home/service/EmailVerificationService.java`
- `src/main/java/com/fix4home/fix4home/config/RestTemplateConfig.java`
- `src/main/resources/application.properties` (Resilience4j config)

