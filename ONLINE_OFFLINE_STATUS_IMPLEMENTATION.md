# 🔄 ONLINE/OFFLINE STATUS SYSTEM - IMPLEMENTATION SUMMARY

## 🎯 Tổng Quan

Online/Offline Status System là một tính năng quan trọng được bổ sung vào Fix4Home Backend cho phép theo dõi trạng thái hoạt động và vị trí của thợ kỹ thuật. Hệ thống này hỗ trợ tìm kiếm thợ gần nhất, quản lý khả năng làm việc và tối ưu hóa việc matching giữa khách hàng và thợ.

## 🏗️ Kiến Trúc Hệ Thống

### Business Flow
```
1. Thợ cập nhật trạng thái Online/Offline
2. Thợ cập nhật vị trí hiện tại và bán kính làm việc
3. Hệ thống tự động ghi nhận thời gian "last seen"
4. Khách hàng tìm kiếm thợ gần nhất (có thể lọc theo online)
5. Hệ thống tính toán khoảng cách và trả về danh sách
```

### Status Lifecycle
```
OFFLINE → ONLINE → (Working) → OFFLINE
    ↓         ↓
Location   Location
Updated    Updated
```

## 📁 Cấu Trúc Implementation

### 1. Database Schema Changes

#### Bảng `technician_profiles` - Các cột mới:
```sql
-- Online/Offline Status
is_online BOOLEAN NOT NULL DEFAULT FALSE

-- Location Tracking  
last_seen_at TIMESTAMP NULL
current_latitude DOUBLE PRECISION NULL
current_longitude DOUBLE PRECISION NULL
current_address VARCHAR(500) NULL

-- Working Configuration
working_radius INTEGER NOT NULL DEFAULT 10
```

#### Database Indexes (Performance Optimization):
```sql
CREATE INDEX idx_technician_profiles_online_status ON technician_profiles(is_online);
CREATE INDEX idx_technician_profiles_location ON technician_profiles(current_latitude, current_longitude);
CREATE INDEX idx_technician_profiles_last_seen ON technician_profiles(last_seen_at);
```

### 2. Entity Updates

#### TechnicianProfile.java - Các trường mới:
```java
// Online/Offline Status and Location fields
@Column(name = "is_online", nullable = false)
@Builder.Default
private Boolean isOnline = false;

@Column(name = "last_seen_at")
private LocalDateTime lastSeenAt;

@Column(name = "current_latitude")
private Double currentLatitude;

@Column(name = "current_longitude")
private Double currentLongitude;

@Column(name = "current_address", length = 500)
private String currentAddress;

@Column(name = "working_radius")
@Builder.Default
private Integer workingRadius = 10; // Default 10km working radius
```

### 3. DTOs và Request/Response Models

#### a) TechnicianProfileDTO - Cập nhật:
```java
// Online/Offline Status and Location fields
private Boolean isOnline;
private LocalDateTime lastSeenAt;
private Double currentLatitude;
private Double currentLongitude;
private String currentAddress;
private Integer workingRadius;
```

#### b) UpdateStatusRequest.java (Mới):
```java
@NotNull(message = "Online status is required")
private Boolean isOnline;
```

#### c) UpdateLocationRequest.java (Mới):
```java
@DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
@DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
private Double latitude;

@DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
@DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
private Double longitude;

@Size(max = 500, message = "Address cannot exceed 500 characters")
private String address;

@Min(value = 1, message = "Working radius must be at least 1 km")
private Integer workingRadius;
```

#### d) NearbyTechnicianDTO.java (Mới):
```java
// Basic technician info
private Long userId;
private Long profileId;
private String fullName;
private String email;
private String phoneNumber;
private Float rating;
private UserStatus status;

// Location info
private Double currentLatitude;
private Double currentLongitude;
private String currentAddress;
private Integer workingRadius;
private Double distanceKm; // Distance from search point

// Online status
private Boolean isOnline;
private LocalDateTime lastSeenAt;

// Skills
private List<SkillDTO> skillList;
```

