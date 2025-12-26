# 🔔 Hướng Dẫn Sử Dụng Notification Endpoints Cho Frontend

## 📋 Tổng Quan

Hệ thống notification tự động tạo thông báo cho các hoạt động của user:
- ✅ **Đặt lịch thành công** (Booking Created)
- ⚠️ **Hủy đặt lịch** (Booking Cancelled)
- 👋 **Chào mừng** (User Registered)
- 🔐 **Đăng nhập thành công** (User Logged In)

Frontend có thể lấy danh sách thông báo, đánh dấu đã đọc, xóa thông báo, và xem thống kê.

---

## 🔑 Base URL & Authentication

**Base URL:** `/api/v1/notifications`

**Authentication:** 
- Header: `Authorization: Bearer <jwt_token>`
- Tất cả endpoints yêu cầu user đã đăng nhập (bất kỳ role nào)

---

## 📱 Các Endpoint Chính Cho Frontend

### 1. Lấy Danh Sách Thông Báo Của User

**Endpoint:** `GET /api/v1/notifications/my`

**Mô tả:** Lấy danh sách thông báo của user hiện tại với phân trang và filter

**Query Parameters:**
- `page` (optional): Số trang (0-based), mặc định: `0`
- `size` (optional): Số items mỗi trang, mặc định: `20`
- `sortBy` (optional): Field để sort, mặc định: `createdAt`
- `sortDir` (optional): Hướng sort (`asc` hoặc `desc`), mặc định: `desc`
- `isRead` (optional): Filter theo trạng thái đọc (`true` hoặc `false`)

**Request Example:**
```javascript
// Lấy tất cả thông báo (mới nhất trước)
GET /api/v1/notifications/my
Authorization: Bearer <jwt_token>

// Lấy thông báo chưa đọc
GET /api/v1/notifications/my?isRead=false
Authorization: Bearer <jwt_token>

// Lấy với phân trang
GET /api/v1/notifications/my?page=0&size=10&sortBy=createdAt&sortDir=desc
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Notifications retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "userId": 123,
        "title": "Đặt lịch thành công",
        "message": "Bạn đã đặt lịch dịch vụ: Sửa chữa máy lạnh. Chúng tôi sẽ liên hệ với bạn sớm nhất.",
        "type": "SUCCESS",
        "isRead": false,
        "createdAt": "2024-12-20T10:30:00",
        "timeAgo": "2 hours ago",
        "userFullName": "user12345678",
        "userEmail": "user@example.com",
        "category": "GENERAL",
        "priority": "MEDIUM"
      },
      {
        "id": 2,
        "userId": 123,
        "title": "Chào mừng bạn!",
        "message": "Cảm ơn bạn đã đăng ký tài khoản. Chúc bạn có trải nghiệm tuyệt vời với Fix4Home.",
        "type": "INFO",
        "isRead": true,
        "createdAt": "2024-12-19T08:00:00",
        "timeAgo": "1 day ago",
        "userFullName": "user12345678",
        "userEmail": "user@example.com",
        "category": "GENERAL",
        "priority": "MEDIUM"
      }
    ],
    "totalElements": 15,
    "totalPages": 2,
    "size": 10,
    "number": 0,
    "first": true,
    "last": false
  },
  "timestamp": "2024-12-20T12:35:00"
}
```

---

### 2. Lấy Số Lượng Thông Báo Chưa Đọc

**Endpoint:** `GET /api/v1/notifications/unread-count`

**Mô tả:** Lấy số lượng thông báo chưa đọc của user (dùng để hiển thị badge)

**Request Example:**
```javascript
GET /api/v1/notifications/unread-count
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Unread count retrieved successfully",
  "data": 5,
  "timestamp": "2024-12-20T12:35:00"
}
```

---

### 3. Lấy Chi Tiết Một Thông Báo

**Endpoint:** `GET /api/v1/notifications/{notificationId}`

**Mô tả:** Lấy chi tiết một thông báo cụ thể

