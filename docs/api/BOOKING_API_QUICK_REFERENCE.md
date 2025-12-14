# 📋 Booking API - Quick Reference

## Base URL
```
http://localhost:8100/api/v1/bookings
```

## Authentication
```
Authorization: Bearer <JWT_TOKEN>
Role Required: CUSTOMER
```

---

## Endpoints Summary

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `POST` | `/api/v1/bookings` | Tạo booking mới |
| `GET` | `/api/v1/bookings` | Lấy danh sách bookings (có pagination & filter) |
| `GET` | `/api/v1/bookings/{id}` | Lấy chi tiết booking |
| `PUT` | `/api/v1/bookings/{id}` | Cập nhật booking (chỉ PENDING) |
| `POST` | `/api/v1/bookings/{id}/cancel` | Hủy booking (chỉ PENDING) |

---

## 1. POST /api/v1/bookings - Tạo Booking

**Request:**
```json
{
  "title": "Sửa chữa máy lạnh",
  "address": "123 Đường ABC, Phường XYZ, Quận 1, TP.HCM",
  "date": "2024-12-25T14:00:00",
  "notes": "Máy lạnh không lạnh",
  "phone": "0901234567",
  "name": "Nguyễn Văn A",
  "wardCode": "27601",
  "needsSurvey": true
}
```

**Required Fields:** `title`, `address`, `date`, `phone`, `name`

**Response:** `201 Created` - BookingDTO

---

## 2. GET /api/v1/bookings - Lấy Danh Sách

**Query Params:**
- `status` (optional): `PENDING` | `COMPLETED` | `CANCELLED`
- `page` (optional, default: 0): Số trang
- `limit` (optional, default: 10, max: 100): Số items/trang

**Examples:**
```
GET /api/v1/bookings
GET /api/v1/bookings?status=PENDING
GET /api/v1/bookings?page=0&limit=20
GET /api/v1/bookings?status=COMPLETED&page=1&limit=15
```

**Response:** `200 OK` - BookingListResponseDTO

---

## 3. GET /api/v1/bookings/{id} - Chi Tiết

**Path:** `{id}` - Booking ID

**Response:** `200 OK` - BookingDTO

**Error:** `404` - Booking không tồn tại hoặc không thuộc về user

---

## 4. PUT /api/v1/bookings/{id} - Cập Nhật

**Request:** (Tất cả fields optional)
```json
{
  "title": "Tên mới",
  "date": "2024-12-26T15:00:00",
  "notes": "Ghi chú mới"
}
```

**Rules:** Chỉ booking có status `PENDING` mới được cập nhật

**Response:** `200 OK` - BookingDTO

**Error:** `400` - Booking không ở trạng thái PENDING

---

## 5. POST /api/v1/bookings/{id}/cancel - Hủy

**Rules:** Chỉ booking có status `PENDING` mới được hủy

**Response:** `200 OK` - BookingDTO (status = CANCELLED)

**Error:** `400` - Booking không ở trạng thái PENDING

---

## Booking Status

| Status | Mô tả | Có thể Update? | Có thể Cancel? |
|--------|-------|---------------|----------------|
| `PENDING` | Đang chờ | ✅ Yes | ✅ Yes |
| `COMPLETED` | Đã hoàn thành | ❌ No | ❌ No |
| `CANCELLED` | Đã hủy | ❌ No | ❌ No |

---

## Response Format

**Success:**
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2024-12-20T10:30:00"
}
```

**Error:**
```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2024-12-20T10:30:00"
}
```

---

## Common Errors

| Status | Message | Nguyên nhân |
|--------|---------|-------------|
| `400` | "Cannot update booking with status: COMPLETED" | Cố gắng update booking không phải PENDING |
| `404` | "Booking ID X not found for user ID Y" | Booking không tồn tại hoặc không thuộc về user |
| `401` | "Unauthorized" | Thiếu hoặc JWT token không hợp lệ |
| `403` | "Forbidden" | User không có role CUSTOMER |

---

## cURL Examples

**Create:**
```bash
curl -X POST http://localhost:8100/api/v1/bookings \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test","address":"123 ABC","date":"2024-12-25T14:00:00","phone":"0901234567","name":"Test User"}'
```

**List:**
```bash
curl -X GET "http://localhost:8100/api/v1/bookings?status=PENDING&page=0&limit=10" \
  -H "Authorization: Bearer TOKEN"
```

**Get Details:**
```bash
curl -X GET http://localhost:8100/api/v1/bookings/1 \
  -H "Authorization: Bearer TOKEN"
```

**Update:**
```bash
curl -X PUT http://localhost:8100/api/v1/bookings/1 \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"date":"2024-12-26T15:00:00"}'
```

**Cancel:**
```bash
curl -X POST http://localhost:8100/api/v1/bookings/1/cancel \
  -H "Authorization: Bearer TOKEN"
```

---

## Date Format

ISO8601: `YYYY-MM-DDTHH:mm:ss`

Example: `2024-12-25T14:00:00`

---

Xem tài liệu đầy đủ tại: [`BOOKING_API_DOCUMENTATION.md`](./BOOKING_API_DOCUMENTATION.md)

