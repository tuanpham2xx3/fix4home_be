# ===============================================================
# Fix4Home Backend - Local Development (PowerShell)
# ===============================================================
# Script to run Fix4Home Backend locally on Windows using PowerShell
# Prerequisites: MySQL, Redis, and Email Service must be running in Docker
# ===============================================================

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Fix4Home Backend - Local Development" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Check if MySQL is running in Docker on port 3307
Write-Host "Checking MySQL connection (Docker)..." -ForegroundColor Yellow
try {
    $connection = New-Object System.Net.Sockets.TcpClient('localhost', 3307)
    $connection.Close()
    Write-Host "[OK] MySQL is accessible on localhost:3307 (Docker)" -ForegroundColor Green
} catch {
    Write-Host "[WARN] Cannot connect to MySQL on localhost:3307" -ForegroundColor Yellow
    Write-Host "       Please ensure MySQL container is running in Docker" -ForegroundColor Yellow
    Write-Host "       Start with: docker compose up -d mysql" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "       You can continue anyway, but the app may fail to start..." -ForegroundColor Yellow
    Write-Host ""
}

# Check if Redis is running in Docker on port 6379
Write-Host "Checking Redis connection (Docker)..." -ForegroundColor Yellow
try {
    $connection = New-Object System.Net.Sockets.TcpClient('localhost', 6379)
    $connection.Close()
    Write-Host "[OK] Redis is accessible on localhost:6379 (Docker)" -ForegroundColor Green
} catch {
    Write-Host "[WARN] Cannot connect to Redis on localhost:6379" -ForegroundColor Yellow
    Write-Host "       Please ensure Redis container is running in Docker" -ForegroundColor Yellow
    Write-Host "       Start with: docker compose up -d redis" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "       You can continue anyway, but caching features may not work..." -ForegroundColor Yellow
    Write-Host ""
}

# Check if email service is running (optional)
Write-Host "Checking Email Service (Docker)..." -ForegroundColor Yellow
try {
    $connection = New-Object System.Net.Sockets.TcpClient('localhost', 8200)
    $connection.Close()
    Write-Host "[OK] Email Service is accessible on localhost:8200 (Docker)" -ForegroundColor Green
} catch {
    Write-Host "[WARN] Email Service is not running (optional)" -ForegroundColor Yellow
    Write-Host "       Start with: docker compose up -d email-service" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Starting Spring Boot application with 'local' profile..." -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Use Maven wrapper if available, otherwise use system Maven
if (Test-Path "mvnw.cmd") {
    Write-Host "Using Maven wrapper (mvnw.cmd)..." -ForegroundColor Green
    & .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
} elseif (Get-Command mvn -ErrorAction SilentlyContinue) {
    Write-Host "Using system Maven..." -ForegroundColor Green
    & mvn spring-boot:run "-Dspring-boot.run.profiles=local"
} else {
    Write-Host "[ERROR] Maven not found!" -ForegroundColor Red
    Write-Host "Please install Maven or use the Maven wrapper (mvnw.cmd)" -ForegroundColor Red
    exit 1
}





