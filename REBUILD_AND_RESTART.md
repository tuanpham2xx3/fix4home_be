# 🔄 Rebuild và Restart Backend

## Vấn đề hiện tại

Backend đang chạy code cũ, chưa có các fix:
1. Thiếu `baseUrl` trong request gửi đến email service
2. Email sending block response trong transaction callback
3. Transaction bị rollback do exception

## Giải pháp

Rebuild backend để áp dụng code mới đã sửa.

---

## Windows (PowerShell)

### Cách 1: Dùng script có sẵn (khuyến nghị)

```powershell
# Stop backend hiện tại (Ctrl+C trong terminal đang chạy backend)
# Hoặc tìm và kill process:
Get-Process -Name "java" | Where-Object {$_.CommandLine -like "*fix4home*"} | Stop-Process -Force

# Rebuild và restart
.\run-local.ps1
```

### Cách 2: Build thủ công

```powershell
# Build (bỏ qua tests để build nhanh hơn)
# Lưu ý: Trong PowerShell, cần đặt tham số trong dấu ngoặc kép
.\mvnw.cmd clean package "-Dmaven.test.skip=true"

# Hoặc dùng -DskipTests (không cần ngoặc kép)
.\mvnw.cmd clean package -DskipTests

# Sau khi build xong, restart backend
java -jar target/fix4home-0.0.1-SNAPSHOT.jar
```

---

## Linux/Mac

### Cách 1: Dùng script có sẵn

```bash
# Stop backend hiện tại (Ctrl+C trong terminal đang chạy backend)

# Rebuild và restart
./run-local.sh
```

### Cách 2: Build thủ công

```bash
# Build (bỏ qua tests để build nhanh hơn)
./mvnw clean package -Dmaven.test.skip=true

# Restart backend
java -jar target/fix4home-0.0.1-SNAPSHOT.jar
```

---

## Kiểm tra sau khi restart

### 1. Kiểm tra logs

```powershell
# Windows
Get-Content logs\fix4home.log -Tail 50 -Wait

# Linux/Mac
tail -f logs/fix4home.log
```

### 2. Test đăng ký

```powershell
# Windows
.\test_register.ps1

# Linux/Mac
./test_register.sh
```

### 3. Kiểm tra email service

```powershell
# Windows
cd .microservice\MRS_SENDEMAIL_BE
.\test-connection.ps1
```

---

## Expected Results

Sau khi restart với code mới:

1. ✅ Đăng ký thành công, frontend nhận response ngay lập tức
2. ✅ User được lưu vào DB với status `PENDING_EMAIL_VERIFICATION`
3. ✅ Email được gửi trong background thread (không block response)
4. ✅ Nếu email service lỗi, chỉ log error, không ảnh hưởng đăng ký

### Logs mong muốn

```
Transaction committed, sending activation email to: user@example.com in background thread
=== START sendActivationLink: email=user@example.com, action=registration, userId=123 ===
Email service response received: success=true
Activation email sent successfully to: user@example.com
```

### Response cho frontend

```json
{
  "code": 200,
  "message": "User registered successfully",
  "data": {
    "userId": 123,
    "username": "user123",
    "email": "user@example.com",
    "role": "CUSTOMER",
    "status": "PENDING_EMAIL_VERIFICATION",
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

## Troubleshooting

### Build failed

```powershell
# Kiểm tra Java version
java -version  # Cần Java 17+

# Clean và rebuild (bỏ qua tests)
.\mvnw.cmd clean
.\mvnw.cmd package "-Dmaven.test.skip=true"
# Hoặc: .\mvnw.cmd package -DskipTests
```

### Email service không chạy

```powershell
# Kiểm tra email service
cd .microservice\MRS_SENDEMAIL_BE
.\test-connection.ps1

# Nếu không chạy, start email service
go run cmd/server/main.go
```

### Backend không connect được email service

Kiểm tra config trong `application.properties`:

```properties
email.verification.service.url=http://localhost:8200
email.verification.service.api-key=${EMAIL_VERIFICATION_API_KEY:fix4home_prod_123abc456def789}
email.activation.frontend.base-url=${FRONTEND_BASE_URL:https://fe.iceteadev.site}
```

---

## Các fix đã áp dụng

### 1. EmailVerificationService.java

- ✅ Thêm `baseUrl` vào `sendActivationLink()` method
- ✅ Thêm `baseUrl` vào `resendActivationLink()` method

### 2. AuthService.java

- ✅ Gửi email trong background thread (không block response)
- ✅ Exception trong email sending được catch và log
- ✅ Transaction không bị rollback do email error

### 3. Luồng đăng ký mới

```
User đăng ký
  → Validate request
  → Save user to DB
  → Commit transaction
  → Return response to frontend (ngay lập tức)
  → [Background thread] Send activation email
      → Nếu thành công: Log success
      → Nếu lỗi: Log error (không ảnh hưởng đăng ký)
```

