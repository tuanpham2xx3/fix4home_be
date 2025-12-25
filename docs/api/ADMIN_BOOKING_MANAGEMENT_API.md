## Admin Booking Management API

### 1. Tổng quan

- **Mục đích**: Cho phép admin:
  - **Xem danh sách tất cả booking** (mọi user), có phân trang và lọc trạng thái.
  - **Xem chi tiết một booking bất kỳ**.
  - **Cập nhật trạng thái booking** (ví dụ: PENDING → COMPLETED / CANCELLED).
- **Yêu cầu quyền**: Tài khoản phải có role **ADMIN**.
- **Authentication**: Sử dụng JWT Bearer Token:
  - **Header**: `Authorization: Bearer <access_token>`

---

### 2. Lấy danh sách tất cả booking (admin)

- **Method**: `GET`
- **URL**: `/api/v1/bookings/admin`
- **Mô tả**: Admin lấy danh sách tất cả booking của mọi user, có phân trang và lọc theo trạng thái.

- **Header bắt buộc**:
  - `Authorization: Bearer <admin_access_token>`

- **Query params**:
  - **status** (optional, string):
    - Giá trị hợp lệ: `PENDING`, `COMPLETED`, `CANCELLED`, ...
    - Nếu không truyền: lấy tất cả trạng thái.
  - **page** (optional, int):
    - Số trang, bắt đầu từ `0`.
    - Nếu không truyền: mặc định `0`.
  - **limit** (optional, int):
    - Số bản ghi mỗi trang.
    - Mặc định `10`, tối đa `100`.

- **Response 200** (`application/json`):

{
  "success": true,
  "message": "Bookings retrieved successfully",
  "data": {
    "bookings": [
      {
        "id": 1,
        "title": "Sửa điều hòa",
        "address": "123 Nguyễn Trãi, Hà Nội",
        "date": "2025-12-22T09:00:00",
        "notes": "Kiểm tra gas",
        "phone": "0901234567",
        "name": "Nguyễn Văn A",
        "wardCode": "010101",
        "needsSurvey": false,
        "status": "PENDING",
        "createdAt": "2025-12-20T10:00:00",
        "updatedAt": "2025-12-20T10:00:00"
      }
    ],
    "total": 1,
    "page": 0,
    "limit": 10
  }
}- **Response 401/403**:
  - 401: Thiếu hoặc token không hợp lệ.
  - 403: User không có role ADMIN.

---

### 3. Xem chi tiết booking (admin)

- **Method**: `GET`
- **URL**: `/api/v1/bookings/admin/{id}`
- **Mô tả**: Admin xem chi tiết một booking bất kỳ (không phụ thuộc user nào).

- **Path params**:
  - **id** (required, long): ID của booking.

- **Header bắt buộc**:
  - `Authorization: Bearer <admin_access_token>`

- **Response 200**:

{
  "success": true,
  "message": "Booking details retrieved successfully",
  "data": {
    "id": 1,
    "title": "Sửa điều hòa",
    "address": "123 Nguyễn Trãi, Hà Nội",
    "date": "2025-12-22T09:00:00",
    "notes": "Kiểm tra gas",
    "phone": "0901234567",
    "name": "Nguyễn Văn A",
    "wardCode": "010101",
    "needsSurvey": false,
    "status": "PENDING",
    "createdAt": "2025-12-20T10:00:00",
    "updatedAt": "2025-12-20T10:00:00"
  }
}- **Response 404**:
  - Khi booking không tồn tại:
  - Message code: `BOOKING_NOT_FOUND`.

---

### 4. Admin cập nhật trạng thái booking

- **Method**: `PATCH`
- **URL**: `/api/v1/bookings/admin/{id}/status`
- **Mô tả**: Admin cập nhật trạng thái booking.

- **Ràng buộc nghiệp vụ (hiện tại)**:
  - Chỉ các booking có trạng thái **PENDING** mới được phép cập nhật.
  - Nếu booking không ở trạng thái `PENDING` → trả lỗi `IllegalStateException` với thông báo:
    - `"Only bookings with PENDING status can be updated by admin. Current status: ..."`

- **Path params**:
  - **id** (required, long): ID của booking.

- **Header bắt buộc**:
  - `Authorization: Bearer <admin_access_token>`
  - `Content-Type: application/json`

- **Request body** (`UpdateBookingStatusRequest`):

{
  "status": "COMPLETED",
  "note": "Kỹ thuật viên đã hoàn thành dịch vụ"
}- **Fields**:
  - **status** (required, string):
    - Enum `BookingStatus`, ví dụ: `PENDING`, `COMPLETED`, `CANCELLED`, ...
  - **note** (optional, string):
    - Ghi chú lý do hoặc thông tin thêm (chưa được lưu trong entity, nhưng sẵn sàng mở rộng).

- **Response 200**:

{
  "success": true,
  "message": "Booking status updated successfully",
  "data": {
    "id": 1,
    "title": "Sửa điều hòa",
    "address": "123 Nguyễn Trãi, Hà Nội",
    "date": "2025-12-22T09:00:00",
    "notes": "Kiểm tra gas",
    "phone": "0901234567",
    "name": "Nguyễn Văn A",
    "wardCode": "010101",
    "needsSurvey": false,
    "status": "COMPLETED",
    "createdAt": "2025-12-20T10:00:00",
    "updatedAt": "2025-12-22T11:00:00"
  }
}- **Response 400**:
  - Khi `status` null hoặc không hợp lệ (vi phạm validation).

- **Response 404**:
  - Khi booking không tồn tại (`BOOKING_NOT_FOUND`).

- **Response 409 (đề xuất mapping)**:
  - Khi cố cập nhật booking không ở trạng thái `PENDING` (xử lý `IllegalStateException` ở global exception handler).

---

### 5. Tóm tắt nhanh cho frontend / tester

- **Lấy danh sách booking (admin)**:
  - `GET /api/v1/bookings/admin`
  - Query: `status`, `page`, `limit`
  - Header: `Authorization: Bearer <admin_token>`

- **Xem chi tiết booking (admin)**:
  - `GET /api/v1/bookings/admin/{id}`

- **Cập nhật trạng thái booking (admin)**:
  - `PATCH /api/v1/bookings/admin/{id}/status`
  - Body: `{"status": "<NEW_STATUS>", "note": "..."}`

Tất cả các endpoint trên chỉ dùng được với **tài khoản ADMIN**.