**Path Parameters:**
- `notificationId`: ID của thông báo

**Request Example:**
```javascript
GET /api/v1/notifications/1
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Notification retrieved successfully",
  "data": {
    "id": 1,
    "userId": 123,
    "title": "Đặt lịch thành công",
    "message": "Bạn đã đặt lịch dịch vụ: Sửa chữa máy lạnh. Chúng tôi sẽ liên hệ với bạn sớm nhất.",
    "type": "SUCCESS",
    "isRead": false,
    "createdAt": "2024-12-20T10:30:00",
    "timeAgo": "2 hours ago",
    "userFullName": "user12345678",
    "userEmail": "user@example.com",
    "category": "GENERAL",
    "priority": "MEDIUM"
  },
  "timestamp": "2024-12-20T12:35:00"
}
```

---

### 4. Đánh Dấu Thông Báo Đã Đọc

**Endpoint:** `PUT /api/v1/notifications/{notificationId}/read`

**Mô tả:** Đánh dấu một thông báo là đã đọc

**Path Parameters:**
- `notificationId`: ID của thông báo

**Request Example:**
```javascript
PUT /api/v1/notifications/1/read
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Notification marked as read successfully",
  "data": {
    "id": 1,
    "userId": 123,
    "title": "Đặt lịch thành công",
    "message": "Bạn đã đặt lịch dịch vụ: Sửa chữa máy lạnh. Chúng tôi sẽ liên hệ với bạn sớm nhất.",
    "type": "SUCCESS",
    "isRead": true,
    "createdAt": "2024-12-20T10:30:00",
    "timeAgo": "2 hours ago",
    "userFullName": "user12345678",
    "userEmail": "user@example.com",
    "category": "GENERAL",
    "priority": "MEDIUM"
  },
  "timestamp": "2024-12-20T12:35:00"
}
```

---

### 5. Đánh Dấu Tất Cả Thông Báo Đã Đọc

**Endpoint:** `PUT /api/v1/notifications/mark-all-read`

**Mô tả:** Đánh dấu tất cả thông báo của user là đã đọc

**Request Example:**
```javascript
PUT /api/v1/notifications/mark-all-read
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "15 notification(s) marked as read successfully",
  "data": 15,
  "timestamp": "2024-12-20T12:35:00"
}
```

---

### 6. Đánh Dấu Nhiều Thông Báo (Bulk Mark)

**Endpoint:** `PUT /api/v1/notifications/mark`

**Mô tả:** Đánh dấu một hoặc nhiều thông báo là đã đọc/chưa đọc

**Request Body:**
```json
{
  "isRead": true,
  "notificationId": 1
}
```

Hoặc đánh dấu nhiều thông báo:
```json
{
  "isRead": true,
  "notificationIds": [1, 2, 3, 4, 5]
}
```

Hoặc đánh dấu tất cả:
```json
{
  "isRead": true,
  "markAll": true
}
```

**Request Example:**
```javascript
PUT /api/v1/notifications/mark
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "isRead": true,
  "notificationIds": [1, 2, 3]
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "3 notification(s) marked read successfully",
  "data": 3,
  "timestamp": "2024-12-20T12:35:00"
}
```

---

### 7. Xóa Một Thông Báo

**Endpoint:** `DELETE /api/v1/notifications/{notificationId}`

**Mô tả:** Xóa một thông báo cụ thể

**Path Parameters:**
- `notificationId`: ID của thông báo

**Request Example:**
```javascript
DELETE /api/v1/notifications/1
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Notification deleted successfully",
  "data": null,
  "timestamp": "2024-12-20T12:35:00"
}
```

---

### 8. Xóa Tất Cả Thông Báo Đã Đọc

**Endpoint:** `DELETE /api/v1/notifications/read`

**Mô tả:** Xóa tất cả thông báo đã đọc của user

**Request Example:**
```javascript
DELETE /api/v1/notifications/read
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "10 read notification(s) deleted successfully",
  "data": 10,
  "timestamp": "2024-12-20T12:35:00"
}
```

