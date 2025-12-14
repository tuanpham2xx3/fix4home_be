# 📋 Booking/Order Management API - Hướng Dẫn Sử Dụng

## 📌 Tổng Quan

API Booking Management cho phép khách hàng quản lý các đơn đặt dịch vụ (bookings/orders) của mình. Hệ thống thay thế việc lưu trữ local trong SharedPreferences bằng backend persistence.

**Base URL:** `http://localhost:8100/api/v1/bookings`

**Version:** v1

**Content-Type:** `application/json`

**Authentication:** JWT Bearer Token (Required - CUSTOMER role)

---

## 🔐 Authentication

Tất cả các endpoint đều yêu cầu JWT token trong header:

```http
Authorization: Bearer <your-jwt-token>
```

**Yêu cầu Role:** `CUSTOMER` - Chỉ khách hàng mới có thể truy cập các booking của chính họ.

---

## 📊 Booking Status

Booking có 3 trạng thái:

| Status | Mô tả |
|--------|-------|
| `PENDING` | Đơn đặt đang chờ xử lý |
| `COMPLETED` | Đơn đặt đã hoàn thành |
| `CANCELLED` | Đơn đặt đã bị hủy |

**Lưu ý:** Chỉ booking có status `PENDING` mới có thể được cập nhật hoặc hủy.

---

## 🚀 API Endpoints

### 1. Tạo Booking Mới

**Endpoint:** `POST /api/v1/bookings`

**Mô tả:** Tạo một booking/order mới

**Request Body:**
```json
{
  "title": "Sửa chữa máy lạnh",
  "address": "123 Đường ABC, Phường XYZ, Quận 1, TP.HCM",
  "date": "2024-12-25T14:00:00",
  "notes": "Máy lạnh không lạnh, cần kiểm tra gas",
  "phone": "0901234567",
  "name": "Nguyễn Văn A",
  "wardCode": "27601",
  "needsSurvey": true
}
```

**Request Fields:**

| Field | Type | Required | Max Length | Mô tả |
|-------|------|----------|-----------|-------|
| `title` | String | ✅ Yes | 200 | Tên dịch vụ |
| `address` | String | ✅ Yes | - | Địa chỉ đầy đủ |
| `date` | DateTime (ISO8601) | ✅ Yes | - | Thời gian hẹn |
| `notes` | String | ❌ No | 1000 | Ghi chú (optional) |
| `phone` | String | ✅ Yes | 20 | Số điện thoại |
| `name` | String | ✅ Yes | 100 | Tên khách hàng |
| `wardCode` | String | ❌ No | 20 | Mã phường/xã (optional) |
| `needsSurvey` | Boolean | ❌ No | - | Cần khảo sát (optional, default: false) |

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Booking created successfully",
  "data": {
    "id": 1,
    "title": "Sửa chữa máy lạnh",
    "address": "123 Đường ABC, Phường XYZ, Quận 1, TP.HCM",
    "date": "2024-12-25T14:00:00",
    "notes": "Máy lạnh không lạnh, cần kiểm tra gas",
    "phone": "0901234567",
    "name": "Nguyễn Văn A",
    "wardCode": "27601",
    "needsSurvey": true,
    "status": "PENDING",
    "createdAt": "2024-12-20T10:30:00",
    "updatedAt": "2024-12-20T10:30:00"
  },
  "timestamp": "2024-12-20T10:30:00"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8100/api/v1/bookings \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Sửa chữa máy lạnh",
    "address": "123 Đường ABC, Phường XYZ, Quận 1, TP.HCM",
    "date": "2024-12-25T14:00:00",
    "notes": "Máy lạnh không lạnh, cần kiểm tra gas",
    "phone": "0901234567",
    "name": "Nguyễn Văn A",
    "wardCode": "27601",
    "needsSurvey": true
  }'
```

---

### 2. Lấy Danh Sách Bookings

**Endpoint:** `GET /api/v1/bookings`

**Mô tả:** Lấy danh sách bookings của khách hàng với phân trang và lọc theo status

**Query Parameters:**

| Parameter | Type | Required | Default | Mô tả |
|-----------|------|----------|---------|-------|
| `status` | String | ❌ No | - | Lọc theo status: `PENDING`, `COMPLETED`, `CANCELLED` |
| `page` | Integer | ❌ No | 0 | Số trang (0-based) |
| `limit` | Integer | ❌ No | 10 | Số lượng items mỗi trang (max: 100) |

**Request Examples:**

```bash
# Lấy tất cả bookings (trang đầu, 10 items)
GET /api/v1/bookings