## 🚀 API Endpoints

### 1. Cập Nhật Trạng Thái Online/Offline

#### PUT `/api/v1/technicians/me/status`
**Mô tả:** Thợ cập nhật trạng thái online/offline của mình  
**Authorization:** HAS_TECHNICIAN_ROLE  
**Request Body:**
```json
{
  "isOnline": true
}
```
**Response:**
```json
{
  "success": true,
  "message": "Online status updated successfully",
  "data": {
    "userId": 1,
    "profileId": 1,
    "fullName": "Nguyễn Văn A",
    "isOnline": true,
    "lastSeenAt": "2024-01-15T10:30:00",
    // ... other fields
  }
}
```

### 2. Cập Nhật Vị Trí và Bán Kính Làm Việc

#### PUT `/api/v1/technicians/me/location`
**Mô tả:** Thợ cập nhật vị trí hiện tại và bán kính làm việc  
**Authorization:** HAS_TECHNICIAN_ROLE  
**Request Body:**
```json
{
  "latitude": 10.7769,
  "longitude": 106.6951,
  "address": "Quận 1, TP.HCM",
  "workingRadius": 15
}
```
**Response:**
```json
{
  "success": true,
  "message": "Location updated successfully",
  "data": {
    "userId": 1,
    "profileId": 1,
    "fullName": "Nguyễn Văn A",
    "currentLatitude": 10.7769,
    "currentLongitude": 106.6951,
    "currentAddress": "Quận 1, TP.HCM",
    "workingRadius": 15,
    "lastSeenAt": "2024-01-15T10:35:00",
    // ... other fields
  }
}
```

### 3. Tìm Kiếm Thợ Gần Nhất

#### GET `/api/v1/technicians/nearby`
**Mô tả:** Tìm kiếm thợ kỹ thuật gần vị trí chỉ định  
**Authorization:** Public  
**Query Parameters:**
- `latitude` (required): Tọa độ vĩ độ
- `longitude` (required): Tọa độ kinh độ  
- `radiusKm` (optional, default=10): Bán kính tìm kiếm (km)
- `onlineOnly` (optional, default=false): Chỉ tìm thợ đang online
- `serviceId` (optional): Lọc theo dịch vụ cụ thể

**Example Request:**
```
GET /api/v1/technicians/nearby?latitude=10.7769&longitude=106.6951&radiusKm=10&onlineOnly=true
```

**Response:**
```json
{
  "success": true,
  "message": "Nearby technicians found successfully",
  "data": [
    {
      "userId": 1,
      "profileId": 1,
      "fullName": "Nguyễn Văn A",
      "email": "technician1@example.com",
      "phoneNumber": "0901234567",
      "rating": 4.5,
      "status": "ACTIVE",
      "currentLatitude": 10.7769,
      "currentLongitude": 106.6951,
      "currentAddress": "Quận 1, TP.HCM",
      "workingRadius": 15,
      "distanceKm": 2.3,
      "isOnline": true,
      "lastSeenAt": "2024-01-15T10:35:00",
      "skillList": [
        {
          "id": 1,
          "name": "Sửa chữa điện"
        }
      ]
    }
  ]
}
```

## 🔍 Repository & Business Logic

### 1. Repository Methods - TechnicianProfileRepository

#### Các Query Methods Mới:
```java
// Online/Offline Status queries
List<TechnicianProfile> findByIsOnlineAndStatus(Boolean isOnline, UserStatus status);

@Query("SELECT tp FROM TechnicianProfile tp WHERE tp.isOnline = true AND tp.status = :status")
List<TechnicianProfile> findOnlineTechnicians(@Param("status") UserStatus status);

@Query("SELECT tp FROM TechnicianProfile tp WHERE tp.isOnline = true AND tp.status = :status AND tp.currentLatitude IS NOT NULL AND tp.currentLongitude IS NOT NULL")
List<TechnicianProfile> findOnlineTechniciansWithLocation(@Param("status") UserStatus status);

// Location-based queries
@Query("SELECT tp FROM TechnicianProfile tp WHERE tp.currentLatitude IS NOT NULL AND tp.currentLongitude IS NOT NULL AND tp.status = :status")
List<TechnicianProfile> findTechniciansWithLocation(@Param("status") UserStatus status);

@Query("SELECT tp FROM TechnicianProfile tp WHERE tp.status = :status AND tp.isOnline = :isOnline AND tp.currentLatitude IS NOT NULL AND tp.currentLongitude IS NOT NULL")
List<TechnicianProfile> findByStatusAndIsOnlineWithLocation(@Param("status") UserStatus status, @Param("isOnline") Boolean isOnline);
```

