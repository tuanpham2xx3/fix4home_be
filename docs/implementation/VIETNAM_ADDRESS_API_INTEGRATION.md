# 🇻🇳 VIETNAM ADMINISTRATIVE API INTEGRATION - FIX4HOME BACKEND

## 📖 Tổng Quan

Tài liệu này mô tả việc tích hợp Vietnam Administrative API vào hệ thống Fix4Home Backend để cung cấp thông tin địa chỉ chuẩn và hỗ trợ tìm kiếm theo khu vực địa lý Việt Nam.

### 🎯 Mục Tiêu Tích Hợp

- ✅ **Cung cấp dữ liệu địa chỉ chuẩn**: Danh sách đầy đủ tỉnh thành và phường/xã Việt Nam
- ✅ **Validation địa chỉ**: Kiểm tra tính hợp lệ của province_code và ward_code
- ✅ **Tìm kiếm theo khu vực**: Hỗ trợ tìm kiếm thợ kỹ thuật và dịch vụ theo tỉnh/huyện/xã
- ✅ **Tối ưu UX**: Autocomplete và suggestion cho việc nhập địa chỉ

### 📡 Vietnam Administrative API

- **Base URL**: `http://localhost:8080/api/v1` (có thể cấu hình)
- **Documentation**: Xem file `.microservice/API_DOCUMENTATION_ADDRESS.md`
- **Port**: 8080 (khác với Fix4Home Backend chạy port 8100)
- **Authentication**: Không cần (miễn phí)

## 🏗️ Kiến Trúc Tích Hợp

### 1. Flow Diagram

```
Fix4Home Backend (8100) ←→ Vietnam Address API (8080)
         │
         ├── AddressApiService (Integration Layer)
         ├── AddressController (API Endpoints)
         ├── CustomerService (Address Management)
         └── Enhanced Search (Location-based)
```

### 2. Database Schema Changes

#### Bảng `addresses` - Các cột mới:
```sql
-- V013 Migration
ALTER TABLE addresses ADD COLUMN province_code VARCHAR(10) NULL;
ALTER TABLE addresses ADD COLUMN ward_code VARCHAR(10) NULL;

-- Indexes for performance
CREATE INDEX idx_addresses_province_code ON addresses(province_code);
CREATE INDEX idx_addresses_ward_code ON addresses(ward_code);
CREATE INDEX idx_addresses_province_ward ON addresses(province_code, ward_code);
```

### 3. Configuration

#### application.properties
```properties
# Vietnam Administrative API Configuration
vietnam.address.api.url=${VIETNAM_ADDRESS_API_URL:http://localhost:8080/api/v1}
vietnam.address.api.timeout=5000
```

## 📦 Components

### 1. DTOs

#### ProvinceDTO
```java
public class ProvinceDTO {
    private String code;        // "01", "02"
    private String name;        // "Hà Nội", "Hồ Chí Minh"
    private String slug;        // "ha-noi", "ho-chi-minh"
    private String type;        // "thanh-pho", "tinh"
    private String nameWithType; // "Thành phố Hà Nội"
    private String codeName;    // "ha_noi"
}
```

#### WardDTO
```java
public class WardDTO {
    private String code;         // "00001"
    private String name;         // "Phúc Xá"
    private String slug;         // "phuc-xa"
    private String type;         // "phuong", "xa", "thi-tran"
    private String nameWithType; // "Phường Phúc Xá"
    private String path;         // "Phúc Xá, Ba Đình, Hà Nội"
    private String pathWithType; // "Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội"
    private String parentCode;   // "001"
    private ProvinceDTO province;
}
```

#### Enhanced AddressDTO
```java
public class AddressDTO {
    // Existing fields...
    private String ward;
    private String district;
    private String city;
    
    // New Vietnam API integration fields
    private String provinceCode; // "01", "02"
    private String wardCode;     // "00001", "00002"
    
    private BigDecimal latitude;
    private BigDecimal longitude;
}
```

### 2. Core Services

#### AddressApiService
**Chức năng chính:**
- `getAllProvinces()` - Lấy tất cả tỉnh thành
- `searchProvinces(keyword, limit)` - Tìm kiếm tỉnh
- `getProvinceByCode(code)` - Lấy tỉnh theo mã
- `getWardsByProvince(provinceCode, keyword, limit)` - Lấy phường/xã theo tỉnh
- `searchWards(keyword, provinceCode, limit)` - Tìm kiếm phường/xã
- `getWardByCode(code)` - Lấy phường/xã theo mã
- `validateAddress(provinceCode, wardCode)` - Validation địa chỉ
- `globalSearch(keyword, limit)` - Tìm kiếm toàn cục
- `isApiHealthy()` - Kiểm tra tình trạng API

#### Enhanced CustomerService
**Cập nhật:**
- Address validation khi create/update
- Hỗ trợ province_code và ward_code
- Search addresses by province/ward
- Integration với AddressApiService

## 🔗 API Endpoints

### 1. Address Helper APIs (`/api/v1/addresses`)