---

### 9. Tìm Kiếm Thông Báo

**Endpoint:** `GET /api/v1/notifications/search`

**Mô tả:** Tìm kiếm thông báo theo từ khóa (tìm trong title và message)

**Query Parameters:**
- `keyword` (required): Từ khóa tìm kiếm
- `page` (optional): Số trang (0-based), mặc định: `0`
- `size` (optional): Số items mỗi trang, mặc định: `20`

**Request Example:**
```javascript
GET /api/v1/notifications/search?keyword=đặt lịch&page=0&size=10
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Search completed successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "userId": 123,
        "title": "Đặt lịch thành công",
        "message": "Bạn đã đặt lịch dịch vụ: Sửa chữa máy lạnh...",
        "type": "SUCCESS",
        "isRead": false,
        "createdAt": "2024-12-20T10:30:00",
        "timeAgo": "2 hours ago"
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  },
  "timestamp": "2024-12-20T12:35:00"
}
```

---

### 10. Lấy Thông Báo Gần Đây

**Endpoint:** `GET /api/v1/notifications/recent`

**Mô tả:** Lấy thông báo trong N ngày gần đây

**Query Parameters:**
- `days` (optional): Số ngày gần đây, mặc định: `7`

**Request Example:**
```javascript
GET /api/v1/notifications/recent?days=7
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Recent notifications retrieved successfully",
  "data": [
    {
      "id": 1,
      "userId": 123,
      "title": "Đặt lịch thành công",
      "message": "Bạn đã đặt lịch dịch vụ: Sửa chữa máy lạnh...",
      "type": "SUCCESS",
      "isRead": false,
      "createdAt": "2024-12-20T10:30:00",
      "timeAgo": "2 hours ago"
    }
  ],
  "timestamp": "2024-12-20T12:35:00"
}
```

---

### 11. Lấy Thống Kê Thông Báo

**Endpoint:** `GET /api/v1/notifications/stats`

**Mô tả:** Lấy thống kê thông báo của user (tổng số, chưa đọc, đã đọc, v.v.)

**Request Example:**
```javascript
GET /api/v1/notifications/stats
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Statistics retrieved successfully",
  "data": {
    "userId": 123,
    "totalNotifications": 25,
    "unreadNotifications": 5,
    "readNotifications": 20,
    "readPercentage": 80.0,
    "notificationsToday": 3,
    "notificationsThisWeek": 10,
    "notificationsThisMonth": 25,
    "lastNotificationAt": "2024-12-20T10:30:00",
    "lastReadAt": null,
    "notificationsByCategory": {
      "SERVICE_REQUEST": 12,
      "SYSTEM": 8,
      "PAYMENT": 5
    },
    "notificationsByPriority": {
      "HIGH": 6,
      "MEDIUM": 12,
      "LOW": 7
    }
  },
  "timestamp": "2024-12-20T12:35:00"
}
```

---

## 📝 Notification Types

Các loại thông báo (`type` field):

- `INFO` - Thông tin chung (đăng nhập, đăng ký)
- `SUCCESS` - Thành công (tạo booking thành công)
- `WARNING` - Cảnh báo (hủy booking)
- `ERROR` - Lỗi (hiện tại chưa sử dụng)

---

## 💻 Code Examples Cho Frontend

### JavaScript/TypeScript (Fetch API)

```javascript
const API_BASE_URL = 'http://localhost:8100/api/v1';
const token = localStorage.getItem('accessToken');

/**
 * Lấy danh sách thông báo của user
 */
async function getMyNotifications(page = 0, size = 20, isRead = null) {
  try {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString()
    });
    
    if (isRead !== null) {
      params.append('isRead', isRead.toString());
    }
    
    const response = await fetch(`${API_BASE_URL}/notifications/my?${params}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch notifications');
    }
    
    const result = await response.json();
    return result.data;
  } catch (error) {
    console.error('Error fetching notifications:', error);
    throw error;
  }
}