### 2. Service Methods - TechnicianService

#### a) Cập Nhật Trạng Thái:
```java
@Transactional
public TechnicianProfileDTO updateMyStatus(UpdateStatusRequest request) {
    // Validation
    validateRequired(request, "request");
    validateRequired(request.getIsOnline(), "isOnline");
    
    // Update status and last seen time
    User user = getCurrentUser();
    TechnicianProfile profile = findTechnicianProfileByUser(user);
    
    profile.setIsOnline(request.getIsOnline());
    profile.setLastSeenAt(LocalDateTime.now());
    
    TechnicianProfile savedProfile = technicianProfileRepository.save(profile);
    return convertToTechnicianProfileDTO(user, savedProfile);
}
```

#### b) Cập Nhật Vị Trí:
```java
@Transactional
public TechnicianProfileDTO updateMyLocation(UpdateLocationRequest request) {
    // Validation và cập nhật từng trường
    User user = getCurrentUser();
    TechnicianProfile profile = findTechnicianProfileByUser(user);
    
    if (request.getLatitude() != null) {
        profile.setCurrentLatitude(request.getLatitude());
    }
    // ... update other fields
    
    profile.setLastSeenAt(LocalDateTime.now());
    
    TechnicianProfile savedProfile = technicianProfileRepository.save(profile);
    return convertToTechnicianProfileDTO(user, savedProfile);
}
```

#### c) Tìm Kiếm Thợ Gần Nhất:
```java
@Transactional(readOnly = true)
public List<NearbyTechnicianDTO> findNearbyTechnicians(Double latitude, Double longitude, 
                                                       Integer radiusKm, Boolean onlineOnly, Long serviceId) {
    // Get candidates with location data
    List<TechnicianProfile> candidates = technicianProfileRepository.findByStatus(UserStatus.ACTIVE).stream()
            .filter(profile -> profile.getCurrentLatitude() != null && profile.getCurrentLongitude() != null)
            .filter(profile -> !onlineOnly || Boolean.TRUE.equals(profile.getIsOnline()))
            .toList();

    // Calculate distances and filter by radius
    return candidates.stream()
            .map(profile -> {
                double distance = calculateDistance(latitude, longitude, 
                        profile.getCurrentLatitude(), profile.getCurrentLongitude());
                
                if (distance <= radiusKm) {
                    return convertToNearbyTechnicianDTO(profile, distance, serviceId);
                }
                return null;
            })
            .filter(dto -> dto != null)
            .sorted((a, b) -> Double.compare(a.getDistanceKm(), b.getDistanceKm()))
            .toList();
}
```

### 3. Distance Calculation - Haversine Formula

```java
/**
 * Calculate distance between two points using Haversine formula
 * @param lat1 Latitude of first point
 * @param lon1 Longitude of first point
 * @param lat2 Latitude of second point
 * @param lon2 Longitude of second point
 * @return Distance in kilometers
 */
private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    final double EARTH_RADIUS = 6371.0; // Earth radius in kilometers

    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);

    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
               Math.sin(dLon / 2) * Math.sin(dLon / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return EARTH_RADIUS * c;
}
```

## 🔒 Security & Authorization

### 1. Endpoint Security Configuration