#### Provinces
```http
GET /api/v1/addresses/provinces
GET /api/v1/addresses/provinces/search?keyword=hà&limit=10
GET /api/v1/addresses/provinces/{code}
GET /api/v1/addresses/provinces/types
GET /api/v1/addresses/provinces/major-cities
```

#### Wards
```http
GET /api/v1/addresses/provinces/{provinceCode}/wards?keyword=&limit=50
GET /api/v1/addresses/wards/search?keyword=phúc&provinceCode=01&limit=20
GET /api/v1/addresses/wards/{code}
GET /api/v1/addresses/wards/types
```

#### Validation & Search
```http
POST /api/v1/addresses/validate
Content-Type: application/json
{
  "provinceCode": "01",
  "wardCode": "00001"
}

GET /api/v1/addresses/search?keyword=hà nội&limit=20
GET /api/v1/addresses/quick-search?q=phúc xá
GET /api/v1/addresses/health
```

### 2. Enhanced Customer Address APIs

#### Enhanced Address Management
```http
POST /api/v1/customers/addresses
Content-Type: application/json
{
  "recipientName": "Nguyễn Văn A",
  "recipientPhone": "0901234567",
  "addressLine": "123 Đường ABC",
  "ward": "Phúc Xá",
  "district": "Ba Đình",
  "city": "Hà Nội",
  "provinceCode": "01",        // New field
  "wardCode": "00001",         // New field
  "latitude": 21.0285,
  "longitude": 105.8542
}

PUT /api/v1/customers/addresses/{addressId}
// Same request body structure
```

#### Location-based Search
```http
GET /api/v1/customers/addresses/search/by-province?provinceCode=01
GET /api/v1/customers/addresses/search/by-ward?wardCode=00001
```

### 3. Response Examples

#### Province List
```json
{
  "success": true,
  "message": "Provinces retrieved successfully",
  "data": [
    {
      "code": "01",
      "name": "Hà Nội",
      "slug": "ha-noi",
      "type": "thanh-pho",
      "nameWithType": "Thành phố Hà Nội",
      "codeName": "ha_noi"
    },
    {
      "code": "79",
      "name": "Hồ Chí Minh",
      "slug": "ho-chi-minh",
      "type": "thanh-pho",
      "nameWithType": "Thành phố Hồ Chí Minh",
      "codeName": "ho_chi_minh"
    }
  ]
}
```

#### Ward Details
```json
{
  "success": true,
  "message": "Ward retrieved successfully",
  "data": {
    "code": "00001",
    "name": "Phúc Xá",
    "slug": "phuc-xa",
    "type": "phuong",
    "nameWithType": "Phường Phúc Xá",
    "path": "Phúc Xá, Ba Đình, Hà Nội",
    "pathWithType": "Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội",
    "parentCode": "001",
    "province": {
      "code": "01",
      "name": "Hà Nội",
      "nameWithType": "Thành phố Hà Nội",
      "type": "thanh-pho"
    }
  }
}
```

#### Address Validation
```json
{
  "success": true,
  "message": "Address is valid",
  "data": {
    "valid": true,
    "message": "Address is valid",
    "wardData": {
      "code": "00001",
      "name": "Phúc Xá",
      "path": "Phúc Xá, Ba Đình, Hà Nội"
    }
  }
}
```

## 🔍 Enhanced Search Capabilities

### 1. Location-based Technician Search

```java
// TechnicianService có thể được mở rộng để tìm kiếm theo province/ward codes
public List<TechnicianSearchResultDTO> findTechniciansByProvince(String provinceCode) {
    // Implementation using AddressApiService to get province info
    // Then search technicians in that province
}

public List<TechnicianSearchResultDTO> findTechniciansByWard(String wardCode) {
    // Similar implementation for ward-level search
}
```

### 2. Service Post Location Enhancement

```java
// ServicePostService có thể được mở rộng
public List<ServicePostSearchResultDTO> searchPostsByProvince(String provinceCode) {
    // Find service posts in specific province
}
```

### 3. Advanced Search Integration

Các endpoint advanced search hiện tại có thể được mở rộng:

```http
POST /api/v1/technicians/search/advanced
Content-Type: application/json
{
  "keyword": "sửa điện",
  "provinceCode": "01",       // New filter
  "wardCode": "00001",        // New filter
  "latitude": 21.0285,
  "longitude": 105.8542,
  "radius": 10,
  "minRating": 4.0
}
```

## 💡 Use Cases

### 1. Frontend Address Autocomplete

```javascript
// Step 1: Get provinces for dropdown
const provinces = await fetch('/api/v1/addresses/provinces')
  .then(res => res.json());

// Step 2: User selects province, get wards
const wards = await fetch(`/api/v1/addresses/provinces/${provinceCode}/wards`)
  .then(res => res.json());

// Step 3: Create address with codes
const addressData = {
  recipientName: "Nguyễn Văn A",
  addressLine: "123 Đường ABC",
  ward: selectedWard.name,
  district: "Ba Đình", 
  city: "Hà Nội",
  provinceCode: selectedProvince.code,
  wardCode: selectedWard.code
};
```

### 2. Service Area Management

