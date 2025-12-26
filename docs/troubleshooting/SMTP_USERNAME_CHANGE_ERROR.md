# 🔧 Lỗi Khi Đổi SMTP_USERNAME Ở Email Service

## 🔍 Vấn Đề

Khi chỉ đổi `SMTP_USERNAME` ở email service (sendmail microservice), backend bị lỗi mặc dù backend không trực tiếp cấu hình SMTP credentials.

## 🎯 Nguyên Nhân

### Flow Lỗi Khi SMTP_USERNAME Không Đúng:

```
1. Backend gửi request đến Email Service
   POST http://localhost:8200/generate-activation
   
2. Email Service nhận request
   → Cố gắng authenticate với SMTP server
   → Sử dụng SMTP_USERNAME và SMTP_PASSWORD từ .env
   
3. SMTP Authentication Fail
   → SMTP_USERNAME không đúng hoặc không khớp với SMTP_PASSWORD
   → SMTP server reject authentication
   
4. Email Service trả về error
   → 500 INTERNAL_SERVER_ERROR (hoặc 401/403 nếu SMTP server reject)
   → Response body: {"error": "SMTP authentication failed", ...}
   
5. Backend nhận HttpServerErrorException (500)
   EmailVerificationService.sendActivationLink()
   → catch HttpServerErrorException
   → throw RuntimeException("Email service error: 500")
   
6. Exception propagate lên
   → Nếu trong @Transactional method và không được catch đúng
   → Transaction có thể bị mark rollback-only
   → UnexpectedRollbackException
```

## 📊 Các Trường Hợp Lỗi

### 1. SMTP_USERNAME Không Tồn Tại Hoặc Sai

**Triệu chứng:**
- Email service log: `Authentication failed: Invalid credentials`
- Backend log: `Server error when sending activation link: 500 INTERNAL_SERVER_ERROR`

**Log từ Email Service:**
```
ERROR - SMTP authentication failed for username: wrong_username@example.com
ERROR - javax.mail.AuthenticationFailedException: 535 5.7.8 Username and Password not accepted
```

**Log từ Backend:**
```
ERROR - Server error when sending activation link to xxx@example.com: 500 INTERNAL_SERVER_ERROR - {"error":"SMTP authentication failed"}
ERROR - RuntimeException when sending activation email to: xxx. Exception type: RuntimeException, Cause: HttpServerErrorException, Message: Email service error: 500 INTERNAL_SERVER_ERROR
```

### 2. SMTP_USERNAME Không Khớp Với SMTP_PASSWORD

**Triệu chứng:**
- Tương tự case 1
- SMTP server reject vì credentials không match

### 3. SMTP_USERNAME Chưa Được Verify/Activated

**Triệu chứng:**
- Email service có thể kết nối được nhưng SMTP server từ chối gửi
- Error: `Sender address not verified`

### 4. SMTP_USERNAME Bị Block/Suspended

**Triệu chứng:**
- Authentication có thể pass nhưng gửi email fail
- Hoặc authentication fail với message về account suspended

## ✅ Giải Pháp

### Bước 1: Kiểm Tra SMTP Credentials Ở Email Service

Kiểm tra file `.env` trong email service:

```bash
# .microservice/MRS_SENDEMAIL_BE/.env
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com  # ← Kiểm tra giá trị này
SMTP_PASSWORD=your-app-password      # ← Phải khớp với SMTP_USERNAME
SMTP_FROM=noreply@fix4home.com      # ← Có thể khác với SMTP_USERNAME
```

**Lưu ý quan trọng:**
- `SMTP_USERNAME` phải là email address đúng format
- `SMTP_PASSWORD` phải là App Password (nếu dùng Gmail) hoặc password đúng
- `SMTP_USERNAME` và `SMTP_PASSWORD` phải khớp với nhau (same account)

### Bước 2: Test SMTP Connection Từ Email Service

```bash
# Vào container của email service
docker exec -it email-service-container bash

# Test SMTP connection (nếu có script test)
python test_smtp.py
# hoặc
node test_smtp.js
```

### Bước 3: Kiểm Tra Log Của Email Service

Xem log chi tiết của email service khi nhận request:

```bash
# Log của email service container
docker logs email-service-container --tail 100 | grep -i "smtp\|auth\|error"
```

Tìm các log như:
- `SMTP authentication failed`
- `javax.mail.AuthenticationFailedException`
- `Invalid credentials`
- `Username and Password not accepted`

### Bước 4: Verify SMTP_USERNAME Có Thể Gửi Email

**Với Gmail:**
1. Đảm bảo account đã enable 2-Step Verification
2. Tạo App Password cho `SMTP_PASSWORD` (không dùng regular password)
3. Kiểm tra `SMTP_USERNAME` là email address đúng

**Với SMTP Server Khác:**
1. Test đăng nhập với `SMTP_USERNAME` và `SMTP_PASSWORD`
2. Verify account không bị suspended
3. Kiểm tra SPF/DKIM records nếu cần

