# 📚 Fix4Home Backend - Documentation Index

## 🗂️ Tài liệu đã được tổ chức lại

Tất cả documentation và scripts đã được sắp xếp vào các thư mục chuyên biệt để dễ quản lý:

### 📖 Documentation → `docs/`
```
docs/
├── README.md                    # Hướng dẫn tổng quan
├── api/                        # API Documentation
├── openapi/                    # OpenAPI Specification & Interactive Docs ⭐ NEW!
├── implementation/             # Feature Implementation Guides
├── testing/                    # Testing Documentation  
├── database/                   # Database Schema & Files
└── collections/               # Postman Collections
```

### ⚡ Scripts → `scripts/`
```
scripts/
├── testing/                    # PowerShell Test Scripts
└── examples/                  # Example Scripts (curl, etc.)
```

## 🔍 Tìm nhanh

| Bạn muốn | Đi tới |
|----------|--------|
| 🌟 **OpenAPI Interactive Docs** | `docs/openapi/index.html` ⭐ **NEW!** |
| 📘 Swagger UI | `docs/openapi/swagger-ui.html` |
| 📄 ReDoc | `docs/openapi/redoc.html` |
| 📋 API Documentation | `docs/api/` |
| 🔧 Hướng dẫn triển khai feature | `docs/implementation/` |
| 🧪 Testing & QA | `docs/testing/` |
| 🗄️ Database schema | `docs/database/` |
| 📮 Postman collections | `docs/collections/` |
| ⚡ Test scripts | `scripts/testing/` |
| 📝 Curl examples | `scripts/examples/` |

## 📌 Quick Start

### 🎯 Cho Frontend Developers:
1. **OpenAPI Interactive Docs**: Mở `docs/openapi/index.html` trong browser
2. **Swagger UI**: Test API trực tiếp tại `docs/openapi/swagger-ui.html`
3. **Generate Client SDK**: Chạy `docs/openapi/generate-clients.bat` (Windows) hoặc `./generate-clients.sh` (Linux/Mac)
4. **Frontend Integration**: Đọc `docs/openapi/FRONTEND_INTEGRATION.md`

### 🧪 Cho Testing:
1. **API Documentation**: Bắt đầu từ `docs/README.md`
2. **Testing**: Chạy `scripts/testing/complete_api_test_suite.ps1`
3. **Postman**: Import `docs/collections/Fix4Home_Complete_Postman_Collection.json`
4. **Quick Test**: Mở `docs/openapi/test-api.html` trong browser

## 🚀 OpenAPI Documentation (NEW!)

### Các cách xem documentation:
1. **Swagger UI** (Recommended for testing)
   ```bash
   # Open in browser
   docs/openapi/swagger-ui.html
   ```
   - Test endpoints trực tiếp
   - JWT authentication
   - Request/Response examples

2. **ReDoc** (Beautiful documentation)
   ```bash
   docs/openapi/redoc.html
   ```
   - Clean design
   - Easy to read
   - Three-panel layout

3. **RapiDoc** (Advanced features)
   ```bash
   docs/openapi/rapidoc.html
   ```
   - Dark theme
   - Advanced search
   - Multiple layouts

4. **API Tester** (Quick testing)
   ```bash
   docs/openapi/test-api.html
   ```
   - Test API trong browser
   - Không cần cài đặt gì
   - JWT token management

### Serve documentation với web server:
```bash
cd docs/openapi

# Windows
serve.bat

# Linux/Mac
./serve.sh

# Sau đó mở: http://localhost:8000/index.html
```

### Generate Client SDKs:
```bash
cd docs/openapi

# Windows
generate-clients.bat

# Linux/Mac
./generate-clients.sh

# Chọn ngôn ngữ: TypeScript, JavaScript, Java, Python, Go, PHP, Swift, Kotlin, C#...
```

---
**🎯 Lưu ý**: File này có thể xóa sau khi team đã làm quen với cấu trúc mới.