```javascript
// Thợ kỹ thuật có thể đăng ký phục vụ các tỉnh/huyện cụ thể
const serviceAreas = await fetch('/api/v1/addresses/provinces/01/wards')
  .then(res => res.json());

// Khách hàng tìm thợ theo khu vực
const technicians = await fetch('/api/v1/technicians/search/advanced', {
  method: 'POST',
  body: JSON.stringify({
    provinceCode: "01",
    wardCode: "00001",
    availableNow: true
  })
});
```

### 3. Analytics & Reporting

```javascript
// Thống kê theo tỉnh thành
const customersByProvince = await Promise.all(
  provinces.map(async province => {
    const addresses = await fetch(`/api/v1/addresses/search?provinceCode=${province.code}`);
    return { province: province.name, count: addresses.data.length };
  })
);
```

## 🛠️ Development & Testing

### 1. Local Setup

```bash
# 1. Start Vietnam Administrative API (port 8080)
cd .microservice
# Follow setup instructions in API_DOCUMENTATION_ADDRESS.md

# 2. Start Fix4Home Backend (port 8100)
mvn spring-boot:run

# 3. Test integration
curl "http://localhost:8100/api/v1/addresses/health"
```

### 2. Testing Endpoints

```bash
# Test province list
curl "http://localhost:8100/api/v1/addresses/provinces"

# Test province search
curl "http://localhost:8100/api/v1/addresses/provinces/search?keyword=hà"

# Test ward search
curl "http://localhost:8100/api/v1/addresses/provinces/01/wards?keyword=phúc"

# Test address validation
curl -X POST "http://localhost:8100/api/v1/addresses/validate" \
  -H "Content-Type: application/json" \
  -d '{"provinceCode":"01","wardCode":"00001"}'

# Test customer address creation with codes
curl -X POST "http://localhost:8100/api/v1/customers/addresses" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "recipientName": "Test User",
    "recipientPhone": "0901234567",
    "addressLine": "123 Test Street",
    "ward": "Phúc Xá",
    "district": "Ba Đình",
    "city": "Hà Nội",
    "provinceCode": "01",
    "wardCode": "00001",
    "latitude": 21.0285,
    "longitude": 105.8542
  }'
```

### 3. Error Handling

#### API Unavailable
```java
// AddressApiService gracefully handles API downtime
// Returns empty lists instead of throwing exceptions
// Logs warnings for monitoring
```

#### Invalid Codes
```java
// Validation returns clear error messages
{
  "success": false,
  "message": "Ward 99999 does not belong to province 01"
}
```

## 📊 Monitoring & Maintenance

### 1. Health Checks

```http
GET /api/v1/addresses/health
```

### 2. Logging

```java
// AddressApiService logs all API calls
log.info("Fetching all provinces from Vietnam Address API");
log.info("Successfully fetched {} provinces", response.getData().size());
log.warn("Address API health check failed: {}", e.getMessage());
```

### 3. Performance Considerations

- **Caching**: Consider implementing Redis cache for province/ward data
- **Rate Limiting**: Monitor API call frequency
- **Failover**: Graceful degradation when external API is down
- **Indexing**: Database indexes on province_code, ward_code for performance

## 🔄 Migration Guide

### 1. Database Migration

```sql
-- Run V013__Add_Province_Ward_Codes_To_Addresses.sql
-- This adds province_code and ward_code columns with proper indexes
```

### 2. Existing Data

```sql
-- Optional: Populate existing addresses with codes
-- This would require mapping existing city/district names to codes
UPDATE addresses SET 
  province_code = (SELECT code FROM provinces WHERE name = addresses.city),
  ward_code = (SELECT code FROM wards WHERE name = addresses.ward)
WHERE province_code IS NULL OR ward_code IS NULL;
```

### 3. Frontend Migration

```javascript
// Update frontend forms to include province/ward selection
// Enhance address autocomplete with API integration
// Add validation for address codes
```

## 🚀 Future Enhancements

### 1. Advanced Features

- **District/County Support**: Thêm hỗ trợ level quận/huyện
- **Geocoding**: Tự động convert address text sang coordinates
- **Address Suggestion**: Smart address completion
- **Delivery Zone**: Quản lý khu vực giao hàng theo địa chỉ

### 2. Performance Optimizations

- **Local Cache**: Cache popular province/ward data
- **Background Sync**: Định kỳ sync data từ external API
- **CDN Integration**: Phân phối data qua CDN

### 3. Integration Extensions

- **Map Integration**: Tích hợp với Google Maps/OpenStreetMap
- **Shipping Calculator**: Tính phí ship theo khoảng cách
- **Weather API**: Thông tin thời tiết theo địa điểm

---

## 📞 Support

Nếu có vấn đề với tích hợp Vietnam Administrative API:

1. Kiểm tra `/api/v1/addresses/health` endpoint
2. Xem logs của AddressApiService
3. Verify Vietnam Administrative API đang chạy trên port 8080
4. Check network connectivity giữa các services

**Version**: 1.0.0  
**Last Updated**: 04/01/2025  
**Author**: Fix4Home Development Team 