/**
 * Lấy số lượng thông báo chưa đọc
 */
async function getUnreadCount() {
  try {
    const response = await fetch(`${API_BASE_URL}/notifications/unread-count`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch unread count');
    }
    
    const result = await response.json();
    return result.data;
  } catch (error) {
    console.error('Error fetching unread count:', error);
    throw error;
  }
}

/**
 * Đánh dấu thông báo đã đọc
 */
async function markAsRead(notificationId) {
  try {
    const response = await fetch(`${API_BASE_URL}/notifications/${notificationId}/read`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to mark notification as read');
    }
    
    const result = await response.json();
    return result.data;
  } catch (error) {
    console.error('Error marking notification as read:', error);
    throw error;
  }
}

/**
 * Đánh dấu tất cả thông báo đã đọc
 */
async function markAllAsRead() {
  try {
    const response = await fetch(`${API_BASE_URL}/notifications/mark-all-read`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to mark all as read');
    }
    
    const result = await response.json();
    return result.data;
  } catch (error) {
    console.error('Error marking all as read:', error);
    throw error;
  }
}

/**
 * Xóa một thông báo
 */
async function deleteNotification(notificationId) {
  try {
    const response = await fetch(`${API_BASE_URL}/notifications/${notificationId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to delete notification');
    }
    
    const result = await response.json();
    return result;
  } catch (error) {
    console.error('Error deleting notification:', error);
    throw error;
  }
}

/**
 * Xóa tất cả thông báo đã đọc
 */
async function deleteReadNotifications() {
  try {
    const response = await fetch(`${API_BASE_URL}/notifications/read`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to delete read notifications');
    }
    
    const result = await response.json();
    return result.data;
  } catch (error) {
    console.error('Error deleting read notifications:', error);
    throw error;
  }
}

/**
 * Tìm kiếm thông báo
 */
async function searchNotifications(keyword, page = 0, size = 20) {
  try {
    const params = new URLSearchParams({
      keyword: keyword,
      page: page.toString(),
      size: size.toString()
    });
    
    const response = await fetch(`${API_BASE_URL}/notifications/search?${params}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to search notifications');
    }
    
    const result = await response.json();
    return result.data;
  } catch (error) {
    console.error('Error searching notifications:', error);
    throw error;
  }
}

/**
 * Lấy thống kê thông báo
 */
async function getNotificationStats() {
  try {
    const response = await fetch(`${API_BASE_URL}/notifications/stats`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch notification stats');
    }
    
    const result = await response.json();
    return result.data;
  } catch (error) {
    console.error('Error fetching notification stats:', error);
    throw error;
  }
}
```

---

### React Hook Example

```javascript
import { useState, useEffect } from 'react';

