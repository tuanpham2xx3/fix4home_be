# 📧 Email Service Endpoints - Backend Integration

## 📋 Tổng Quan

Backend Java gọi đến Email Microservice (chạy ở `http://localhost:8200`) để gửi email verification, activation links, và các chức năng liên quan.

**Base URL:** `http://localhost:8200` (configurable via `email.verification.service.url`)

**Authentication:** Header `x-api-key` (configurable via `email.verification.service.api-key`)

---

## 🔑 Configuration

File: `src/main/resources/application.properties`

```properties
# Email Verification Service Configuration
email.verification.service.url=http://localhost:8200
email.verification.service.api-key=${EMAIL_VERIFICATION_API_KEY:fix4home_prod_123abc456def789}
email.verification.system.name=Fix4Home

# Email Verification Method
email.verification.method=${EMAIL_VERIFICATION_METHOD:activation_link}

# Frontend URL (email service tự động dùng FRONTEND_URL từ config của nó)
email.activation.frontend.base-url=${FRONTEND_BASE_URL:https://fe.iceteadev.site}
```

---

## 📡 Các Endpoint Backend Gọi Đến Email Service

### 1. Health Check

**Endpoint:** `GET /health`

**Mô tả:** Kiểm tra email service có hoạt động không

**Method:** `EmailVerificationService.checkServiceHealth()`

**Request:**
```http
GET http://localhost:8200/health
```

**Response (200 OK):**
```json
{
  "status": "healthy"
}
```

**Lưu ý:** 
- ✅ **KHÔNG cần API key** (public endpoint)
- Được gọi tự động mỗi 30 giây bởi `EmailServiceHealthMonitor`
- Được gọi trước khi gửi email nếu service unhealthy

---

### 2. Send Activation Link (New - Recommended)

**Endpoint:** `POST /generate-activation`

**Mô tả:** Gửi activation link qua email (method mới, recommended)

**Method:** `EmailVerificationService.sendActivationLink()`

**Request:**
```http
POST http://localhost:8200/generate-activation
Content-Type: application/json
x-api-key: fix4home_prod_123abc456def789

{
  "email": "user@example.com",
  "action": "registration",
  "system": "Fix4Home",
  "customData": {
    "user_id": "123",
    "action": "registration"
  }
}
```

**Request Body Fields:**
- `email` (required): Email người nhận
- `action` (required): Loại action (`registration`, `password_reset`, etc.)
- `system` (required): Tên hệ thống (`Fix4Home`)
- `customData` (optional): Dữ liệu tùy chỉnh
  - `user_id`: ID của user
  - `action`: Loại action

**Lưu ý:** 
- ❌ **KHÔNG gửi `baseUrl`** - Email service tự động dùng `FRONTEND_URL` từ config của nó

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Activation link sent successfully",
  "can_resend": false,
  "next_resend_at": null,
  "send_count": 1,
  "max_sends": 3
}
```

**Response Fields:**
- `success`: true nếu thành công
- `message`: Thông báo
- `can_resend`: Có thể resend không
- `next_resend_at`: Thời gian có thể resend tiếp (timestamp)
- `send_count`: Số lần đã gửi
- `max_sends`: Số lần tối đa có thể gửi

**Error Responses:**

**429 Too Many Requests (Rate Limit):**
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

**500 Internal Server Error:**
```json
{
  "error": "Internal server error"
}
```

**Khi nào được gọi:**
- User registration (`AuthService.register()`)
- Password reset (`ActivationTokenService.generatePasswordResetToken()`)
- Resend activation link

---

### 3. Verify Activation Token

**Endpoint:** `POST /verify-activation`

**Mô tả:** Verify activation token từ email link

**Method:** `EmailVerificationService.verifyActivationToken()`

**Request:**
```http
POST http://localhost:8200/verify-activation
Content-Type: application/json
x-api-key: fix4home_prod_123abc456def789

{
  "token": "abc123def456..."
}
```

**Request Body Fields:**
- `token` (required): Activation token từ email link

**Response (200 OK):**
```json
{
  "success": true,
  "email": "user@example.com",
  "action": "registration",
  "customData": {
    "user_id": "123",
    "action": "registration"
  }
}
```

**Khi nào được gọi:**
- User click vào activation link trong email
- `AuthController.verifyActivationToken()`

---

### 4. Resend Activation Link

**Endpoint:** `POST /resend-activation`

**Mô tả:** Gửi lại activation link

**Method:** `EmailVerificationService.resendActivationLink()`

**Request:**
```http
POST http://localhost:8200/resend-activation
Content-Type: application/json
x-api-key: fix4home_prod_123abc456def789

{
  "email": "user@example.com",
  "action": "registration"
}
```

**Request Body Fields:**
- `email` (required): Email người nhận
- `action` (required): Loại action

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Activation link resent successfully",
  "can_resend": false,
  "next_resend_at": 1703568000,
  "send_count": 2,
  "max_sends": 3
}
```

**Khi nào được gọi:**
- User request resend activation email
- `AuthController.resendActivationLink()`

---

### 5. Send Verification Code (Legacy - Deprecated)

**Endpoint:** `POST /generate`

**Mô tả:** Gửi verification code 6 chữ số (legacy method, không dùng nữa)

**Method:** `EmailVerificationService.sendVerificationCode()`

**Request:**
```http
POST http://localhost:8200/generate
Content-Type: application/json
x-api-key: fix4home_prod_123abc456def789

{
  "email": "user@example.com",
  "system": "Fix4Home",
  "customData": {
    "user_id": "123",
    "action": "registration"
  }
}
```

**Response (200 OK):**
```json
{
  "success": true
}
```