# Lấy bookings có status PENDING
GET /api/v1/bookings?status=PENDING

# Lấy bookings với phân trang
GET /api/v1/bookings?page=0&limit=20

# Lấy bookings COMPLETED, trang 2, mỗi trang 15 items
GET /api/v1/bookings?status=COMPLETED&page=1&limit=15
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
        "notes": "Máy lạnh không lạnh, cần kiểm tra gas",
        "phone": "0901234567",
        "name": "Nguyễn Văn A",
        "wardCode": "27601",
        "needsSurvey": true,
        "status": "PENDING",
        "createdAt": "2024-12-20T10:30:00",
        "updatedAt": "2024-12-20T10:30:00"
      },
      {
        "id": 2,
        "title": "Lắp đặt quạt trần",
        "address": "456 Đường DEF, Phường UVW, Quận 2, TP.HCM",
        "date": "2024-12-26T09:00:00",
        "notes": null,
        "phone": "0907654321",
        "name": "Trần Thị B",
        "wardCode": "27602",
        "needsSurvey": false,
        "status": "COMPLETED",
        "createdAt": "2024-12-19T08:15:00",
        "updatedAt": "2024-12-26T10:00:00"
      }
    ],
    "total": 2,
    "page": 0,
    "limit": 10
  },
  "timestamp": "2024-12-20T10:35:00"
}
```

**cURL Example:**
```bash
curl -X GET "http://localhost:8100/api/v1/bookings?status=PENDING&page=0&limit=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3. Lấy Chi Tiết Booking

**Endpoint:** `GET /api/v1/bookings/{id}`

**Mô tả:** Lấy thông tin chi tiết của một booking cụ thể

**Path Parameters:**

| Parameter | Type | Required | Mô tả |
|-----------|------|----------|-------|
| `id` | Long | ✅ Yes | ID của booking |

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
    "notes": "Máy lạnh không lạnh, cần kiểm tra gas",
    "phone": "0901234567",
    "name": "Nguyễn Văn A",
    "wardCode": "27601",
    "needsSurvey": true,
    "status": "PENDING",
    "createdAt": "2024-12-20T10:30:00",
    "updatedAt": "2024-12-20T10:30:00"
  },
  "timestamp": "2024-12-20T10:40:00"
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8100/api/v1/bookings/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Error Response (404 Not Found):**
```json
{
  "success": false,
  "message": "Booking ID 1 not found for user ID 123",
  "data": null,
  "timestamp": "2024-12-20T10:40:00"
}
```

---

### 4. Cập Nhật Booking

**Endpoint:** `PUT /api/v1/bookings/{id}`

**Mô tả:** Cập nhật thông tin booking (chỉ áp dụng cho booking có status `PENDING`)

**Path Parameters:**

| Parameter | Type | Required | Mô tả |
|-----------|------|----------|-------|
| `id` | Long | ✅ Yes | ID của booking |

**Request Body:** (Tất cả các fields đều optional - chỉ gửi fields cần cập nhật)

```json
{
  "title": "Sửa chữa máy lạnh - Cập nhật",
  "address": "123 Đường ABC, Phường XYZ, Quận 1, TP.HCM - Tầng 5",
  "date": "2024-12-26T15:00:00",
  "notes": "Đã kiểm tra, cần thay gas",
  "phone": "0901234567",
  "name": "Nguyễn Văn A",
  "wardCode": "27601",
  "needsSurvey": false
}
```

**Request Fields:** (Tất cả optional)

