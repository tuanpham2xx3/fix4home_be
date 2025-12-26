# Email Request Hanging on Save - Root Cause và Giải Pháp

## 🔍 Vấn Đề

Khi user đăng ký, code bị **hang** ở phần save activation token, không bao giờ đến được phần gọi email service.

### Bằng Chứng Từ Log

```
2025-12-26 10:43:48.361 [http-nio-0.0.0.0-8100-exec-1] INFO - === Saving activation token: email=niyanime282+224@gmail.com, action=registration, userId=40, isNewToken=true ===
2025-12-26 10:43:48.363 [http-nio-0.0.0.0-8100-exec-1] DEBUG [org.hibernate.SQL] - [SQL query logged]
```

**Không có log tiếp theo:**
- ❌ Không có "Token saved successfully"
- ❌ Không có "About to call emailVerificationService.sendActivationLink"
- ❌ Không có "START sendActivationLink"

## 🎯 Nguyên Nhân

Code bị **hang** tại `activationTokenRepository.save(token)` do:

1. **Transaction Lock Timeout**: Transaction đang chờ lock từ transaction khác
2. **No Exception Thrown**: Code không throw exception, chỉ đơn giản là **hang** và chờ
3. **No Timeout Configured**: Transaction không có timeout, nên có thể chờ vô hạn

## ✅ Giải Pháp Đã Áp Dụng

### 1. Thêm Transaction Timeout

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, 
               timeout = 5) // 5 seconds timeout
private TokenSaveResult saveTokenInTransaction(...) {
    // ...
}
```

**Lợi ích:**
- Transaction sẽ timeout sau 5 giây nếu không hoàn thành
- Throw `TransactionTimedOutException` thay vì hang vô hạn

### 2. Thêm Retry Logic với Exponential Backoff

```java
int maxRetries = 3;
int retryCount = 0;
long retryDelayMs = 100; // Start with 100ms delay

while (retryCount < maxRetries) {
    try {
        token = activationTokenRepository.save(token);
        log.info("=== Token saved successfully: tokenId={} ===", token.getId());
        return new TokenSaveResult(token, isNewToken);
    } catch (PessimisticLockingFailureException e) {
        retryCount++;
        if (retryCount >= maxRetries) {
            // Throw exception after max retries
            throw new BusinessValidationException(...);
        }
        // Exponential backoff: 100ms, 200ms, 400ms
        Thread.sleep(retryDelayMs);
        retryDelayMs *= 2;
    }
}
```

**Lợi ích:**
- Tự động retry khi gặp lock timeout
- Exponential backoff giảm conflict với các request khác
- Throw exception rõ ràng sau khi retry hết

### 3. Cải Thiện Error Handling

- Catch `TransactionTimedOutException` riêng
- Log chi tiết cho mỗi retry attempt
- Throw message rõ ràng cho user

## 📊 Flow Sau Khi Fix

```
1. User đăng ký
2. generateActivationToken() được gọi
3. saveTokenInTransaction() được gọi (transaction riêng, timeout 5s)
4. Attempt 1: save() → Nếu lock timeout → Retry sau 100ms
5. Attempt 2: save() → Nếu lock timeout → Retry sau 200ms
6. Attempt 3: save() → Nếu lock timeout → Throw exception
7. Nếu save thành công → Log "Token saved successfully"
8. Gọi emailVerificationService.sendActivationLink()
9. Email service nhận được request ✅
```

## 🧪 Testing

### Test Case 1: Normal Flow
- ✅ User đăng ký lần đầu → Token được save → Email được gửi

### Test Case 2: Concurrent Requests
- ✅ 2 requests cùng lúc cho cùng email → 1 thành công, 1 retry và thành công
- ✅ Không còn hang

### Test Case 3: Lock Timeout
- ✅ Nếu lock timeout → Retry 3 lần → Throw exception rõ ràng
- ✅ Không còn hang vô hạn

## 📝 Logs Để Theo Dõi

### Success Case:
```
INFO - === Saving activation token: email=..., action=..., userId=..., isNewToken=... ===
DEBUG - Attempt 1 to save token for email: ...
INFO - === Token saved successfully: tokenId=... ===
INFO - === About to call emailVerificationService.sendActivationLink: email=..., action=..., userId=... ===
INFO - === START sendActivationLink: email=..., action=..., userId=... ===
```

### Retry Case:
```
INFO - === Saving activation token: email=..., action=..., userId=..., isNewToken=... ===
DEBUG - Attempt 1 to save token for email: ...
WARN - ⚠️ PessimisticLockingFailureException on attempt 1 for email: .... Retrying in 100ms...
DEBUG - Attempt 2 to save token for email: ...
INFO - === Token saved successfully: tokenId=... ===
```

### Failure Case:
```
INFO - === Saving activation token: email=..., action=..., userId=..., isNewToken=... ===
DEBUG - Attempt 1 to save token for email: ...
WARN - ⚠️ PessimisticLockingFailureException on attempt 1 for email: .... Retrying in 100ms...
DEBUG - Attempt 2 to save token for email: ...
WARN - ⚠️ PessimisticLockingFailureException on attempt 2 for email: .... Retrying in 200ms...
DEBUG - Attempt 3 to save token for email: ...
ERROR - ⚠️ PessimisticLockingFailureException after 3 retries when saving token for email: ...
```

## 🔄 Rollback Plan

Nếu giải pháp này không hoạt động, có thể thử:

1. **Tăng timeout:**
   - Từ 5 giây → 10 giây

2. **Tăng số lần retry:**
   - Từ 3 lần → 5 lần

3. **Sử dụng optimistic locking:**
   - Thay vì pessimistic lock, dùng `@Version` field

4. **Queue-based approach:**
   - Save token → Queue email request → Process async

5. **Database-level fix:**
   - Tăng `innodb_lock_wait_timeout` trong MySQL
   - Kiểm tra và fix deadlock

## 📚 References

- [Spring Transaction Timeout](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#transaction-declarative-annotations)
- [MySQL Lock Wait Timeout](https://dev.mysql.com/doc/refman/8.0/en/innodb-parameters.html#sysvar_innodb_lock_wait_timeout)
- [Hibernate Pessimistic Locking](https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#locking-pessimistic)

## ✅ Kết Luận

**Nguyên nhân:** Code bị hang ở `save()` do transaction lock timeout, không throw exception.

**Giải pháp:** 
- Thêm transaction timeout (5 giây)
- Thêm retry logic với exponential backoff (3 lần)
- Cải thiện error handling

**Kết quả mong đợi:**
- Token được save thành công (hoặc throw exception rõ ràng sau retry)
- Email service nhận được request
- Không còn hang vô hạn

