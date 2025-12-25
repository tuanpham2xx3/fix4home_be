# 📋 Hướng Dẫn Sử Dụng Booking Endpoints Cho Frontend

## ⚠️ Vấn Đề Hiện Tại

Sau khi tạo endpoint admin để lấy tất cả bookings (`GET /api/v1/bookings/admin`), có thể frontend đang gọi nhầm endpoint này thay vì endpoint dành cho user.

## ✅ Giải Pháp: Sử Dụng Đúng Endpoint

### 🔑 Phân Biệt Endpoint User vs Admin

| Endpoint | Method | Mô Tả | Role Yêu Cầu |
|----------|--------|-------|--------------|
| `/api/v1/bookings` | GET | **User lấy danh sách booking của chính mình** | `CUSTOMER` |
| `/api/v1/bookings/admin` | GET | **Admin lấy danh sách tất cả bookings** | `ADMIN` |

---

## 📱 Endpoint Cho User (CUSTOMER)

### 1. Lấy Danh Sách Bookings Của User

**Endpoint:** `GET /api/v1/bookings`

**Mô tả:** User lấy danh sách booking của chính mình (tự động filter theo user hiện tại)

**Authentication:** 
- Header: `Authorization: Bearer <customer_jwt_token>`
- Role: `CUSTOMER`

**Query Parameters:**
- `status` (optional): `PENDING`, `COMPLETED`, `CANCELLED`
- `page` (optional): Số trang (0-based), mặc định: 0
- `limit` (optional): Số items mỗi trang, mặc định: 10, tối đa: 100

**Request Example:**
```javascript
// Lấy tất cả bookings của user
GET /api/v1/bookings
Authorization: Bearer <customer_jwt_token>

// Lấy bookings có status PENDING
GET /api/v1/bookings?status=PENDING
Authorization: Bearer <customer_jwt_token>

// Lấy với phân trang
GET /api/v1/bookings?page=0&limit=20
Authorization: Bearer <customer_jwt_token>
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
        "createdAt": "2024-12-20T10:30:00",
        "updatedAt": "2024-12-20T10:30:00"
      }
    ],
    "total": 1,
    "page": 0,
    "limit": 10
  },
  "timestamp": "2024-12-20T10:35:00"
}
```

---

## 🔧 Code Example Cho Frontend

### JavaScript/TypeScript (Fetch API)

```javascript
const API_BASE_URL = 'http://localhost:8100/api/v1';
const token = localStorage.getItem('accessToken'); // JWT token của user

/**
 * Lấy danh sách bookings của user hiện tại
 * @param {string|null} status - Filter theo status (PENDING, COMPLETED, CANCELLED)
 * @param {number} page - Số trang (0-based)
 * @param {number} limit - Số items mỗi trang
 * @returns {Promise<Object>} Danh sách bookings
 */
async function getUserBookings(status = null, page = 0, limit = 10) {
  try {
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    params.append('page', page);
    params.append('limit', limit);
    
    const response = await fetch(`${API_BASE_URL}/bookings?${params}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to fetch bookings');
    }
    
    const result = await response.json();
    return result.data; // { bookings: [], total: 0, page: 0, limit: 10 }
  } catch (error) {
    console.error('Error fetching user bookings:', error);
    throw error;
  }
}

// Sử dụng:
// Lấy tất cả bookings
const allBookings = await getUserBookings();

// Lấy bookings PENDING
const pendingBookings = await getUserBookings('PENDING');

// Lấy với phân trang
const page2Bookings = await getUserBookings(null, 1, 20);
```

### React Hook Example

```typescript
import { useState, useEffect } from 'react';

interface Booking {
  id: number;
  title: string;
  address: string;
  date: string;
  status: 'PENDING' | 'COMPLETED' | 'CANCELLED';
  // ... other fields
}

interface BookingListResponse {
  bookings: Booking[];
  total: number;
  page: number;
  limit: number;
}

function useUserBookings(status?: string, page: number = 0, limit: number = 10) {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pagination, setPagination] = useState({ total: 0, page: 0, limit: 10 });

  useEffect(() => {
    const fetchBookings = async () => {
      setLoading(true);
      setError(null);
      
      try {
        const token = localStorage.getItem('accessToken');
        const params = new URLSearchParams();
        if (status) params.append('status', status);
        params.append('page', page.toString());
        params.append('limit', limit.toString());
        
        const response = await fetch(
          `${process.env.REACT_APP_API_URL}/api/v1/bookings?${params}`,
          {
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json'
            }
          }
        );
        
        if (!response.ok) {
          const errorData = await response.json();
          throw new Error(errorData.message || 'Failed to fetch bookings');
        }
        
        const result = await response.json();
        setBookings(result.data.bookings);
        setPagination({
          total: result.data.total,
          page: result.data.page,
          limit: result.data.limit
        });
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unknown error');
      } finally {
        setLoading(false);
      }
    };

    fetchBookings();
  }, [status, page, limit]);

  return { bookings, loading, error, pagination };
}

