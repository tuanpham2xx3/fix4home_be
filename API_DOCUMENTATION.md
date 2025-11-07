# 🏠 Fix4Home API Documentation

## 🎯 Quick Start for Frontend Developers

**Muốn test API ngay lập tức?** → Mở file này trong browser:
```
docs/openapi/index.html
```

Hoặc dùng web server (recommended):
```bash
cd docs/openapi

# Windows
serve.bat

# Linux/Mac
./serve.sh

# Sau đó mở: http://localhost:8000/index.html
```

## 📚 Tài Liệu Có Sẵn

### 🌟 OpenAPI Documentation (MỚI - RECOMMENDED!)

**Đây là cách tốt nhất để frontend team sử dụng API:**

| Công cụ | File | Mục đích |
|---------|------|----------|
| 🏠 **Documentation Hub** | [`docs/openapi/index.html`](./docs/openapi/index.html) | Trang chính, tổng quan tất cả |
| 📘 **Swagger UI** | [`docs/openapi/swagger-ui.html`](./docs/openapi/swagger-ui.html) | Test API trực tiếp trong browser |
| 📄 **ReDoc** | [`docs/openapi/redoc.html`](./docs/openapi/redoc.html) | Xem documentation đẹp, dễ đọc |
| ⚡ **RapiDoc** | [`docs/openapi/rapidoc.html`](./docs/openapi/rapidoc.html) | Advanced features, dark theme |
| 🧪 **API Tester** | [`docs/openapi/test-api.html`](./docs/openapi/test-api.html) | Test nhanh, không cần cài đặt |

### 📖 Hướng Dẫn Chi Tiết

| Tài liệu | Đường dẫn | Nội dung |
|----------|-----------|----------|
| **OpenAPI Spec** | [`docs/openapi/openapi.yaml`](./docs/openapi/openapi.yaml) | Định nghĩa API đầy đủ (OpenAPI 3.0) |
| **Setup Guide** | [`docs/openapi/README.md`](./docs/openapi/README.md) | Hướng dẫn đầy đủ về OpenAPI docs |
| **Frontend Integration** | [`docs/openapi/FRONTEND_INTEGRATION.md`](./docs/openapi/FRONTEND_INTEGRATION.md) | Tích hợp vào React/Vue/Angular |
| **Quick Reference** | [`docs/openapi/QUICK_REFERENCE.md`](./docs/openapi/QUICK_REFERENCE.md) | Tra cứu API nhanh |
| **Summary** | [`docs/openapi/SUMMARY.md`](./docs/openapi/SUMMARY.md) | Tổng kết toàn bộ |

### 📋 Tài Liệu Khác

| Loại | Đường dẫn |
|------|-----------|
| API Guide | [`docs/api/README_API.md`](./docs/api/README_API.md) |
| Postman Collection | [`docs/collections/Fix4Home_API_Collection.json`](./docs/collections/Fix4Home_API_Collection.json) |
| Database Schema | [`docs/database/database_schema_complete.sql`](./docs/database/database_schema_complete.sql) |
| Testing Guide | [`docs/testing/`](./docs/testing/) |

## 🚀 Bắt Đầu Nhanh (3 Bước)

### Bước 1: Xem Documentation
```bash
# Mở trong browser (double-click)
docs/openapi/index.html

# Hoặc với web server
cd docs/openapi && ./serve.sh
# Mở: http://localhost:8000/index.html
```

### Bước 2: Test API
1. Mở `swagger-ui.html` hoặc `test-api.html`
2. Register account: `POST /api/v1/auth/register`
3. Login: `POST /api/v1/auth/login` → lấy JWT token
4. Click **"Authorize" 🔓**, nhập: `Bearer YOUR_TOKEN`
5. Test các endpoints!

### Bước 3: Tích Hợp Frontend

#### Option A: Generate Client SDK (Recommended)
```bash
cd docs/openapi

# Windows
generate-clients.bat
# Chọn 1 (TypeScript/Axios)

# Linux/Mac
./generate-clients.sh
# Chọn 1 (TypeScript/Axios)

# SDK được tạo tại: ./clients/typescript/
```

#### Option B: Manual Integration
```typescript
// Tạo file: src/services/api.ts
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8100/api/v1',
  headers: { 'Content-Type': 'application/json' }
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export const authAPI = {
  login: (email, password) => 
    api.post('/auth/login', { email, password }),
  register: (data) => 
    api.post('/auth/register', data),
};

export const serviceAPI = {
  getAll: () => api.get('/services'),
  getById: (id) => api.get(`/services/${id}`),
};

// ... thêm các API khác
```

Xem chi tiết: [`docs/openapi/FRONTEND_INTEGRATION.md`](./docs/openapi/FRONTEND_INTEGRATION.md)

## 📊 API Overview

### Base URLs
- **Local**: `http://localhost:8100/api/v1`
- **Staging**: `https://staging-api.fix4home.com/api/v1`
- **Production**: `https://api.fix4home.com/api/v1`

### Statistics
- **100+ Endpoints** được document đầy đủ
- **14 Categories**: Auth, Customer, Technician, Services, Payments, Chat, etc.
- **3 User Roles**: Customer, Technician, Admin
- **OpenAPI 3.0.3** standard
- **JWT Authentication**

