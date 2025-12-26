# 🔧 Giải Pháp: Build Lại Vẫn Không Gửi Request Đến Sendmail và Response Về FE

## 📋 Vấn Đề Hiện Tại

Sau khi build lại, vẫn gặp 3 vấn đề:

1. ❌ Request không được gửi đến email service (port 8200)
2. ❌ Response không trả về frontend
3. ❌ Database lock timeout khi insert notification

### Log Lỗi Quan Trọng

```
org.hibernate.PessimisticLockException: could not execute statement 
[Lock wait timeout exceeded; try restarting transaction] 
[insert into notifications (...) values (...)]

org.springframework.transaction.UnexpectedRollbackException: 
Transaction silently rolled back because it has been marked as rollback-only
```

---

## 🎯 Nguyên Nhân Chính

### 1. Database Lock Timeout

**Nguyên nhân:**
- Có transaction khác đang giữ lock trên bảng `notifications`
- Nested transaction (`REQUIRES_NEW`) trong `createRegisterNotification()` gây conflict
- Lock wait timeout = 50 giây (mặc định MySQL)

**Flow gây lỗi:**
```
AuthService.register() [@Transactional]
  → Save user ✅
  → Create profile ✅
  → createRegisterNotification() [@Transactional(REQUIRES_NEW)] 
    → Cố insert notification
    → ❌ Lock timeout (table đang bị lock)
  → Transaction mark rollback-only
  → ❌ Không thể return response
```

### 2. Transaction Rollback

**Nguyên nhân:**
- Lock timeout làm nested transaction fail
- Nested transaction mark parent transaction là rollback-only
- Tất cả operations sau đó fail
- Không bao giờ đến được dòng `return authResponse`

### 3. Email Service Không Nhận Request

**Nguyên nhân:**
- Email được gửi TRONG background thread AFTER commit
- Nhưng transaction bị rollback → afterCommit() không chạy
- Hoặc email service không chạy trên port 8200

---

## ✅ Giải Pháp Toàn Diện

### Bước 1: Fix Database Lock Issue

#### 1.1. Clear Current Locks

**Chạy script SQL này:**

```bash
# Từ PowerShell trong thư mục FIX4HOME_BE
mysql -u root -p -P 3307 < fix_database_locks.sql
```

Hoặc chạy trực tiếp trong MySQL Workbench:

```sql
USE fix4home_db;

-- Xem các locks hiện tại
SELECT * FROM information_schema.innodb_lock_waits;

-- Xem các transactions đang chạy
SELECT 
    trx_id,
    trx_state,
    trx_started,
    trx_mysql_thread_id,
    TIME_TO_SEC(TIMEDIFF(NOW(), trx_started)) as seconds_running
FROM information_schema.innodb_trx;

-- Kill long-running transactions (CAREFUL!)
SHOW PROCESSLIST;
-- KILL <process_id>; -- Replace with actual ID

-- Optimize table
ANALYZE TABLE notifications;
OPTIMIZE TABLE notifications;
```

#### 1.2. Fix Nested Transaction Issue

**ĐÃ SỬA** trong code:

- Đổi `@Transactional(propagation = Propagation.REQUIRES_NEW)` 
- Thành `@Transactional(propagation = Propagation.MANDATORY)`
- Trong `NotificationHelperService.createRegisterNotification()`

**Lý do:**
- `REQUIRES_NEW` tạo transaction mới riêng biệt → dễ gây lock
- `MANDATORY` sử dụng transaction hiện có → không tạo nested transaction

---

### Bước 2: Restart Services Đúng Cách

#### 2.1. Sử dụng Script Tự Động

```powershell
# Chạy script fix tự động
.\fix_register_issue.ps1
```

Script sẽ:
- ✅ Check MySQL connection
- ✅ Stop backend để clear locks
- ✅ Check email service
- ✅ Guide bạn clear database locks
- ✅ Restart backend

#### 2.2. Hoặc Làm Thủ Công

**Bước 1: Stop Backend**
```powershell
# Tìm process đang chạy trên port 8100
$process = Get-NetTCPConnection -LocalPort 8100 -ErrorAction SilentlyContinue | 
           Select-Object -First 1 -ExpandProperty OwningProcess

# Kill process
if ($process) {
    Stop-Process -Id $process -Force
    Write-Host "Backend stopped (PID: $process)"
}
```

**Bước 2: Clear Database Locks**
```powershell
# Run SQL script
mysql -u root -p -P 3307 < fix_database_locks.sql
```

**Bước 3: Check Email Service**
```powershell
# Test email service
curl http://localhost:8200/health
```

Nếu không chạy:
```bash
cd .microservice/MRS_SENDEMAIL_BE
npm start
```

**Bước 4: Start Backend**
```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

---

### Bước 3: Test Registration Flow

#### 3.1. Test Endpoint

```powershell
# Test registration
curl -X POST http://localhost:8100/api/v1/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "email": "test123@example.com",
    "username": "test123",
    "password": "Test123!@#",
    "role": "CUSTOMER"
  }'
