# 🔧 Test Connection: Backend → Email Service

## 📋 Vấn Đề

Backend không gọi được email service ở `http://localhost:8200` khi gửi mail.

## 🧪 Các Bước Test Connection

### Bước 1: Kiểm Tra Email Service Có Chạy Không

```bash
# Test health check (không cần API key)
curl -X GET http://localhost:8200/health

# Expected response:
# {"status":"healthy"}
```

**Nếu không có response:**
- Email service không chạy
- Port 8200 bị chiếm bởi service khác
- Firewall block

**Giải pháp:**
```bash
# Kiểm tra port 8200
netstat -an | grep 8200
# hoặc
lsof -i :8200

# Khởi động email service
# (tùy vào cách bạn chạy email service)
```

---

### Bước 2: Test Endpoint Với API Key

```bash
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

# Expected response:
# {
#   "success": true,
#   "message": "Activation link sent successfully",
#   "can_resend": false,
#   "next_resend_at": null,
#   "send_count": 1,
#   "max_sends": 3
# }
```

**Nếu lỗi 401/403:**
- API key không đúng
- Kiểm tra API key trong `application.properties`

**Nếu lỗi 500:**
- Email service có lỗi nội bộ
- Kiểm tra log của email service

**Nếu timeout:**
- Email service xử lý chậm
- Kiểm tra database/network của email service

---

### Bước 3: Test Từ Backend Code

Thêm test endpoint trong `EmailVerificationService`:

```java
// Test method (temporary)
public void testConnection() {
    log.info("Testing connection to email service at: {}", emailServiceUrl);
    
    // Test 1: Health check
    try {
        ResponseEntity<Map> healthResponse = restTemplate.getForEntity(
            emailServiceUrl + "/health", 
            Map.class
        );
        log.info("Health check: {}", healthResponse.getBody());
    } catch (Exception e) {
        log.error("Health check failed: {}", e.getMessage(), e);
    }
    
    // Test 2: Send activation link
    try {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", "test@example.com");
        requestBody.put("action", "registration");
        requestBody.put("system", systemName);
        
        Map<String, Object> customData = new HashMap<>();
        customData.put("user_id", "999");
        customData.put("action", "registration");
        requestBody.put("customData", customData);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            emailServiceUrl + "/generate-activation", 
            request, 
            Map.class
        );
        
        log.info("Send activation link test: Status={}, Body={}", 
                response.getStatusCode(), response.getBody());
    } catch (Exception e) {
        log.error("Send activation link test failed: {}", e.getMessage(), e);
    }
}
```

---

### Bước 4: Kiểm Tra Configuration

File: `src/main/resources/application.properties`

```properties
# Kiểm tra các giá trị này
email.verification.service.url=http://localhost:8200
email.verification.service.api-key=fix4home_prod_123abc456def789
```

**Test trong code:**
```java
@Value("${email.verification.service.url}")
private String emailServiceUrl;

@PostConstruct
public void logConfig() {
    log.info("Email service URL: {}", emailServiceUrl);
    log.info("Email service API key present: {}", apiKey != null && !apiKey.isEmpty());
}
```

---

### Bước 5: Kiểm Tra Network/Firewall

```bash
# Test từ backend server
telnet localhost 8200
# hoặc
nc -zv localhost 8200

# Nếu không connect được:
# - Firewall block
# - Email service không listen trên 0.0.0.0
# - Network issue
```

---

### Bước 6: Kiểm Tra Timeout

```bash
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

**Nếu timeout > 15 giây:**
- Email service xử lý chậm
- Cần tăng timeout trong `RestTemplateConfig.java`

---

## 🔍 Debug Logs

### Enable Debug Logging

File: `src/main/resources/application.properties`

```properties
# Enable HTTP client logging
logging.level.org.springframework.web.client.RestTemplate=DEBUG
logging.level.org.apache.http=DEBUG
```

### Xem Logs

```bash
# Tìm connection errors
grep -i "cannot connect\|connection refused\|timeout\|resourceaccess" logs/fix4home.log | tail -20

# Tìm email service requests
grep -i "sending activation link\|email service response" logs/fix4home.log | tail -20

# Tìm exceptions
grep -i "exception.*email\|runtimeexception.*email" logs/fix4home.log | tail -20
```

---

## 🛠️ Common Issues & Solutions

### Issue 1: Connection Refused

**Lỗi:**
```
ResourceAccessException: Cannot connect to email service at http://localhost:8200
ConnectException: Connection refused
```

**Giải pháp:**
1. Kiểm tra email service có chạy không
2. Kiểm tra port 8200 có bị chiếm không
3. Kiểm tra email service có listen trên `0.0.0.0:8200` không (không phải `127.0.0.1:8200`)

---

### Issue 2: Timeout

**Lỗi:**
```
SocketTimeoutException: Read timed out
```

**Giải pháp:**
1. Kiểm tra email service có xử lý chậm không
2. Tăng timeout trong `RestTemplateConfig.java`:
   ```java
   .setResponseTimeout(Timeout.ofSeconds(30))  // Tăng từ 15 → 30
   ```
3. Optimize email service

---

### Issue 3: Unauthorized

**Lỗi:**
```
HttpClientErrorException: 401 Unauthorized
```

**Giải pháp:**
1. Kiểm tra API key trong `application.properties`
2. Kiểm tra API key có đúng với email service không
3. Kiểm tra email service có yêu cầu API key không

---

### Issue 4: Email Service Not Responding

**Triệu chứng:**
- Health check OK
- Nhưng gửi email không được

**Giải pháp:**
1. Kiểm tra log của email service
2. Kiểm tra database của email service
3. Kiểm tra SMTP configuration của email service

---

## 📊 Test Checklist

- [ ] Email service chạy ở port 8200
- [ ] Health check endpoint hoạt động (`GET /health`)
- [ ] API key đúng trong `application.properties`
- [ ] Test send activation link thành công
- [ ] Backend có thể connect đến `http://localhost:8200`
- [ ] Timeout configuration phù hợp (15 giây)
- [ ] Logs hiển thị request/response đầy đủ

---

## 🔗 Related Documents

- `docs/api/EMAIL_SERVICE_ENDPOINTS.md` - Tổng hợp các endpoint
- `docs/troubleshooting/EMAIL_SERVICE_ERROR_TROUBLESHOOTING.md` - Troubleshooting chung
- `docs/troubleshooting/EMAIL_EXCEPTION_DEBUG.md` - Debug exceptions