### Main Categories
1. 🔐 **Authentication** - Register, Login, Logout
2. 👥 **Customer Management** - Profile, Addresses
3. 🔧 **Technician Management** - Profile, Skills, Availability
4. 🛠️ **Service Management** - Service Catalog
5. 📋 **Service Requests** - Request Lifecycle
6. 📝 **Service Posts** - Customer Posts & Responses
7. 💡 **Consultations** - Consultation System
8. 💰 **Payments** - Payment Processing
9. ⭐ **Feedback** - Reviews & Ratings
10. 🔔 **Notifications** - User Notifications
11. 💬 **Chat** - Real-time Messaging
12. 🚨 **Complaints** - Complaint Handling
13. 👑 **Admin** - System Management
14. 🧪 **Testing** - Health Checks

## 🎯 Use Cases

### Frontend Developer
```bash
1. Mở swagger-ui.html để xem API
2. Test authentication flow
3. Generate TypeScript client SDK
4. Copy generated code vào project
5. Start coding!
```

### Mobile Developer (iOS/Android)
```bash
1. Generate Swift/Kotlin SDK
   ./generate-clients.sh
   # Chọn 7 (Swift) hoặc 8 (Kotlin)
   
2. Import SDK vào project
3. Configure base URL
4. Start building app!
```

### QA/Tester
```bash
1. Mở test-api.html hoặc swagger-ui.html
2. Test từng endpoint
3. Verify responses
4. Report issues với endpoint ID
```

### Product Manager
```bash
1. Mở index.html để xem overview
2. Review các features đã implement
3. Share docs với stakeholders
4. Track API changes
```

## 💡 Ví Dụ Sử Dụng

### Login và Get Profile
```javascript
// 1. Login
const loginResponse = await fetch('http://localhost:8100/api/v1/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'customer@example.com',
    password: 'password123'
  })
});

const { token } = (await loginResponse.json()).data;

// 2. Get Profile
const profileResponse = await fetch('http://localhost:8100/api/v1/customers/profile', {
  headers: { 
    'Authorization': `Bearer ${token}` 
  }
});

const profile = (await profileResponse.json()).data;
console.log(profile);
```

### Create Service Request
```javascript
const response = await fetch('http://localhost:8100/api/v1/service-requests', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    serviceId: 1,
    addressId: 1,
    description: 'Leaking pipe needs repair',
    scheduledTime: '2024-11-05T10:00:00Z'
  })
});

const serviceRequest = (await response.json()).data;
```

### Send Chat Message
```javascript
const response = await fetch(`http://localhost:8100/api/v1/conversations/${conversationId}/messages`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    content: 'Hello, when can you start?',
    messageType: 'TEXT'
  })
});
```

## 🔧 Generate Client SDKs

Hỗ trợ 50+ ngôn ngữ:

```bash
cd docs/openapi

# Chạy script
./generate-clients.sh  # Linux/Mac
generate-clients.bat   # Windows

# Chọn ngôn ngữ:
1. TypeScript/Axios (React, Vue, Angular)
2. JavaScript
3. Java
4. Python
5. Go
6. PHP
7. Swift (iOS)
8. Kotlin (Android)
9. C#
10. Tất cả
```

Generated SDKs sẽ ở: `docs/openapi/clients/`

## 📱 Platform Support

| Platform | SDK | Status |
|----------|-----|--------|
| **Web** | TypeScript/JavaScript | ✅ Ready |
| **React** | TypeScript/Axios | ✅ Ready |
| **Vue.js** | TypeScript/Axios | ✅ Ready |
| **Angular** | TypeScript/Axios | ✅ Ready |
| **iOS** | Swift 5 | ✅ Ready |
| **Android** | Kotlin | ✅ Ready |
| **Java** | Java | ✅ Ready |
| **Python** | Python | ✅ Ready |
| **.NET** | C# | ✅ Ready |

## 🐛 Troubleshooting

### CORS Errors
```bash
# Cần serve documentation với web server
cd docs/openapi && ./serve.sh
```

### JWT Token không hoạt động
```bash
# Đảm bảo có prefix "Bearer "
Authorization: Bearer YOUR_TOKEN
```

### Không generate được client
```bash
# Cài đặt Node.js và openapi-generator
npm install -g @openapitools/openapi-generator-cli
```

### API không response
```bash
# Kiểm tra backend có chạy không
curl http://localhost:8100/api/v1/test/health
```

## 📞 Liên Hệ & Support

- **Documentation Issues**: Xem [`docs/openapi/README.md`](./docs/openapi/README.md)
- **API Issues**: Xem [`docs/api/README_API.md`](./docs/api/README_API.md)
- **Testing**: Xem [`scripts/testing/`](./scripts/testing/)
- **Backend Team**: Contact qua internal channels

## 🔗 Useful Links

- [Main Documentation Hub](./docs/openapi/index.html)
- [Swagger UI - Test API](./docs/openapi/swagger-ui.html)
- [Frontend Integration Guide](./docs/openapi/FRONTEND_INTEGRATION.md)
- [Quick Reference Card](./docs/openapi/QUICK_REFERENCE.md)
- [Full Setup Guide](./docs/openapi/README.md)
- [OpenAPI Specification](./docs/openapi/openapi.yaml)

## 🎉 Ready to Build!

Bạn đã có đầy đủ:
- ✅ OpenAPI specification (industry standard)
- ✅ Interactive documentation (4 viewers)
- ✅ Client SDK generators (50+ languages)
- ✅ Frontend integration guides
- ✅ Quick testing tools
- ✅ Professional UI

**Start building your frontend now!** 🚀

---

**Version**: 1.0.0 | **Last Updated**: November 3, 2024 | **Status**: ✅ Production Ready