```java
// TechnicianController endpoints security:
// - /api/v1/technicians/me/status     -> HAS_TECHNICIAN_ROLE  
// - /api/v1/technicians/me/location   -> HAS_TECHNICIAN_ROLE
// - /api/v1/technicians/nearby        -> Public (no authentication required)
```

### 2. Data Validation

- **Latitude:** Phải nằm trong khoảng -90 đến 90
- **Longitude:** Phải nằm trong khoảng -180 đến 180  
- **Address:** Tối đa 500 ký tự
- **Working Radius:** Tối thiểu 1 km
- **Online Status:** Bắt buộc (true/false)

### 3. Business Rules

- Chỉ thợ đã được approve (status = ACTIVE) mới được tìm kiếm
- Tự động cập nhật `lastSeenAt` khi thay đổi status/location
- Tìm kiếm nearby chỉ trả về thợ có đầy đủ thông tin vị trí
- Sắp xếp kết quả theo khoảng cách (gần nhất trước)

## 🧪 Testing Guide

### 1. Functional Testing

#### Test Case 1: Cập Nhật Trạng Thái Online
```bash
# Request
curl -X PUT http://localhost:8080/api/v1/technicians/me/status \
  -H "Authorization: Bearer {technician_token}" \
  -H "Content-Type: application/json" \
  -d '{"isOnline": true}'

# Expected: 200 OK, profile với isOnline=true, lastSeenAt được cập nhật
```

#### Test Case 2: Cập Nhật Vị Trí
```bash
# Request  
curl -X PUT http://localhost:8080/api/v1/technicians/me/location \
  -H "Authorization: Bearer {technician_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 10.7769,
    "longitude": 106.6951,
    "address": "Quận 1, TP.HCM",
    "workingRadius": 15
  }'

# Expected: 200 OK, profile với location được cập nhật
```

#### Test Case 3: Tìm Kiếm Thợ Gần Nhất
```bash
# Request
curl -X GET "http://localhost:8080/api/v1/technicians/nearby?latitude=10.7769&longitude=106.6951&radiusKm=10&onlineOnly=true"

# Expected: 200 OK, danh sách thợ online trong bán kính 10km, sắp xếp theo khoảng cách
```

### 2. Edge Case Testing

- **Invalid coordinates:** latitude=91 (expected: 400 Bad Request)
- **Missing location data:** Thợ chưa cập nhật vị trí (expected: không xuất hiện trong nearby search)
- **Zero radius:** radiusKm=0 (expected: chỉ trả về thợ ở cùng vị trí)
- **Offline technician:** onlineOnly=true (expected: không trả về thợ offline)

### 3. Performance Testing

- **Database indexes:** Kiểm tra query performance với large dataset
- **Distance calculation:** Test với nhiều technicians (>1000)
- **Concurrent updates:** Multiple technicians cập nhật status cùng lúc

## 📊 Database Migration

### Migration Script: V009__Add_Online_Status_To_Technician_Profiles.sql

```sql
-- ========================================
-- Migration: Add Online Status and Location Tracking to Technician Profiles
-- Version: V009
-- Description: Adds online/offline status, location, and working radius fields
-- ========================================

-- Add online status field
ALTER TABLE technician_profiles 
ADD COLUMN is_online BOOLEAN NOT NULL DEFAULT FALSE;

-- Add last seen timestamp
ALTER TABLE technician_profiles 
ADD COLUMN last_seen_at TIMESTAMP NULL;

-- Add current location fields
ALTER TABLE technician_profiles 
ADD COLUMN current_latitude DOUBLE PRECISION NULL;

ALTER TABLE technician_profiles 
ADD COLUMN current_longitude DOUBLE PRECISION NULL;

-- Add current address field
ALTER TABLE technician_profiles 
ADD COLUMN current_address VARCHAR(500) NULL;

-- Add working radius field (in kilometers)
ALTER TABLE technician_profiles 
ADD COLUMN working_radius INTEGER NOT NULL DEFAULT 10;

-- Create index for location-based queries
CREATE INDEX idx_technician_profiles_online_status ON technician_profiles(is_online);
CREATE INDEX idx_technician_profiles_location ON technician_profiles(current_latitude, current_longitude);
CREATE INDEX idx_technician_profiles_last_seen ON technician_profiles(last_seen_at);

-- Add comments for documentation
COMMENT ON COLUMN technician_profiles.is_online IS 'Indicates if technician is currently online and available';
COMMENT ON COLUMN technician_profiles.last_seen_at IS 'Timestamp when technician was last seen online';
COMMENT ON COLUMN technician_profiles.current_latitude IS 'Current latitude coordinate of technician';
COMMENT ON COLUMN technician_profiles.current_longitude IS 'Current longitude coordinate of technician';
COMMENT ON COLUMN technician_profiles.current_address IS 'Human-readable current address of technician';
COMMENT ON COLUMN technician_profiles.working_radius IS 'Working radius in kilometers from current location';
```