**Lưu ý:** 
- ⚠️ **Deprecated** - Chỉ dùng cho backward compatibility
- Method mới: `sendActivationLink()` (recommended)

---

### 6. Verify Code (Legacy - Deprecated)

**Endpoint:** `POST /verify`

**Mô tả:** Verify verification code 6 chữ số (legacy method, không dùng nữa)

**Method:** `EmailVerificationService.verifyCode()`

**Request:**
```http
POST http://localhost:8200/verify
Content-Type: application/json
x-api-key: fix4home_prod_123abc456def789

{
  "email": "user@example.com",
  "code": "123456"
}
```

**Response (200 OK):**
```json
{
  "success": true
}
```

**Lưu ý:** 
- ⚠️ **Deprecated** - Chỉ dùng cho backward compatibility

---

## 🔧 Timeout Configuration

File: `src/main/java/com/fix4home/fix4home/config/RestTemplateConfig.java`

```java
RequestConfig requestConfig = RequestConfig.custom()
    .setConnectTimeout(Timeout.ofSeconds(5))      // 5 giây để connect
    .setResponseTimeout(Timeout.ofSeconds(15))     // 15 giây để nhận response
    .setConnectionRequestTimeout(Timeout.ofSeconds(3)) // 3 giây để lấy connection từ pool
    .build();
```

**Nếu email service xử lý > 15 giây → sẽ timeout**

---

## 🔄 Retry & Circuit Breaker

### Retry Configuration

File: `src/main/resources/application.properties`

```properties
# Retry Configuration
resilience4j.retry.instances.emailService.maxAttempts=3
resilience4j.retry.instances.emailService.waitDuration=1s
resilience4j.retry.instances.emailService.enableExponentialBackoff=true
resilience4j.retry.instances.emailService.exponentialBackoffMultiplier=2
```

**Retry sẽ tự động retry 3 lần với exponential backoff (1s, 2s, 4s)**

### Circuit Breaker Configuration

```properties
# Circuit Breaker Configuration
resilience4j.circuitbreaker.instances.emailService.slidingWindowSize=10
resilience4j.circuitbreaker.instances.emailService.failureRateThreshold=50
resilience4j.circuitbreaker.instances.emailService.waitDurationInOpenState=30s
```

**Circuit breaker sẽ mở nếu failure rate > 50%**

---

## 🐛 Troubleshooting

### 1. Connection Refused

**Lỗi:**
```
ResourceAccessException: Cannot connect to email service at http://localhost:8200
```

**Giải pháp:**
- Kiểm tra email service có chạy không: `curl http://localhost:8200/health`
- Kiểm tra port 8200 có bị chiếm không: `netstat -an | grep 8200`
- Kiểm tra firewall/network

### 2. Timeout

**Lỗi:**
```
SocketTimeoutException: Read timed out
```

**Giải pháp:**
- Kiểm tra email service có xử lý chậm không
- Tăng timeout trong `RestTemplateConfig.java` (từ 15s → 30s)
- Optimize email service

### 3. Unauthorized (401/403)

**Lỗi:**
```
HttpClientErrorException: 401 Unauthorized
```

**Giải pháp:**
- Kiểm tra API key trong `application.properties`
- Kiểm tra API key có đúng với email service không

### 4. Rate Limit (429)

**Lỗi:**
```
HttpClientErrorException: 429 Too Many Requests
```

**Giải pháp:**
- Đợi một lúc rồi thử lại
- Kiểm tra `next_resend_at` trong response
- Giảm số lần gửi email

---

## 📊 Code Examples

### Backend Java Code

```java
// EmailVerificationService.java

// 1. Health Check
public boolean checkServiceHealth() {
    ResponseEntity<Map> response = restTemplate.getForEntity(
        emailServiceUrl + "/health", 
        Map.class
    );
    // ...
}

// 2. Send Activation Link
public ActivationResponse sendActivationLink(String email, String action, Long userId) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("x-api-key", apiKey);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("email", email);
    requestBody.put("action", action);
    requestBody.put("system", systemName);
    
    Map<String, Object> customData = new HashMap<>();
    customData.put("user_id", userId.toString());
    customData.put("action", action);
    requestBody.put("customData", customData);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
    
    ResponseEntity<Map> response = restTemplate.postForEntity(
        emailServiceUrl + "/generate-activation", 
        request, 
        Map.class
    );
    // ...
}
```

---

## 🧪 Test Endpoints Manually

### Test Health Check

```bash
curl -X GET http://localhost:8200/health
```

### Test Send Activation Link

```bash
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

### Test Verify Activation Token

```bash
curl -X POST http://localhost:8200/verify-activation \
  -H "Content-Type: application/json" \
  -H "x-api-key: fix4home_prod_123abc456def789" \
  -d '{
    "token": "abc123def456..."
  }'
```

---

## 📝 Summary

| Endpoint | Method | Auth | Mô Tả | Status |
|----------|--------|------|-------|--------|
| `/health` | GET | ❌ | Health check | ✅ Active |
| `/generate-activation` | POST | ✅ | Send activation link | ✅ **Recommended** |
| `/verify-activation` | POST | ✅ | Verify activation token | ✅ Active |
| `/resend-activation` | POST | ✅ | Resend activation link | ✅ Active |
| `/generate` | POST | ✅ | Send verification code | ⚠️ Deprecated |
| `/verify` | POST | ✅ | Verify code | ⚠️ Deprecated |

---

## 🔗 Related Files

- `src/main/java/com/fix4home/fix4home/service/EmailVerificationService.java`
- `src/main/java/com/fix4home/fix4home/config/RestTemplateConfig.java`
- `src/main/resources/application.properties`
- `docs/troubleshooting/EMAIL_SERVICE_ERROR_TROUBLESHOOTING.md`