// Sử dụng trong component:
function BookingList() {
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [page, setPage] = useState(0);
  const { bookings, loading, error, pagination } = useUserBookings(statusFilter, page, 10);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div>
      <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
        <option value="">All</option>
        <option value="PENDING">Pending</option>
        <option value="COMPLETED">Completed</option>
        <option value="CANCELLED">Cancelled</option>
      </select>
      
      <ul>
        {bookings.map(booking => (
          <li key={booking.id}>
            {booking.title} - {booking.status}
          </li>
        ))}
      </ul>
      
      <div>
        Page {pagination.page + 1} of {Math.ceil(pagination.total / pagination.limit)}
        <button onClick={() => setPage(p => Math.max(0, p - 1))}>Previous</button>
        <button onClick={() => setPage(p => p + 1)}>Next</button>
      </div>
    </div>
  );
}
```

### Axios Example

```javascript
import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8100/api/v1',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Interceptor để tự động thêm token
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Lấy danh sách bookings của user
export const getUserBookings = async (status = null, page = 0, limit = 10) => {
  try {
    const params = { page, limit };
    if (status) params.status = status;
    
    const response = await apiClient.get('/bookings', { params });
    return response.data.data; // { bookings: [], total: 0, page: 0, limit: 10 }
  } catch (error) {
    console.error('Error fetching user bookings:', error);
    throw error;
  }
};
```

---

## 🚫 Lỗi Thường Gặp

### 1. Gọi Nhầm Endpoint Admin

**❌ SAI:**
```javascript
// User đang gọi endpoint admin (sẽ bị 403 Forbidden)
fetch('/api/v1/bookings/admin', {
  headers: { 'Authorization': `Bearer ${customerToken}` }
});
```

**✅ ĐÚNG:**
```javascript
// User gọi endpoint của mình
fetch('/api/v1/bookings', {
  headers: { 'Authorization': `Bearer ${customerToken}` }
});
```

### 2. Thiếu Token Hoặc Token Không Hợp Lệ

**Lỗi:** `401 Unauthorized`

**Giải pháp:** Đảm bảo:
- Token được gửi trong header: `Authorization: Bearer <token>`
- Token còn hiệu lực (chưa hết hạn)
- Token đúng format

### 3. User Không Có Role CUSTOMER

**Lỗi:** `403 Forbidden`

**Giải pháp:** Đảm bảo user đã đăng ký với role `CUSTOMER`

### 4. Response Trống (Không Có Bookings)

**Nguyên nhân:** User chưa tạo booking nào, hoặc filter status không khớp

**Giải pháp:** Kiểm tra:
- User đã tạo booking chưa?
- Filter status có đúng không?
- Thử gọi không có filter: `GET /api/v1/bookings`

---

## 📊 So Sánh Endpoint User vs Admin

| Tính Năng | User Endpoint | Admin Endpoint |
|-----------|---------------|----------------|
| **URL** | `/api/v1/bookings` | `/api/v1/bookings/admin` |
| **Role** | `CUSTOMER` | `ADMIN` |
| **Dữ Liệu Trả Về** | Chỉ bookings của user hiện tại | Tất cả bookings của mọi user |
| **Filter User** | Tự động (theo JWT token) | Không (lấy tất cả) |
| **Use Case** | User xem booking của mình | Admin quản lý tất cả bookings |

---

## ✅ Checklist Cho Frontend

- [ ] Sử dụng endpoint `/api/v1/bookings` (KHÔNG phải `/admin`)
- [ ] Gửi JWT token trong header `Authorization: Bearer <token>`
- [ ] User có role `CUSTOMER`
- [ ] Xử lý error cases (401, 403, 404, 500)
- [ ] Hiển thị loading state khi fetch
- [ ] Xử lý pagination nếu có nhiều bookings
- [ ] Filter theo status nếu cần

---

## 🔍 Debug Tips

1. **Kiểm tra Network Tab:**
   - Xem request URL có đúng không
   - Xem headers có token không
   - Xem response status code

2. **Kiểm tra Console:**
   - Xem error message từ API
   - Kiểm tra token có hợp lệ không

3. **Test với cURL:**
```bash
# Test endpoint user
curl -X GET "http://localhost:8100/api/v1/bookings?page=0&limit=10" \
  -H "Authorization: Bearer YOUR_CUSTOMER_TOKEN"
```

---

## 📞 Support

Nếu vẫn gặp vấn đề, kiểm tra:
1. Backend có đang chạy không? (http://localhost:8100)
2. Token có hợp lệ không? (test với endpoint `/api/v1/auth/me`)
3. User có role CUSTOMER không?
4. Có booking nào trong database không?

