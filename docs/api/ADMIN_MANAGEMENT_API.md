# 🔧 Admin Management API - Hướng Dẫn Sử Dụng

## 📌 Tổng Quan

API Admin Management cho phép quản trị viên quản lý bookings và users trong hệ thống Fix4Home.

**Base URL:** `http://localhost:8100/api/v1/admin`

**Version:** v1

**Content-Type:** `application/json`

**Authentication:** JWT Bearer Token (Required - ADMIN role)

---

## 🔐 Authentication

Tất cả các endpoint đều yêu cầu JWT token trong header:

```http
Authorization: Bearer <admin-jwt-token>
```

**Yêu cầu Role:** `ADMIN` - Chỉ quản trị viên mới có thể truy cập các endpoint này.

---

## 📊 Booking Status

Booking có 3 trạng thái:

| Status | Mô tả |
|--------|-------|
| `PENDING` | Đơn đặt đang chờ xử lý |
| `COMPLETED` | Đơn đặt đã hoàn thành |
| `CANCELLED` | Đơn đặt đã bị hủy |

---

## 👤 User Status

User có các trạng thái:

| Status | Mô tả |
|--------|-------|
| `ACTIVE` | Tài khoản đang hoạt động |
| `INACTIVE` | Tài khoản đã bị vô hiệu hóa |
| `PENDING_APPROVAL` | Đang chờ phê duyệt |
| `REJECTED` | Đã bị từ chối |
| `PENDING_EMAIL_VERIFICATION` | Đang chờ xác thực email |

---

## 🚀 API Endpoints

### 📋 BOOKING MANAGEMENT

#### 1. Lấy Danh Sách Tất Cả Bookings

**Endpoint:** `GET /api/v1/admin/bookings`

**Mô tả:** Admin xem danh sách tất cả bookings của mọi user với pagination, sorting và filtering.

**Query Parameters:**

| Parameter | Type | Required | Default | Mô tả |
|-----------|------|----------|---------|-------|
| `page` | Integer | ❌ No | `0` | Số trang (0-based) |
| `size` | Integer | ❌ No | `10` | Số bản ghi mỗi trang |
| `sortBy` | String | ❌ No | `createdAt` | Field để sort |
| `sortDir` | String | ❌ No | `desc` | Hướng sort (`asc` hoặc `desc`) |
| `status` | BookingStatus | ❌ No | - | Lọc theo trạng thái (`PENDING`, `COMPLETED`, `CANCELLED`) |

**Request Example:**
```http
GET /api/v1/admin/bookings?page=0&size=20&sortBy=createdAt&sortDir=desc&status=PENDING
Authorization: Bearer <admin-token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Bookings retrieved successfully",
  "data": {
    "bookings": [
      {
        "id": 1,
        "title": "Sửa chữa máy lạnh",
        "address": "123 Đường ABC, Phường XYZ, Quận 1, TP.HCM",
        "date": "2024-12-25T14:00:00",
        "notes": "Máy lạnh không lạnh",
        "phone": "0901234567",
        "name": "Nguyễn Văn A",
        "wardCode": "27601",
        "needsSurvey": true,
        "status": "PENDING",
        "userId": 5,
        "createdAt": "2024-12-20T10:30:00",
        "updatedAt": "2024-12-20T10:30:00"
      }
    ],
    "total": 1,
    "page": 0,
    "limit": 20
  },
  "timestamp": "2024-12-20T10:30:00"
}
```

**Response Fields:**

| Field | Type | Mô tả |
|-------|------|-------|
| `bookings` | Array | Danh sách bookings |
| `total` | Long | Tổng số bookings |
| `page` | Integer | Số trang hiện tại |
| `limit` | Integer | Số bản ghi mỗi trang |

**BookingDTO Fields:**

