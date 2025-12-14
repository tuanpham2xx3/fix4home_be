# Local Development Guide

Hướng dẫn chạy Fix4Home Backend ở local (ngoài Docker) để tiện debug, trong khi MySQL, Redis và Email Service vẫn chạy trên Docker.

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

Bạn sẽ thấy các containers:
- `fix4home-mysql` - MySQL trên port 3307
- `fix4home-redis` - Redis trên port 6379
- `fix4home-email-service` - Email Service trên port 8200

### 2. Chạy Backend Application

#### Cách 1: Sử dụng script (Khuyến nghị)

**Linux/Mac:**
```bash
chmod +x run-local.sh
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
- MySQL: `localhost:3307` (port được map từ Docker container)
- Username: `fix4home`
- Password: `pass123`
- Database: `fix4home_db`

### Kết nối Redis
- Host: `localhost`
- Port: `6379` (port được map từ Docker container)
- No password

### Email Service
- URL: `http://localhost:8200` (port được map từ Docker container)
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
- Tìm process đang sử dụng port: `netstat -ano | findstr 8100` (Windows) hoặc `lsof -i :8100` (Linux/Mac)
- Dừng process đó hoặc đổi port trong `application-local.properties`: `server.port=8101`

### Flyway migration errors
Nếu gặp lỗi migration:
- Kiểm tra database đã được tạo chưa
- Kiểm tra user có quyền tạo bảng không
- Có thể tắt Flyway tạm thời trong `application-local.properties`: `spring.flyway.enabled=false`
- Hoặc chạy migrations thủ công từ thư mục `src/main/resources/db/migration`


## Lợi ích chạy local

1. ✅ **Debug nhanh hơn** - Không cần rebuild Docker image
2. ✅ **Hot reload** - Code thay đổi được reload ngay với Spring Boot DevTools
3. ✅ **Breakpoints** - Debug dễ dàng với IDE
4. ✅ **Logs rõ ràng** - Xem logs trực tiếp trong console
5. ✅ **Performance tốt hơn** - Không bị overhead của Docker cho ứng dụng
6. ✅ **Dễ quản lý database** - Truy cập MySQL qua port 3307 với các tool như MySQL Workbench, phpMyAdmin

## Lưu ý

- Đảm bảo MySQL, Redis và Email Service luôn chạy trong Docker trước khi start ứng dụng
- File uploads sẽ được lưu trong thư mục `uploads/` (đã được thêm vào `.gitignore`)
- Logs sẽ hiển thị trực tiếp trong console với level DEBUG
- Flyway sẽ tự động chạy migrations khi ứng dụng khởi động

