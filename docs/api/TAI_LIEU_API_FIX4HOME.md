# TÀI LIỆU API FIX4HOME - HƯỚNG DẪN CHI TIẾT

## 📋 MỤC LỤC

1. [Tổng quan hệ thống](#tổng-quan-hệ-thống)
2. [Cấu trúc Response chuẩn](#cấu-trúc-response-chuẩn)  
3. [Phân loại API theo chức năng](#phân-loại-api-theo-chức-năng)
4. [Chi tiết từng nhóm API](#chi-tiết-từng-nhóm-api)
5. [Cách Frontend gửi/nhận dữ liệu](#cách-frontend-gửinhận-dữ-liệu)
6. [Xử lý lỗi và Status Code](#xử-lý-lỗi-và-status-code)

---

## 🏗️ TỔNG QUAN HỆ THỐNG

### URL Gốc
```
http://localhost:8080/api/v1
```

### Kiến trúc Authentication
- **JWT Bearer Token**: Dùng cho xác thực người dùng
- **Role-based Access**: Phân quyền theo vai trò (CUSTOMER, TECHNICIAN, ADMIN)
- **Refresh Token**: Tự động gia hạn session

### Các vai trò trong hệ thống
| Vai trò | Mô tả | Quyền truy cập |
|---------|-------|----------------|
| **CUSTOMER** | Khách hàng sử dụng dịch vụ | Đặt dịch vụ, thanh toán, đánh giá |
| **TECHNICIAN** | Thợ kỹ thuật thực hiện dịch vụ | Nhận việc, cập nhật tiến độ |
| **ADMIN** | Quản trị viên hệ thống | Quản lý toàn bộ hệ thống |

---

## 📦 CẤU TRÚC RESPONSE CHUẨN

Tất cả API đều trả về theo định dạng sau:

### ✅ Response Thành công
```json
{
    "success": true,
    "message": "Thông báo thành công",
    "data": {
        // Dữ liệu trả về
    },
    "timestamp": "2024-01-15T10:30:00Z"
}
```

### ❌ Response Lỗi
```json
{
    "success": false,
    "message": "Mô tả lỗi chi tiết",
    "data": null,
    "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## 🗂️ PHÂN LOẠI API THEO CHỨC NĂNG

| STT | Nhóm chức năng | Số lượng API | Mô tả |
|-----|----------------|--------------|-------|
| 1 | **Xác thực & Phân quyền** | 6 endpoints | Đăng ký, đăng nhập, gia hạn token |
| 2 | **Quản lý Khách hàng** | 11 endpoints | Profile, địa chỉ khách hàng |
| 3 | **Quản lý Thợ kỹ thuật** | 16 endpoints | Profile, kỹ năng thợ |
| 4 | **Quản lý Dịch vụ** | 12 endpoints | Danh mục dịch vụ |
| 5 | **Quản lý Yêu cầu dịch vụ** | 12 endpoints | Đặt, theo dõi, thực hiện dịch vụ |
| 6 | **Quản lý Thanh toán** | 9 endpoints | Xử lý thanh toán |
| 7 | **Quản lý Đánh giá** | 13 endpoints | Feedback và đánh giá |
| 8 | **Quản lý Thông báo** | 16 endpoints | Gửi/nhận thông báo |
| 9 | **Quản lý Hệ thống (Admin)** | 15+ endpoints | Báo cáo, thống kê |
| 10 | **Kiểm tra Hệ thống** | 6 endpoints | Health check |

**Tổng cộng: 120+ API endpoints**

---

## 📝 CHI TIẾT TỪNG NHÓM API

## 1. 🔐 XÁC THỰC & PHÂN QUYỀN

### 1.1 Đăng ký tài khoản
```http
POST /api/v1/auth/register
```

**Frontend gửi:**
```json
{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "MatKhau123!",
    "fullName": "Nguyễn Văn A",
    "phoneNumber": "+84987654321",
    "role": "CUSTOMER"
}
```

**Backend trả về:**
```json
{
    "success": true,
    "message": "Đăng ký thành công",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "tokenType": "Bearer",
        "expiresIn": 900,
        "userId": 1,
        "username": "john_doe",
        "email": "john@example.com",
        "role": "CUSTOMER",
        "fullName": "Nguyễn Văn A"
    }
}
```

### 1.2 Đăng nhập
```http
POST /api/v1/auth/login
```

**Frontend gửi:**
```json
{
    "usernameOrEmail": "john@example.com",
    "password": "MatKhau123!"
}
```

**Backend trả về:** _Giống như đăng ký_

### 1.3 Gia hạn token
```http
POST /api/v1/auth/refresh
Cookie: refresh_token=<refresh_token>
X-Device-Id: <device_id>
```

**Backend trả về:**
```json
{
    "success": true,
    "message": "Token refreshed successfully",
    "data": {
        "accessToken": "<access_token>",
        "tokenType": "Bearer",
        "expiresIn": 900,
        "userId": 1,
        "username": "user65202568",
        "email": "user@example.com"
    }
}
```

---

## 2. 👤 QUẢN LÝ KHÁCH HÀNG

### 2.1 Xem thông tin cá nhân
```http
GET /api/v1/customers/profile
Authorization: Bearer <access_token>
```

**Backend trả về:**
```json
{
    "success": true,
    "message": "Lấy thông tin thành công",
    "data": {
        "userId": 1,
        "username": "john_doe",
        "email": "john@example.com",
        "phoneNumber": "+84987654321",
        "role": "CUSTOMER",
        "status": "ACTIVE",
        "profileId": 1,
        "fullName": "Nguyễn Văn A",
        "gender": "MALE",
        "dob": "1990-01-01"
    }
}
```

### 2.2 Cập nhật thông tin cá nhân
```http
PUT /api/v1/customers/profile
Authorization: Bearer <access_token>
```

**Frontend gửi:**
```json
{
    "fullName": "Nguyễn Văn B",
    "phoneNumber": "+84987654322",
    "gender": "MALE",
    "dob": "1990-01-01"
}
```

### 2.3 Quản lý địa chỉ
```http
GET /api/v1/customers/addresses          # Danh sách địa chỉ
POST /api/v1/customers/addresses         # Thêm địa chỉ mới
PUT /api/v1/customers/addresses/{id}     # Sửa địa chỉ
DELETE /api/v1/customers/addresses/{id}  # Xóa địa chỉ
```

**Thêm địa chỉ mới:**
```json
{
    "street": "123 Đường ABC",
    "ward": "Phường 1",
    "district": "Quận 1",
    "city": "TP.HCM",
    "isDefault": true
}
```

---

## 3. 🔧 QUẢN LÝ THỢ KỸ THUẬT

### 3.1 Danh sách thợ công khai
```http
GET /api/v1/technicians/active
```

### 3.2 Thông tin cá nhân thợ
```http
GET /api/v1/technicians/me
Authorization: Bearer <technician_token>
```

### 3.3 Cập nhật kỹ năng
```http
PUT /api/v1/technicians/me/skills
Authorization: Bearer <technician_token>
```

**Frontend gửi:**
```json
{
    "skillIds": [1, 2, 3],
    "experience": "5 năm kinh nghiệm sửa chữa điện nước"
}
```

---

## 4. 🛠️ QUẢN LÝ DỊCH VỤ

### 4.1 Danh sách dịch vụ (Công khai)
```http
GET /api/v1/services
```

**Backend trả về:**
```json
{
    "success": true,
    "message": "Danh sách dịch vụ",
    "data": [
        {
            "id": 1,
            "name": "Sửa chữa điện",
            "description": "Sửa chữa hệ thống điện trong nhà",
            "basePrice": 200000,
            "category": "ELECTRICAL",
            "isActive": true
        }
    ]
}
```

### 4.2 Tìm kiếm dịch vụ
```http
GET /api/v1/services/search?keyword=điện&category=ELECTRICAL
```

### 4.3 Quản lý dịch vụ (Admin)
```http
POST /api/v1/services                    # Tạo dịch vụ mới
PUT /api/v1/services/{id}               # Cập nhật dịch vụ
DELETE /api/v1/services/{id}            # Xóa dịch vụ
```

---

## 5. 📋 QUẢN LÝ YÊU CẦU DỊCH VỤ

### 5.1 Khách hàng đặt dịch vụ
```http
POST /api/v1/service-requests
Authorization: Bearer <customer_token>
```

**Frontend gửi:**
```json
{
    "serviceId": 1,
    "addressId": 1,
    "description": "Cần sửa ổ cắm điện bị chập",
    "scheduledTime": "2024-01-20T09:00:00",
    "price": 250000
}
```

**Backend trả về:**
```json
{
    "success": true,
    "message": "Đặt dịch vụ thành công",
    "data": {
        "id": 101,
        "serviceId": 1,
        "customerId": 1,
        "status": "PENDING",
        "description": "Cần sửa ổ cắm điện bị chập",
        "scheduledTime": "2024-01-20T09:00:00",
        "price": 250000,
        "createdAt": "2024-01-15T10:30:00"
    }
}
```

### 5.2 Theo dõi yêu cầu của khách hàng
```http
GET /api/v1/service-requests/my
Authorization: Bearer <customer_token>
```

### 5.3 Thợ xem công việc có sẵn
```http
GET /api/v1/service-requests/available
Authorization: Bearer <technician_token>
```

### 5.4 Thợ nhận việc
```http
PUT /api/v1/service-requests/{id}/accept
Authorization: Bearer <technician_token>
```

### 5.5 Cập nhật trạng thái công việc
```http
PUT /api/v1/service-requests/{id}/start      # Bắt đầu làm
PUT /api/v1/service-requests/{id}/complete   # Hoàn thành
```

**Hoàn thành công việc:**
```json
{
    "completionNotes": "Đã sửa xong ổ cắm, thay dây điện mới",
    "actualPrice": 280000
}
```

### 5.6 Trạng thái yêu cầu dịch vụ
| Trạng thái | Mô tả |
|------------|-------|
| `PENDING` | Chờ thợ nhận việc |
| `ASSIGNED` | Đã có thợ nhận việc |
| `IN_PROGRESS` | Đang thực hiện |
| `DONE` | Hoàn thành |
| `CANCELLED` | Đã hủy |

---

## 6. 💳 QUẢN LÝ THANH TOÁN

### 6.1 Danh sách phương thức thanh toán
```http
GET /api/v1/payments/methods
```

### 6.2 Tạo thanh toán
```http
POST /api/v1/payments/create
Authorization: Bearer <customer_token>
```

**Frontend gửi:**
```json
{
    "serviceRequestId": 101,
    "amount": 280000,
    "paymentMethod": "CASH",
    "notes": "Thanh toán tiền mặt"
}
```

### 6.3 Lịch sử thanh toán khách hàng
```http
GET /api/v1/payments/customer/my
Authorization: Bearer <customer_token>
```

### 6.4 Thu nhập của thợ
```http
GET /api/v1/payments/technician/my
Authorization: Bearer <technician_token>
```

---

## 7. ⭐ QUẢN LÝ ĐÁNH GIÁ

### 7.1 Khách hàng tạo đánh giá
```http
POST /api/v1/feedbacks/create
Authorization: Bearer <customer_token>
```

**Frontend gửi:**
```json
{
    "serviceRequestId": 101,
    "technicianId": 5,
    "rating": 5,
    "comment": "Thợ làm việc rất tận tâm và chuyên nghiệp",
    "isPublic": true
}
```

### 7.2 Xem đánh giá công khai
```http
GET /api/v1/feedbacks/public?minRating=4
```

### 7.3 Đánh giá của thợ
```http
GET /api/v1/feedbacks/technician/{technicianId}
```

---

## 8. 🔔 QUẢN LÝ THÔNG BÁO

### 8.1 Danh sách thông báo cá nhân
```http
GET /api/v1/notifications/my
Authorization: Bearer <access_token>
```

### 8.2 Đánh dấu đã đọc
```http
PUT /api/v1/notifications/mark-read
Authorization: Bearer <access_token>
```

**Frontend gửi:**
```json
{
    "notificationIds": [1, 2, 3]
}
```

### 8.3 Gửi thông báo (Admin)
```http
POST /api/v1/notifications/admin/send
Authorization: Bearer <admin_token>
```

---

## 9. 👨‍💼 QUẢN LÝ HỆ THỐNG (ADMIN)

### 9.1 Dashboard tổng quan
```http
GET /api/v1/admin/dashboard
Authorization: Bearer <admin_token>
```

**Backend trả về:**
```json
{
    "success": true,
    "message": "Dữ liệu dashboard",
    "data": {
        "totalUsers": 1250,
        "totalCustomers": 1000,
        "totalTechnicians": 200,
        "totalServiceRequests": 5000,
        "pendingRequests": 25,
        "completedRequests": 4800,
        "totalRevenue": 50000000,
        "monthlyRevenue": 8000000
    }
}
```

### 9.2 Quản lý người dùng
```http
GET /api/v1/admin/users                 # Danh sách người dùng
PUT /api/v1/admin/users/{id}/status     # Cập nhật trạng thái
DELETE /api/v1/admin/users/{id}         # Xóa người dùng
```

### 9.3 Báo cáo hệ thống
```http
GET /api/v1/admin/reports/system        # Báo cáo tổng quan
GET /api/v1/admin/reports/revenue       # Báo cáo doanh thu
GET /api/v1/admin/reports/users         # Báo cáo người dùng
```

---

## 💻 CÁCH FRONTEND GỬI/NHẬN DỮ LIỆU

### 🔄 Quy trình Authentication

#### 1. **Đăng nhập**
```javascript
// Frontend gửi
const loginData = {
    usernameOrEmail: "user@example.com",
    password: "password123"
};

const response = await fetch('/api/v1/auth/login', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'X-Device-Id': 'web-browser-123'
    },
    body: JSON.stringify(loginData)
});

const result = await response.json();

// Lưu token vào localStorage
if (result.success) {
    localStorage.setItem('accessToken', result.data.accessToken);
    localStorage.setItem('userId', result.data.userId);
    localStorage.setItem('userRole', result.data.role);
}
```

#### 2. **Gửi request có authentication**
```javascript
const token = localStorage.getItem('accessToken');

const response = await fetch('/api/v1/customers/profile', {
    method: 'GET',
    headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    }
});
```

#### 3. **Xử lý refresh token tự động**
```javascript
// Interceptor để tự động refresh token
axios.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (error.response?.status === 401) {
            try {
                const refreshResponse = await fetch('/api/v1/auth/refresh', {
                    method: 'POST',
                    credentials: 'include', // Gửi cookie
                    headers: {
                        'X-Device-Id': 'web-browser-123'
                    }
                });
                
                if (refreshResponse.ok) {
                    const result = await refreshResponse.json();
                    localStorage.setItem('accessToken', result.data.accessToken);
                    // Retry request gốc
                    return axios(error.config);
                }
            } catch (refreshError) {
                // Redirect to login
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);
```

### 📤 Gửi dữ liệu phức tạp

#### 1. **Đặt dịch vụ**
```javascript
const serviceRequest = {
    serviceId: 1,
    addressId: 2,
    description: "Cần sửa vòi nước bếp bị rò rỉ",
    scheduledTime: "2024-01-20T14:00:00",
    price: 150000
};

const response = await fetch('/api/v1/service-requests', {
    method: 'POST',
    headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    },
    body: JSON.stringify(serviceRequest)
});

const result = await response.json();

if (result.success) {
    alert('Đặt dịch vụ thành công!');
    // Redirect hoặc update UI
} else {
    alert(`Lỗi: ${result.message}`);
}
```

#### 2. **Upload file (nếu có)**
```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);
formData.append('description', 'Hình ảnh vấn đề');

const response = await fetch('/api/v1/service-requests/1/attachments', {
    method: 'POST',
    headers: {
        'Authorization': `Bearer ${token}`
        // Không set Content-Type khi upload file
    },
    body: formData
});
```

### 📥 Xử lý dữ liệu nhận về

#### 1. **Phân trang**
```javascript
const fetchServices = async (page = 0, size = 10) => {
    const response = await fetch(
        `/api/v1/services/paginated?page=${page}&size=${size}&sortBy=name&sortDir=asc`
    );
    
    const result = await response.json();
    
    if (result.success) {
        // result.data có cấu trúc:
        // {
        //     content: [...], // Mảng dữ liệu
        //     totalElements: 100,
        //     totalPages: 10,
        //     number: 0, // Trang hiện tại
        //     size: 10
        // }
        
        updateServiceList(result.data.content);
        updatePagination(result.data);
    }
};
```

#### 2. **Realtime với WebSocket (nếu có)**
```javascript
const socket = new WebSocket('ws://localhost:8080/ws/notifications');

socket.onmessage = (event) => {
    const notification = JSON.parse(event.data);
    showNotification(notification.message);
};
```

---

## ⚠️ XỬ LÝ LỖI VÀ STATUS CODE

### 📊 Các mã trạng thái HTTP

| Code | Trạng thái | Mô tả | Cách xử lý Frontend |
|------|------------|-------|-------------------|
| 200 | OK | Thành công | Hiển thị dữ liệu |
| 201 | Created | Tạo mới thành công | Thông báo thành công |
| 400 | Bad Request | Dữ liệu gửi lên không hợp lệ | Hiển thị lỗi validation |
| 401 | Unauthorized | Chưa đăng nhập hoặc token hết hạn | Chuyển về trang đăng nhập |
| 403 | Forbidden | Không có quyền truy cập | Thông báo không đủ quyền |
| 404 | Not Found | Không tìm thấy tài nguyên | Thông báo không tìm thấy |
| 409 | Conflict | Dữ liệu đã tồn tại | Thông báo trùng lặp |
| 429 | Too Many Requests | Quá nhiều request | Thông báo thử lại sau |
| 500 | Internal Server Error | Lỗi server | Thông báo lỗi hệ thống |

### 🔧 Xử lý lỗi validation

```javascript
const handleApiResponse = async (response) => {
    const result = await response.json();
    
    if (!result.success) {
        switch (response.status) {
            case 400:
                // Lỗi validation
                showValidationErrors(result.message);
                break;
            case 401:
                // Chưa đăng nhập
                redirectToLogin();
                break;
            case 403:
                // Không có quyền
                showError('Bạn không có quyền thực hiện thao tác này');
                break;
            case 404:
                // Không tìm thấy
                showError('Không tìm thấy dữ liệu yêu cầu');
                break;
            case 409:
                // Trùng lặp
                showError('Dữ liệu đã tồn tại');
                break;
            default:
                // Lỗi khác
                showError('Có lỗi xảy ra, vui lòng thử lại');
        }
        return null;
    }
    
    return result.data;
};
```

### 🛡️ Xử lý lỗi mạng

```javascript
const apiCall = async (url, options) => {
    try {
        const response = await fetch(url, {
            timeout: 10000, // 10 giây timeout
            ...options
        });
        
        return await handleApiResponse(response);
    } catch (error) {
        if (error.name === 'AbortError') {
            showError('Kết nối bị gián đoạn');
        } else if (error.name === 'TypeError') {
            showError('Không thể kết nối đến server');
        } else {
            showError('Có lỗi xảy ra, vui lòng thử lại');
        }
        return null;
    }
};
```

---

## 🎯 CÁC LƯU Ý QUAN TRỌNG

### 🔐 Bảo mật
1. **Luôn gửi token trong header Authorization**
2. **Không lưu sensitive data trong localStorage**
3. **Validate dữ liệu ở cả FE và BE**
4. **Sử dụng HTTPS trong production**

### ⚡ Performance
1. **Sử dụng pagination cho danh sách lớn**
2. **Cache dữ liệu ít thay đổi**
3. **Debounce cho search input**
4. **Lazy loading cho hình ảnh**

### 🎨 UX/UI
1. **Hiển thị loading state**
2. **Thông báo lỗi rõ ràng**
3. **Confirmation cho thao tác quan trọng**
4. **Offline handling**

---

## 📞 LIÊN HỆ HỖ TRỢ

- **Email**: support@fix4home.com
- **Documentation**: `/swagger-ui/index.html`
- **Version**: v1.0.0
- **Last Updated**: Tháng 1/2024

---

*Tài liệu này được tạo tự động từ codebase Fix4Home Backend* 