| Field | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID của booking |
| `title` | String | Tiêu đề dịch vụ |
| `address` | String | Địa chỉ |
| `date` | DateTime | Thời gian hẹn |
| `notes` | String | Ghi chú |
| `phone` | String | Số điện thoại |
| `name` | String | Tên khách hàng |
| `wardCode` | String | Mã phường/xã |
| `needsSurvey` | Boolean | Cần khảo sát |
| `status` | BookingStatus | Trạng thái booking |
| `userId` | Long | ID của user sở hữu booking (chỉ có trong admin view) |
| `createdAt` | DateTime | Thời gian tạo |
| `updatedAt` | DateTime | Thời gian cập nhật |

**Error Responses:**

- **401 Unauthorized:** Token không hợp lệ hoặc thiếu
- **403 Forbidden:** User không có quyền ADMIN

---

#### 2. Xem Chi Tiết Booking

**Endpoint:** `GET /api/v1/admin/bookings/{id}`

**Mô tả:** Admin xem chi tiết một booking bất kỳ (không phụ thuộc user nào).

**Path Parameters:**

| Parameter | Type | Required | Mô tả |
|-----------|------|----------|-------|
| `id` | Long | ✅ Yes | ID của booking |

**Request Example:**
```http
GET /api/v1/admin/bookings/1
Authorization: Bearer <admin-token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Booking details retrieved successfully",
  "data": {
    "id": 1,
    "title": "Sửa chữa máy lạnh",
    "address": "123 Đường ABC, Phường XYZ, Quận 1, TP.HCM",
    "date": "2024-12-25T14:00:00",
    "notes": "Máy lạnh không lạnh",
    "phone": "0901234567",
    "name": "Nguyễn Văn A",
    "wardCode": "27601",
    "needsSurvey": true,
    "status": "PENDING",
    "userId": 5,
    "createdAt": "2024-12-20T10:30:00",
    "updatedAt": "2024-12-20T10:30:00"
  },
  "timestamp": "2024-12-20T10:30:00"
}
```

**Error Responses:**

- **401 Unauthorized:** Token không hợp lệ hoặc thiếu
- **403 Forbidden:** User không có quyền ADMIN
- **404 Not Found:** Booking không tồn tại

---

#### 3. Cập Nhật Trạng Thái Booking

**Endpoint:** `PUT /api/v1/admin/bookings/{id}/status`

**Mô tả:** Admin cập nhật trạng thái booking.

**Path Parameters:**

| Parameter | Type | Required | Mô tả |
|-----------|------|----------|-------|
| `id` | Long | ✅ Yes | ID của booking |

**Request Body:**

```json
{
  "status": "COMPLETED",
  "note": "Kỹ thuật viên đã hoàn thành dịch vụ"
}
```

**Request Fields:**

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `status` | BookingStatus | ✅ Yes | Trạng thái mới (`PENDING`, `COMPLETED`, `CANCELLED`) |
| `note` | String | ❌ No | Ghi chú lý do thay đổi (optional) |

**Request Example:**
```http
PUT /api/v1/admin/bookings/1/status
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "status": "COMPLETED",
  "note": "Dịch vụ đã hoàn thành thành công"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Booking status updated successfully",
  "data": {
    "id": 1,
    "title": "Sửa chữa máy lạnh",
    "address": "123 Đường ABC, Phường XYZ, Quận 1, TP.HCM",
    "date": "2024-12-25T14:00:00",
    "notes": "Máy lạnh không lạnh",
    "phone": "0901234567",
    "name": "Nguyễn Văn A",
    "wardCode": "27601",
    "needsSurvey": true,
    "status": "COMPLETED",
    "userId": 5,
    "createdAt": "2024-12-20T10:30:00",
    "updatedAt": "2024-12-22T11:00:00"
  },
  "timestamp": "2024-12-22T11:00:00"
}
```

**Error Responses:**

- **400 Bad Request:** Request body không hợp lệ (thiếu status hoặc status không hợp lệ)
- **401 Unauthorized:** Token không hợp lệ hoặc thiếu
- **403 Forbidden:** User không có quyền ADMIN
- **404 Not Found:** Booking không tồn tại

---

### 👥 USER MANAGEMENT

#### 4. Lấy Danh Sách Tất Cả Users

**Endpoint:** `GET /api/v1/admin/users`

**Mô tả:** Admin xem danh sách tất cả users với pagination, sorting và role filtering.

**Query Parameters:**

