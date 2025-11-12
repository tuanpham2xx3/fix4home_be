# Local Development Guide

Hướng dẫn chạy Fix4Home Backend ở local (ngoài Docker) để tiện debug.

## Yêu cầu

1. **Java 21** - Cài đặt JDK 21
2. **Maven 3.6+** - Build tool
3. **Docker & Docker Compose** - Để chạy MySQL, Redis, và Email Service

## Các bước chạy

### 1. Khởi động các services cần thiết trong Docker

Chỉ cần chạy MySQL, Redis, và Email Service (không cần chạy app container):

```bash
# Chạy tất cả services trừ app
docker compose up -d mysql redis email-service

# Hoặc chạy từng service
docker compose up -d mysql
docker compose up -d redis
docker compose up -d email-service
```

Kiểm tra services đang chạy:
```bash
docker compose ps
```

### 2. Chạy Backend Application

#### Cách 1: Sử dụng script (Khuyến nghị)

**Linux/Mac:**
```bash
./run-local.sh
```

**Windows:**
```cmd
run-local.bat
```

#### Cách 2: Chạy trực tiếp với Maven

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

#### Cách 3: Chạy từ IDE (IntelliJ IDEA, VS Code, Eclipse)

1. Set active profile: `local`
2. Run main class: `com.fix4home.fix4home.Fix4homeApplication`
3. Hoặc tạo Run Configuration với VM options: `-Dspring.profiles.active=local`

### 3. Kiểm tra ứng dụng

- Backend API: http://localhost:8100
- Health Check: http://localhost:8100/actuator/health
- Swagger UI: http://localhost:8100/swagger-ui.html

## Cấu hình

File cấu hình cho local development: `src/main/resources/application-local.properties`

### Kết nối Database
- MySQL: `localhost:3307` (port được map từ Docker)
- Username: `fix4home`
- Password: `pass123`
- Database: `fix4home_db`

### Kết nối Redis
- Host: `localhost`
- Port: `6379`
- No password

### Email Service
- URL: `http://localhost:8200`
- API Key: `fix4home_prod_123abc456def789`

## Debugging

### Hot Reload với Spring Boot DevTools

Nếu có Spring Boot DevTools trong dependencies, code sẽ tự động reload khi thay đổi.

### Debug Mode trong IDE

1. **IntelliJ IDEA:**
   - Tạo Run Configuration
   - Set "Active profiles" = `local`
   - Click "Debug" thay vì "Run"
   - Set breakpoints và debug như bình thường

2. **VS Code:**
   - Cài extension "Spring Boot Extension Pack"
   - Tạo `.vscode/launch.json`:
   ```json
   {
     "type": "java",
     "name": "Spring Boot - Local",
     "request": "launch",
     "mainClass": "com.fix4home.fix4home.Fix4homeApplication",
     "projectName": "fix4home",
     "args": "--spring.profiles.active=local"
   }
   ```

## Troubleshooting

### Lỗi kết nối MySQL
```
Communications link failure
```
**Giải pháp:** Đảm bảo MySQL container đang chạy:
```bash
docker compose ps mysql
docker compose up -d mysql
```

### Lỗi kết nối Redis
```
Unable to connect to Redis
```
**Giải pháp:** Đảm bảo Redis container đang chạy:
```bash
docker compose ps redis
docker compose up -d redis
```

### Port 8100 đã được sử dụng
```
Port 8100 is already in use
```
**Giải pháp:** 
- Dừng app container: `docker compose stop app`
- Hoặc đổi port trong `application-local.properties`: `server.port=8101`

### Flyway migration errors
Nếu gặp lỗi migration, có thể tắt Flyway trong local profile:
```properties
spring.flyway.enabled=false
```

## Lợi ích chạy local

1. ✅ **Debug nhanh hơn** - Không cần rebuild Docker image
2. ✅ **Hot reload** - Code thay đổi được reload ngay
3. ✅ **Breakpoints** - Debug dễ dàng với IDE
4. ✅ **Logs rõ ràng** - Xem logs trực tiếp trong console
5. ✅ **Performance** - Không bị overhead của Docker

## Lưu ý

- Đảm bảo MySQL và Redis luôn chạy trong Docker
- File uploads sẽ được lưu trong thư mục `uploads/` (đã được thêm vào `.gitignore`)
- Logs sẽ hiển thị trực tiếp trong console với level DEBUG