function useNotifications() {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Lấy danh sách thông báo
  const fetchNotifications = async (page = 0, size = 20, isRead = null) => {
    setLoading(true);
    setError(null);
    try {
      const data = await getMyNotifications(page, size, isRead);
      setNotifications(data.content);
      return data;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Lấy số lượng chưa đọc
  const fetchUnreadCount = async () => {
    try {
      const count = await getUnreadCount();
      setUnreadCount(count);
      return count;
    } catch (err) {
      console.error('Error fetching unread count:', err);
    }
  };

  // Đánh dấu đã đọc
  const markAsRead = async (notificationId) => {
    try {
      await markAsRead(notificationId);
      // Refresh danh sách và unread count
      await fetchNotifications();
      await fetchUnreadCount();
    } catch (err) {
      console.error('Error marking as read:', err);
      throw err;
    }
  };

  // Đánh dấu tất cả đã đọc
  const markAllAsRead = async () => {
    try {
      await markAllAsRead();
      // Refresh danh sách và unread count
      await fetchNotifications();
      await fetchUnreadCount();
    } catch (err) {
      console.error('Error marking all as read:', err);
      throw err;
    }
  };

  // Xóa thông báo
  const deleteNotification = async (notificationId) => {
    try {
      await deleteNotification(notificationId);
      // Refresh danh sách
      await fetchNotifications();
      await fetchUnreadCount();
    } catch (err) {
      console.error('Error deleting notification:', err);
      throw err;
    }
  };

  // Load initial data
  useEffect(() => {
    fetchNotifications();
    fetchUnreadCount();
    
    // Refresh unread count mỗi 30 giây
    const interval = setInterval(() => {
      fetchUnreadCount();
    }, 30000);
    
    return () => clearInterval(interval);
  }, []);

  return {
    notifications,
    unreadCount,
    loading,
    error,
    fetchNotifications,
    fetchUnreadCount,
    markAsRead,
    markAllAsRead,
    deleteNotification
  };
}

// Sử dụng trong component
function NotificationList() {
  const {
    notifications,
    unreadCount,
    loading,
    markAsRead,
    markAllAsRead,
    deleteNotification
  } = useNotifications();

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      <div className="notification-header">
        <h2>Thông Báo ({unreadCount} chưa đọc)</h2>
        <button onClick={markAllAsRead}>Đánh dấu tất cả đã đọc</button>
      </div>
      
      <div className="notification-list">
        {notifications.map(notification => (
          <div
            key={notification.id}
            className={`notification-item ${!notification.isRead ? 'unread' : ''}`}
          >
            <div className="notification-content">
              <h3>{notification.title}</h3>
              <p>{notification.message}</p>
              <span className="time-ago">{notification.timeAgo}</span>
            </div>
            <div className="notification-actions">
              {!notification.isRead && (
                <button onClick={() => markAsRead(notification.id)}>
                  Đánh dấu đã đọc
                </button>
              )}
              <button onClick={() => deleteNotification(notification.id)}>
                Xóa
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
```

---

## 🎯 Các Trường Hợp Sử Dụng Phổ Biến

### 1. Hiển Thị Badge Số Thông Báo Chưa Đọc

```javascript
// Polling mỗi 30 giây để cập nhật badge
setInterval(async () => {
  const count = await getUnreadCount();
  updateBadge(count);
}, 30000);
```

### 2. Tự Động Đánh Dấu Đã Đọc Khi User Xem

```javascript
// Khi user click vào thông báo
async function handleNotificationClick(notificationId) {
  // Mở chi tiết thông báo
  openNotificationDetail(notificationId);
  
  // Tự động đánh dấu đã đọc nếu chưa đọc
  if (!notification.isRead) {
    await markAsRead(notificationId);
  }
}
```

### 3. Pull-to-Refresh

```javascript
// Kéo xuống để refresh danh sách thông báo
async function handleRefresh() {
  await fetchNotifications();
  await fetchUnreadCount();
}
```

### 4. Filter Thông Báo

```javascript
// Lọc chỉ hiển thị thông báo chưa đọc
const unreadNotifications = await getMyNotifications(0, 20, false);

// Lọc chỉ hiển thị thông báo đã đọc
const readNotifications = await getMyNotifications(0, 20, true);
```

---

## ⚠️ Lưu Ý

1. **Authentication**: Tất cả endpoints yêu cầu JWT token trong header `Authorization: Bearer <token>`
2. **Pagination**: Sử dụng `page` và `size` để phân trang, mặc định `page=0`, `size=20`
3. **Auto-refresh**: Nên refresh danh sách thông báo sau khi user thực hiện các action (đánh dấu đã đọc, xóa, v.v.)
4. **Error Handling**: Luôn xử lý lỗi và hiển thị thông báo phù hợp cho user
5. **Performance**: Cân nhắc sử dụng polling hoặc WebSocket để cập nhật real-time (nếu cần)

---

## 📚 Tham Khảo Thêm

- Base URL: `http://localhost:8100/api/v1`
- Swagger UI: `http://localhost:8100/swagger-ui.html` (nếu có)
- Notification types: `INFO`, `SUCCESS`, `WARNING`, `ERROR`