| Parameter | Type | Required | Default | Mô tả |
|-----------|------|----------|---------|-------|
| `page` | Integer | ❌ No | `0` | Số trang (0-based) |
| `size` | Integer | ❌ No | `10` | Số bản ghi mỗi trang |
| `sortBy` | String | ❌ No | `createdAt` | Field để sort |
| `sortDir` | String | ❌ No | `desc` | Hướng sort (`asc` hoặc `desc`) |
| `role` | Role | ❌ No | - | Lọc theo role (`CUSTOMER`, `TECHNICIAN`, `ADMIN`) |

**Request Example:**
```http
GET /api/v1/admin/users?page=0&size=20&sortBy=createdAt&sortDir=desc&role=CUSTOMER
Authorization: Bearer <admin-token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "username": "customer1",
        "email": "customer1@example.com",
        "phoneNumber": "0901234567",
        "role": "CUSTOMER",
        "status": "ACTIVE",
        "createdAt": "2024-12-20T10:30:00",
        "lastLoginAt": null,
        "fullName": "Nguyễn Văn A",
        "profileStatus": "ACTIVE",
        "totalServiceRequests": 5,
        "completedServiceRequests": 3,
        "totalAddresses": 2
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0
  },
  "timestamp": "2024-12-20T10:30:00"
}
```

**UserManagementDTO Fields:**

| Field | Type | Mô tả |
|-------|------|-------|
| `id` | Long | ID của user |
| `username` | String | Tên đăng nhập |
| `email` | String | Email |
| `phoneNumber` | String | Số điện thoại |
| `role` | Role | Vai trò (`CUSTOMER`, `TECHNICIAN`, `ADMIN`) |
| `status` | UserStatus | Trạng thái user |
| `createdAt` | DateTime | Thời gian tạo |
| `lastLoginAt` | DateTime | Lần đăng nhập cuối |
| `fullName` | String | Họ tên đầy đủ |
| `profileStatus` | String | Trạng thái profile |
| `totalServiceRequests` | Long | Tổng số service requests |
| `completedServiceRequests` | Long | Số service requests đã hoàn thành |
| `totalAddresses` | Long | Tổng số địa chỉ (cho CUSTOMER) |
| `rating` | Float | Đánh giá (cho TECHNICIAN) |
| `isApproved` | Boolean | Đã được phê duyệt (cho TECHNICIAN) |

**Error Responses:**

- **401 Unauthorized:** Token không hợp lệ hoặc thiếu
- **403 Forbidden:** User không có quyền ADMIN

---

#### 5. Xem Chi Tiết User

**Endpoint:** `GET /api/v1/admin/users/{userId}`

**Mô tả:** Admin xem chi tiết một user.

**Path Parameters:**

| Parameter | Type | Required | Mô tả |
|-----------|------|----------|-------|
| `userId` | Long | ✅ Yes | ID của user |

**Request Example:**
```http
GET /api/v1/admin/users/1
Authorization: Bearer <admin-token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "User details retrieved successfully",
  "data": {
    "id": 1,
    "username": "customer1",
    "email": "customer1@example.com",
    "phoneNumber": "0901234567",
    "role": "CUSTOMER",
    "status": "ACTIVE",
    "createdAt": "2024-12-20T10:30:00",
    "lastLoginAt": null,
    "fullName": "Nguyễn Văn A",
    "profileStatus": "ACTIVE",
    "totalServiceRequests": 5,
    "completedServiceRequests": 3,
    "totalAddresses": 2
  },
  "timestamp": "2024-12-20T10:30:00"
}
```

**Error Responses:**

- **401 Unauthorized:** Token không hợp lệ hoặc thiếu
- **403 Forbidden:** User không có quyền ADMIN
- **404 Not Found:** User không tồn tại

---

#### 6. Cập Nhật Trạng Thái User

**Endpoint:** `PUT /api/v1/admin/users/{userId}/status`

**Mô tả:** Admin cập nhật trạng thái user.

**Path Parameters:**

| Parameter | Type | Required | Mô tả |
|-----------|------|----------|-------|
| `userId` | Long | ✅ Yes | ID của user |