| Field | Type | Max Length | Mô tả |
|-------|------|-----------|-------|
| `title` | String | 200 | Tên dịch vụ |
| `address` | String | - | Địa chỉ đầy đủ |
| `date` | DateTime (ISO8601) | - | Thời gian hẹn |
| `notes` | String | 1000 | Ghi chú |
| `phone` | String | 20 | Số điện thoại |
| `name` | String | 100 | Tên khách hàng |
| `wardCode` | String | 20 | Mã phường/xã |
| `needsSurvey` | Boolean | - | Cần khảo sát |

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Booking updated successfully",
  "data": {
    "id": 1,
    "title": "Sửa chữa máy lạnh - Cập nhật",
    "address": "123 Đường ABC, Phường XYZ, Quận 1, TP.HCM - Tầng 5",
    "date": "2024-12-26T15:00:00",
    "notes": "Đã kiểm tra, cần thay gas",
    "phone": "0901234567",
    "name": "Nguyễn Văn A",
    "wardCode": "27601",
    "needsSurvey": false,
    "status": "PENDING",
    "createdAt": "2024-12-20T10:30:00",
    "updatedAt": "2024-12-20T11:00:00"
  },
  "timestamp": "2024-12-20T11:00:00"
}
```

**cURL Example:**
```bash
curl -X PUT http://localhost:8100/api/v1/bookings/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-12-26T15:00:00",
    "notes": "Đã kiểm tra, cần thay gas"
  }'
```

**Error Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Cannot update booking with status: COMPLETED",
  "data": null,
  "timestamp": "2024-12-20T11:00:00"
}
```

---

### 5. Hủy Booking

**Endpoint:** `POST /api/v1/bookings/{id}/cancel`

**Mô tả:** Hủy một booking (chỉ áp dụng cho booking có status `PENDING`)

**Path Parameters:**

| Parameter | Type | Required | Mô tả |
|-----------|------|----------|-------|
| `id` | Long | ✅ Yes | ID của booking |

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Booking cancelled successfully",
  "data": {
    "id": 1,
    "title": "Sửa chữa máy lạnh",
    "address": "123 Đường ABC, Phường XYZ, Quận 1, TP.HCM",
    "date": "2024-12-25T14:00:00",
    "notes": "Máy lạnh không lạnh, cần kiểm tra gas",
    "phone": "0901234567",
    "name": "Nguyễn Văn A",
    "wardCode": "27601",
    "needsSurvey": true,
    "status": "CANCELLED",
    "createdAt": "2024-12-20T10:30:00",
    "updatedAt": "2024-12-20T11:05:00"
  },
  "timestamp": "2024-12-20T11:05:00"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8100/api/v1/bookings/1/cancel \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Error Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Cannot cancel booking with status: COMPLETED",
  "data": null,
  "timestamp": "2024-12-20T11:05:00"
}
```

---

## ⚠️ Error Handling

### HTTP Status Codes

| Code | Mô tả | Khi nào xảy ra |
|------|-------|----------------|
| `200` | OK | Request thành công |
| `201` | Created | Booking được tạo thành công |
| `400` | Bad Request | Dữ liệu request không hợp lệ hoặc booking không ở trạng thái phù hợp |
| `401` | Unauthorized | Thiếu hoặc JWT token không hợp lệ |
| `403` | Forbidden | Không có quyền truy cập (không phải CUSTOMER role) |
| `404` | Not Found | Booking không tồn tại hoặc không thuộc về user hiện tại |
| `500` | Internal Server Error | Lỗi server |

### Error Response Format

```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2024-12-20T10:30:00"
}
```

### Common Error Scenarios

1. **Cập nhật/Hủy booking không phải PENDING:**
   ```json
   {
     "success": false,
     "message": "Cannot update booking with status: COMPLETED"
   }
   ```

2. **Booking không tồn tại hoặc không thuộc về user:**
   ```json
   {
     "success": false,
     "message": "Booking ID 999 not found for user ID 123"
   }
   ```

3. **Validation errors:**
   ```json
   {
     "success": false,
     "message": "Validation failed",
     "data": {
       "title": "Title is required",
       "date": "Date is required"
     }
   }
   ```

---

## 📱 Integration Examples

### JavaScript/TypeScript (Fetch API)

```javascript
const API_BASE_URL = 'http://localhost:8100/api/v1';
const token = 'YOUR_JWT_TOKEN';

// Tạo booking
async function createBooking(bookingData) {
  const response = await fetch(`${API_BASE_URL}/bookings`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(bookingData)
  });
  
  const result = await response.json();
  return result.data;
}