### Bước 5: Restart Email Service Sau Khi Đổi Config

```bash
# Restart email service để load config mới
docker-compose restart email-service
# hoặc
docker restart email-service-container
```

## 🛡️ Protection Ở Backend

### Hiện Tại: Email Gửi Sau Transaction Commit

Backend đã được fix để gửi email **SAU KHI** transaction commit thành công:

```147:178:src/main/java/com/fix4home/fix4home/service/AuthService.java
        // Send email verification AFTER transaction commits to avoid rollback issues
        // Use TransactionSynchronizationManager to send email after commit
        if (savedUser.getStatus() == UserStatus.PENDING_EMAIL_VERIFICATION) {
            final User userToEmail = savedUser; // Final variable for use in inner class
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            log.info("Transaction committed, sending activation email to: {}", userToEmail.getEmail());
                            ActivationTokenService.ActivationTokenResponse response = 
                                activationTokenService.generateActivationToken(userToEmail, "registration");
                            if (!response.isSuccess()) {
                                log.error("Failed to send verification email to: {}. Response: {}", 
                                         userToEmail.getEmail(), response.getMessage());
                            } else {

                            }
                        } catch (Exception e) {
                            log.error("Error sending activation email after commit to: {}. Exception type: {}, Message: {}", 
                                     userToEmail.getEmail(),
                                     e.getClass().getSimpleName(),
                                     e.getMessage(), 
                                     e);
                            // Don't throw exception here as transaction already committed
                            // Email sending failure won't affect registration success
                        }
                    }
                }
            );
            log.info("Registered email sending to run after transaction commit for: {}", savedUser.getEmail());
        }
```

**Lợi ích:**
- ✅ User registration thành công dù email service fail
- ✅ Transaction không bị rollback vì email gửi sau commit
- ✅ Email error được log nhưng không ảnh hưởng đến registration

### Các Nơi Khác Có Thể Bị Ảnh Hưởng

Nếu có chỗ khác gọi `EmailVerificationService` **TRONG** transaction và không handle exception đúng:

```java
// ❌ CÓ THỂ GÂY ROLLBACK
@Transactional
public void someMethod() {
    // ... some operations ...
    emailVerificationService.sendActivationLink(...); // Nếu throw exception → rollback
    // ... more operations ...
}

// ✅ AN TOÀN
@Transactional
public void someMethod() {
    // ... some operations ...
    try {
        emailVerificationService.sendActivationLink(...);
    } catch (Exception e) {
        log.error("Email sending failed but continuing", e);
        // Don't throw - transaction continues
    }
    // ... more operations ...
}
```

## 📝 Checklist Khi Đổi SMTP_USERNAME

- [ ] **SMTP_USERNAME** là email address hợp lệ
- [ ] **SMTP_PASSWORD** khớp với **SMTP_USERNAME** (same account)
- [ ] Nếu dùng Gmail: Đã tạo App Password (không dùng regular password)
- [ ] SMTP account đã được verify/activated
- [ ] SMTP account không bị suspended/blocked
- [ ] Restart email service sau khi đổi config
- [ ] Test gửi email từ email service trực tiếp
- [ ] Kiểm tra log của email service khi gửi email
- [ ] Verify backend vẫn nhận được response từ email service

## 🔍 Debug Commands

### Kiểm Tra Email Service Health

```bash
# Health check
curl http://localhost:8200/health

# Expected: {"status":"healthy"}
```

### Kiểm Tra Log Backend Khi Có Lỗi

```bash
# Tìm error về email service
grep -i "email.*error\|smtp\|500\|Server error when sending" logs/fix4home-error.log

# Tìm error về SMTP authentication
grep -i "authentication.*fail\|smtp.*fail\|credential" logs/fix4home-error.log
```

### Test Gửi Email Trực Từ Email Service

```bash
# Gọi API của email service trực tiếp
curl -X POST http://localhost:8200/generate-activation \
  -H "Content-Type: application/json" \
  -H "x-api-key: fix4home_prod_123abc456def789" \
  -d '{
    "email": "test@example.com",
    "action": "registration",
    "system": "Fix4Home"
  }'
```

## ✅ Kết Luận

**Nguyên nhân:** Khi đổi `SMTP_USERNAME` mà không đúng hoặc không khớp với `SMTP_PASSWORD`, email service không thể authenticate với SMTP server → trả về 500 error → backend nhận exception.

**Giải pháp:**
1. ✅ Đảm bảo `SMTP_USERNAME` và `SMTP_PASSWORD` khớp nhau
2. ✅ Verify SMTP account có thể gửi email
3. ✅ Restart email service sau khi đổi config
4. ✅ Backend đã được protect (email gửi sau transaction commit) → không rollback registration

**Lưu ý:** Backend **KHÔNG** phụ thuộc vào `SMTP_USERNAME`, nhưng phụ thuộc vào response của email service. Nếu email service fail → backend nhận error response → có thể gây lỗi nếu không handle đúng.