**Request Body:**

```json
{
  "status": "INACTIVE",
  "reason": "Vi phạm quy định sử dụng"
}
```

**Request Fields:**

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `status` | UserStatus | ✅ Yes | Trạng thái mới (`ACTIVE`, `INACTIVE`, `PENDING_APPROVAL`, `REJECTED`, `PENDING_EMAIL_VERIFICATION`) |
| `reason` | String | ❌ No | Lý do thay đổi (optional) |

**Request Example:**
```http
PUT /api/v1/admin/users/1/status
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "status": "INACTIVE",
  "reason": "Tài khoản vi phạm quy định"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "User status updated successfully",
  "data": {
    "id": 1,
    "username": "customer1",
    "email": "customer1@example.com",
    "phoneNumber": "0901234567",
    "role": "CUSTOMER",
    "status": "INACTIVE",
    "createdAt": "2024-12-20T10:30:00",
    "lastLoginAt": null,
    "fullName": "Nguyễn Văn A",
    "profileStatus": "INACTIVE",
    "totalServiceRequests": 5,
    "completedServiceRequests": 3,
    "totalAddresses": 2
  },
  "timestamp": "2024-12-20T10:30:00"
}
```

**Error Responses:**

- **400 Bad Request:** Request body không hợp lệ (thiếu status hoặc status không hợp lệ)
- **401 Unauthorized:** Token không hợp lệ hoặc thiếu
- **403 Forbidden:** User không có quyền ADMIN
- **404 Not Found:** User không tồn tại

---

## 📝 Tóm Tắt Nhanh

### Booking Management

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `GET` | `/api/v1/admin/bookings` | Lấy danh sách tất cả bookings |
| `GET` | `/api/v1/admin/bookings/{id}` | Xem chi tiết booking |
| `PUT` | `/api/v1/admin/bookings/{id}/status` | Cập nhật trạng thái booking |

### User Management

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `GET` | `/api/v1/admin/users` | Lấy danh sách tất cả users |
| `GET` | `/api/v1/admin/users/{userId}` | Xem chi tiết user |
| `PUT` | `/api/v1/admin/users/{userId}/status` | Cập nhật trạng thái user |

---

## 🔍 Examples

### Example 1: Lấy danh sách bookings đang pending

```bash
curl -X GET "http://localhost:8100/api/v1/admin/bookings?status=PENDING&page=0&size=10" \
  -H "Authorization: Bearer <admin-token>"
```

### Example 2: Cập nhật booking status thành COMPLETED

```bash
curl -X PUT "http://localhost:8100/api/v1/admin/bookings/1/status" \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "COMPLETED",
    "note": "Dịch vụ đã hoàn thành"
  }'
```

### Example 3: Lấy danh sách users theo role CUSTOMER

```bash
curl -X GET "http://localhost:8100/api/v1/admin/users?role=CUSTOMER&page=0&size=20" \
  -H "Authorization: Bearer <admin-token>"
```

### Example 4: Vô hiệu hóa user

```bash
curl -X PUT "http://localhost:8100/api/v1/admin/users/1/status" \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "INACTIVE",
    "reason": "Vi phạm quy định"
  }'
```

---

## ⚠️ Lưu Ý

1. **Authentication:** Tất cả endpoints đều yêu cầu JWT token với role ADMIN
2. **Pagination:** Mặc định page=0, size=10. Có thể điều chỉnh theo nhu cầu
3. **Sorting:** Mặc định sort theo `createdAt DESC`. Có thể thay đổi `sortBy` và `sortDir`
4. **Filtering:** Có thể lọc bookings theo `status`, users theo `role`
5. **Validation:** Tất cả request đều được validate. Nếu không hợp lệ sẽ trả về 400 Bad Request
6. **Error Handling:** Hệ thống trả về error messages rõ ràng trong response body

---

## 🔗 Related Documentation

- [Booking API Documentation](./BOOKING_API_DOCUMENTATION.md) - API cho customer
- [API Complete Documentation](./API_COMPLETE_DOCUMENTATION.md) - Tổng hợp tất cả APIs