// Lấy danh sách bookings
async function getBookings(status = null, page = 0, limit = 10) {
  const params = new URLSearchParams();
  if (status) params.append('status', status);
  params.append('page', page);
  params.append('limit', limit);
  
  const response = await fetch(`${API_BASE_URL}/bookings?${params}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  const result = await response.json();
  return result.data;
}

// Cập nhật booking
async function updateBooking(id, updateData) {
  const response = await fetch(`${API_BASE_URL}/bookings/${id}`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(updateData)
  });
  
  const result = await response.json();
  return result.data;
}

// Hủy booking
async function cancelBooking(id) {
  const response = await fetch(`${API_BASE_URL}/bookings/${id}/cancel`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  const result = await response.json();
  return result.data;
}
```

### Flutter/Dart

```dart
import 'package:http/http.dart' as http;
import 'dart:convert';

class BookingService {
  final String baseUrl = 'http://localhost:8100/api/v1';
  final String token;
  
  BookingService(this.token);
  
  Map<String, String> get headers => {
    'Authorization': 'Bearer $token',
    'Content-Type': 'application/json',
  };
  
  Future<Map<String, dynamic>> createBooking(Map<String, dynamic> data) async {
    final response = await http.post(
      Uri.parse('$baseUrl/bookings'),
      headers: headers,
      body: jsonEncode(data),
    );
    
    final result = jsonDecode(response.body);
    return result['data'];
  }
  
  Future<Map<String, dynamic>> getBookings({
    String? status,
    int page = 0,
    int limit = 10,
  }) async {
    final params = <String, String>{
      'page': page.toString(),
      'limit': limit.toString(),
    };
    if (status != null) params['status'] = status;
    
    final uri = Uri.parse('$baseUrl/bookings').replace(queryParameters: params);
    final response = await http.get(uri, headers: headers);
    
    final result = jsonDecode(response.body);
    return result['data'];
  }
  
  Future<Map<String, dynamic>> getBookingById(int id) async {
    final response = await http.get(
      Uri.parse('$baseUrl/bookings/$id'),
      headers: headers,
    );
    
    final result = jsonDecode(response.body);
    return result['data'];
  }
  
  Future<Map<String, dynamic>> updateBooking(int id, Map<String, dynamic> data) async {
    final response = await http.put(
      Uri.parse('$baseUrl/bookings/$id'),
      headers: headers,
      body: jsonEncode(data),
    );
    
    final result = jsonDecode(response.body);
    return result['data'];
  }
  
  Future<Map<String, dynamic>> cancelBooking(int id) async {
    final response = await http.post(
      Uri.parse('$baseUrl/bookings/$id/cancel'),
      headers: headers,
    );
    
    final result = jsonDecode(response.body);
    return result['data'];
  }
}
```

---

## 🔍 Testing với Swagger UI

API này đã được tích hợp vào Swagger UI. Để test:

1. Khởi động ứng dụng Spring Boot
2. Truy cập: `http://localhost:8100/swagger-ui.html`
3. Tìm section "Booking Controller"
4. Click "Authorize" và nhập JWT token
5. Test các endpoints trực tiếp từ Swagger UI

---

## 📝 Notes

1. **Date Format:** Tất cả datetime fields sử dụng ISO8601 format: `YYYY-MM-DDTHH:mm:ss`
   - Example: `2024-12-25T14:00:00`

2. **Pagination:** 
   - Page bắt đầu từ 0
   - Limit mặc định là 10, tối đa 100
   - Kết quả được sắp xếp theo `createdAt` descending (mới nhất trước)

3. **Authorization:**
   - Mỗi user chỉ có thể xem/sửa booking của chính mình
   - Hệ thống tự động lấy user từ JWT token

4. **Status Rules:**
   - Chỉ booking có status `PENDING` mới có thể được update hoặc cancel
   - Booking `COMPLETED` hoặc `CANCELLED` không thể thay đổi

5. **Validation:**
   - Tất cả required fields phải được cung cấp khi tạo booking
   - String fields có giới hạn độ dài tối đa
   - Date phải là datetime hợp lệ

---

## 🆘 Support

Nếu gặp vấn đề khi sử dụng API, vui lòng kiểm tra:

1. JWT token còn hiệu lực
2. User có role `CUSTOMER`
3. Booking ID thuộc về user hiện tại
4. Booking status phù hợp với thao tác (PENDING cho update/cancel)
5. Request body đúng format và validation rules