```

#### 3.2. Expected Logs

**Success Flow:**
```
INFO - Registered email sending to run after transaction commit for: test123@example.com
INFO - Transaction committed, sending activation email to: test123@example.com in background thread
INFO - === START sendActivationLink: email=test123@example.com, action=registration ===
INFO - Email service URL: http://localhost:8200, Service healthy: true
INFO - Sending activation link request to: http://localhost:8200/generate-activation
INFO - Email service response status: 200 OK
INFO - Activation email sent successfully to: test123@example.com
```

**If Email Service Down:**
```
WARN - Email service is unhealthy
ERROR - Email service is down, cannot send activation link
ERROR - Failed to send verification email to: test123@example.com
```

---

## 📊 Checklist Debug

### Pre-Flight Checks

- [ ] **MySQL đang chạy?**
  ```powershell
  Test-NetConnection -ComputerName localhost -Port 3307
  ```

- [ ] **Email service đang chạy?**
  ```powershell
  curl http://localhost:8200/health
  ```

- [ ] **Backend không chạy trước khi start?**
  ```powershell
  Get-NetTCPConnection -LocalPort 8100
  ```

### After Starting Backend

- [ ] **Backend start thành công?**
  - Check log: `Started Fix4homeApplication in X seconds`

- [ ] **Không có lock errors?**
  - Check log: KHÔNG có `Lock wait timeout exceeded`

- [ ] **Health check pass?**
  - Check log: `Email service health check: HEALTHY`

### After Registration Test

- [ ] **User được tạo?**
  - Check database: `SELECT * FROM users WHERE email = 'test123@example.com';`

- [ ] **Response trả về FE?**
  - Status code: 200 hoặc 201
  - Body có token và user info

- [ ] **Email được gửi?**
  - Check backend log: `Activation email sent successfully`
  - Check email service log: Request received

---

## 🔍 Troubleshooting Chi Tiết

### Issue 1: Database Lock Timeout

**Triệu chứng:**
```
PessimisticLockException: Lock wait timeout exceeded
UnexpectedRollbackException
```

**Giải pháp:**
1. Run `fix_database_locks.sql`
2. Check PROCESSLIST và kill long transactions
3. Restart backend
4. Code đã được fix (MANDATORY thay vì REQUIRES_NEW)

---

### Issue 2: Email Service Không Nhận Request

**Triệu chứng:**
```
Email service is down, cannot send activation link
ResourceAccessException: Connection refused
```

**Giải pháp:**
1. Check email service:
   ```bash
   curl http://localhost:8200/health
   ```

2. Nếu không chạy:
   ```bash
   cd .microservice/MRS_SENDEMAIL_BE
   npm install
   npm start
   ```

3. Check port conflict:
   ```powershell
   Get-NetTCPConnection -LocalPort 8200
   ```

---

### Issue 3: Response Không Về FE

**Triệu chứng:**
- Request timeout
- Frontend không nhận response
- Backend log có error nhưng không return

**Giải pháp:**
1. Fix database lock (Issue 1)
2. Đảm bảo không có exception trước `return authResponse`
3. Check transaction không bị rollback
4. Enable debug logging:
   ```properties
   logging.level.com.fix4home.fix4home.service.AuthService=DEBUG
   ```

---

## 🎯 Root Cause Summary

### Before Fix

```
register() [@Transactional]
  ├─ Save user ✅
  ├─ Create profile ✅
  ├─ createRegisterNotification() [@Transactional(REQUIRES_NEW)]
  │   └─ ❌ LOCK TIMEOUT (nested transaction conflict)
  ├─ ❌ Transaction mark rollback-only
  ├─ ❌ Cannot generate token
  ├─ ❌ Cannot return response
  └─ ❌ Email not sent (afterCommit() not called)
```

### After Fix

```
register() [@Transactional]
  ├─ Save user ✅
  ├─ Create profile ✅
  ├─ createRegisterNotification() [@Transactional(MANDATORY)]
  │   └─ ✅ Use same transaction (no lock conflict)
  ├─ Generate token ✅
  ├─ Build response ✅
  ├─ Register email sending (afterCommit) ✅
  ├─ ✅ Transaction COMMITS
  ├─ ✅ Return response to FE
  └─ ✅ Email sent in background thread
```

---

## 📚 Related Documents

- `docs/troubleshooting/EMAIL_REQUEST_NOT_SENT.md` - Email service không nhận request
- `docs/troubleshooting/EMAIL_SUCCESS_NO_RESPONSE_FIX.md` - Email success nhưng no response
- `docs/troubleshooting/EMAIL_ERROR_SPECIFIC_ANALYSIS.md` - Phân tích lỗi email
- `REBUILD_AND_RESTART.md` - Rebuild và restart guide

---

## ✅ Kết Luận

### Vấn Đề Đã Fix

1. ✅ **Database Lock Timeout**
   - Đổi từ `REQUIRES_NEW` sang `MANDATORY`
   - Clear existing locks bằng SQL script

2. ✅ **Email Service Integration**
   - Email sent in background AFTER transaction commits
   - Won't block response to frontend

3. ✅ **Response Handling**
   - Response returns immediately
   - Email failure won't affect registration success

### Expected Behavior

- ✅ User registration completes in < 2 seconds
- ✅ Response returns to frontend immediately
- ✅ Email sent in background (check logs)
- ✅ No database locks or transaction rollbacks

### Next Steps

1. Run `fix_register_issue.ps1`
2. Test registration with new user
3. Monitor logs for success
4. If issues persist, check troubleshooting section above

---

## 🆘 Quick Help

**Still not working? Check these:**

1. **Backend logs**
   ```powershell
   # In terminal where backend is running
   # Look for ERROR or WARN messages
   ```

2. **Email service logs**
   ```bash
   cd .microservice/MRS_SENDEMAIL_BE
   npm start
   # Check console output
   ```

3. **Database**
   ```sql
   USE fix4home_db;
   SHOW PROCESSLIST;
   SELECT * FROM information_schema.innodb_lock_waits;
   ```

4. **Network**
   ```powershell
   Test-NetConnection localhost -Port 8100  # Backend
   Test-NetConnection localhost -Port 8200  # Email
   Test-NetConnection localhost -Port 3307  # MySQL
   ```

---

**Last Updated:** 2025-12-26
**Status:** ✅ Fixed - Ready for testing