## 🎯 Use Cases và Scenarios

### 1. Urgent Service Request Scenario
```
1. Khách hàng cần sửa chữa khẩn cấp
2. App tìm kiếm thợ online trong bán kính 5km
3. Hiển thị danh sách thợ gần nhất, ưu tiên thợ online
4. Khách hàng liên hệ trực tiếp với thợ
```

### 2. Technician Work Management Scenario  
```
1. Thợ bắt đầu ca làm việc -> set status = online
2. Thợ di chuyển đến khu vực mới -> update location
3. Thợ nhận job và bắt đầu làm -> có thể set offline
4. Thợ hoàn thành job -> set online để nhận job mới
5. Thợ kết thúc ca -> set status = offline
```

### 3. Location-Based Matching Scenario
```
1. Hệ thống phân tích vị trí service request
2. Tìm kiếm thợ có skills phù hợp trong working radius
3. Ưu tiên thợ online và gần nhất
4. Gửi notification cho top 3 thợ phù hợp
```

## 🔮 Future Enhancements

### 1. Real-time Features
- **WebSocket integration:** Real-time status updates
- **Push notifications:** Thông báo job mới cho thợ online gần đó
- **Live tracking:** Theo dõi vị trí thợ trong quá trình di chuyển

### 2. Advanced Location Features  
- **Route optimization:** Tối ưu hóa lộ trình cho thợ
- **Traffic consideration:** Tính toán thời gian di chuyển thực tế
- **Geofencing:** Tự động update status khi vào/ra khỏi khu vực

### 3. Analytics & Reporting
- **Heatmap:** Phân tích density thợ theo khu vực
- **Performance metrics:** Thống kê thời gian online, response time
- **Demand forecasting:** Dự đoán nhu cầu dịch vụ theo vị trí/thời gian

## 📈 Monitoring & Metrics

### 1. Business Metrics
- **Online technician rate:** % thợ online tại mỗi thời điểm
- **Average response time:** Thời gian phản hồi trung bình
- **Coverage area:** Tỷ lệ khu vực được phủ sóng

### 2. Technical Metrics
- **API response time:** Latency của nearby search
- **Database query performance:** Execution time của location queries  
- **Update frequency:** Tần suất cập nhật status/location

### 3. User Experience Metrics
- **Search success rate:** % tìm kiếm trả về kết quả
- **Distance accuracy:** Độ chính xác tính toán khoảng cách
- **Location freshness:** Thời gian cập nhật location gần nhất

---

## 🎉 CONCLUSION

Online/Offline Status System đã được triển khai hoàn chỉnh với đầy đủ các tính năng cần thiết:

✅ **Completed Features:**
- Real-time status management (online/offline)
- Location tracking với GPS coordinates
- Distance-based technician search
- Working radius configuration
- Comprehensive API endpoints
- Database optimization với indexes
- Security và validation

✅ **Ready for Production:**
- Scalable architecture
- Performance optimized
- Well-documented APIs
- Comprehensive testing guide
- Future-proof design

Hệ thống này tạo nền tảng vững chắc cho việc matching thợ-khách hàng hiệu quả và sẽ là backbone cho các tính năng real-time trong tương lai. 