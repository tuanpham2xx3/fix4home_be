# Email Request Lock Timeout - Root Cause và Giải Pháp

## 🔍 Vấn Đề

Khi user đăng ký, email microservice chỉ nhận được health check requests, không nhận được request gửi email (`/generate-activation`).

## 🎯 Nguyên Nhân Gốc Rễ

**Vấn đề KHÔNG phải ở email service hay circuit breaker!**

Vấn đề thực sự là: **Transaction lock timeout khi save activation token vào database**.

### Phân Tích Chi Tiết

1. **Flow xử lý đăng ký:**
   ```
   AuthService.register()
   → ActivationTokenService.generateActivationToken()
   → ActivationTokenService.generateActivationTokenInternal()
   → activationTokenRepository.save(token)  ← ❌ LỖI Ở ĐÂY
   → emailVerificationService.sendActivationLink()  ← ❌ KHÔNG BAO GIỜ ĐẾN ĐƯỢC
   ```

2. **Lỗi xảy ra:**
   - `PessimisticLockingFailureException: Lock wait timeout exceeded`
   - Xảy ra tại dòng 207: `token = activationTokenRepository.save(token);`
   - Code **KHÔNG BAO GIỜ** đến dòng 210 (gọi email service)

3. **Tại sao có lock timeout?**
   - Nhiều request cùng lúc cố gắng insert/update token cho cùng một email
   - Transaction isolation level `READ_COMMITTED` + không có lock strategy rõ ràng
   - `findActiveTokenByEmailAndAction()` có thể đang lock row khi SELECT
   - Transaction quá dài (bao gồm cả việc gọi email service) → tăng khả năng conflict

4. **Bằng chứng:**
   - Log không có "About to call emailVerificationService.sendActivationLink"
   - Log chỉ có "Exception sending verification email" với `PessimisticLockingFailureException`
   - Email service không nhận được request vì code không bao giờ đến được phần gọi email service

## ✅ Giải Pháp

### 1. Tách Transaction

Tách việc **save token** và **send email** thành 2 transaction riêng biệt:

```java
// Transaction 1: Save token (ngắn, ít conflict)
@Transactional(propagation = Propagation.REQUIRES_NEW)
private TokenSaveResult saveTokenInTransaction(...) {
    // Save token logic
}

// Transaction 2: Send email (không cần transaction, hoặc transaction riêng)
private ActivationTokenResponse generateActivationTokenInternal(...) {
    // Save token trong transaction riêng
    TokenSaveResult result = saveTokenInTransaction(...);
    
    // Send email (ngoài transaction để tránh blocking)
    emailVerificationService.sendActivationLink(...);
}
```

### 2. Cải Thiện Error Handling

- Catch `PessimisticLockingFailureException` riêng
- Log chi tiết để debug
- Throw message rõ ràng cho user

### 3. Thêm Logging Chi Tiết

- Log trước khi save token
- Log sau khi save thành công
- Log trước khi gọi email service
- Log circuit breaker state

## 📝 Code Changes

### File: `ActivationTokenService.java`

1. **Tạo method riêng để save token:**
   ```java
   @Transactional(propagation = Propagation.REQUIRES_NEW)
   private TokenSaveResult saveTokenInTransaction(...) {
       // Logic save token với error handling tốt hơn
   }
   ```

2. **Tách transaction trong `generateActivationTokenInternal`:**
   ```java
   // Save token trong transaction riêng
   TokenSaveResult result = saveTokenInTransaction(...);
   
   // Send email (ngoài transaction)
   emailVerificationService.sendActivationLink(...);
   ```

3. **Cải thiện error handling:**
   - Catch `PessimisticLockingFailureException` riêng
   - Throw message rõ ràng: "Another request is processing. Please wait a moment and try again."

## 🧪 Testing

### Test Case 1: Single Request
- ✅ User đăng ký lần đầu → Token được save → Email được gửi

### Test Case 2: Concurrent Requests
- ✅ 2 requests cùng lúc cho cùng email → 1 thành công, 1 báo lỗi rõ ràng
- ✅ Không có lock timeout

### Test Case 3: Email Service Down
- ✅ Token được save thành công
- ✅ Email service error được handle đúng
- ✅ Token được xóa nếu là new token

## 📊 Monitoring

### Logs để theo dõi:

1. **Token Save:**
   ```
   === Saving activation token: email=..., action=..., userId=..., isNewToken=... ===
   === Token saved successfully: tokenId=... ===
   ```

2. **Email Service Call:**
   ```
   === About to call emailVerificationService.sendActivationLink: email=..., action=..., userId=... ===
   === START sendActivationLink: email=..., action=..., userId=... ===
   ```

3. **Lock Timeout (nếu vẫn xảy ra):**
   ```
   ⚠️ PessimisticLockingFailureException when saving token for email: ...
   This usually means another transaction is locking the row.
   ```

## 🔄 Rollback Plan

Nếu giải pháp này không hoạt động, có thể thử:

1. **Giảm transaction isolation level:**
   - Từ `READ_COMMITTED` → `READ_UNCOMMITTED` (không khuyến khích)

2. **Thêm retry logic:**
   - Retry khi gặp `PessimisticLockingFailureException`

3. **Sử dụng optimistic locking:**
   - Thay vì pessimistic lock, dùng `@Version` field

4. **Queue-based approach:**
   - Save token → Queue email request → Process async

## 📚 References

- [Spring Transaction Management](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#transaction)
- [MySQL Lock Wait Timeout](https://dev.mysql.com/doc/refman/8.0/en/innodb-parameters.html#sysvar_innodb_lock_wait_timeout)
- [Hibernate Pessimistic Locking](https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#locking-pessimistic)

## ✅ Kết Luận

**Nguyên nhân:** Transaction lock timeout khi save token, không phải vấn đề với email service.

**Giải pháp:** Tách transaction để save token và send email riêng biệt, giảm thời gian lock và conflict.

**Kết quả mong đợi:** 
- Token được save thành công
- Email service nhận được request
- Không còn lock timeout

