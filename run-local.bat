@echo off
REM Script to run Fix4Home Backend locally on Windows
REM Prerequisites: MySQL, Redis, and Email Service must be running in Docker

echo ==========================================
echo Fix4Home Backend - Local Development
echo ==========================================
echo.

REM Check if MySQL is running in Docker on port 3307
echo Checking MySQL connection (Docker)...
powershell -Command "try { $connection = New-Object System.Net.Sockets.TcpClient('localhost', 3307); $connection.Close(); exit 0 } catch { exit 1 }" >nul 2>&1
if errorlevel 1 (
    echo [WARN] Cannot connect to MySQL on localhost:3307
    echo        Please ensure MySQL container is running in Docker
    echo        Start with: docker compose up -d mysql
    echo.
    echo        You can continue anyway, but the app may fail to start...
    echo.
) else (
    echo [OK] MySQL is accessible on localhost:3307 (Docker)
)

REM Check if Redis is running in Docker on port 6379
echo Checking Redis connection (Docker)...
powershell -Command "try { $connection = New-Object System.Net.Sockets.TcpClient('localhost', 6379); $connection.Close(); exit 0 } catch { exit 1 }" >nul 2>&1
if errorlevel 1 (
    echo [WARN] Cannot connect to Redis on localhost:6379
    echo        Please ensure Redis container is running in Docker
    echo        Start with: docker compose up -d redis
    echo.
    echo        You can continue anyway, but caching features may not work...
    echo.
) else (
    echo [OK] Redis is accessible on localhost:6379 (Docker)
)

REM Check if email service is running (optional)
echo Checking Email Service (Docker)...
powershell -Command "try { $connection = New-Object System.Net.Sockets.TcpClient('localhost', 8200); $connection.Close(); exit 0 } catch { exit 1 }" >nul 2>&1
if errorlevel 1 (
    echo [WARN] Email Service is not running (optional)
    echo        Start with: docker compose up -d email-service
) else (
    echo [OK] Email Service is accessible on localhost:8200 (Docker)
)

echo.
echo Starting Spring Boot application with 'local' profile...
echo ==========================================
echo.

REM Use Maven wrapper if available, otherwise use system Maven
if exist "mvnw.cmd" (
    echo Using Maven wrapper (mvnw.cmd)...
    call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
) else (
    echo Using system Maven...
    mvn spring-boot:run -Dspring-boot.run.profiles=local
)

