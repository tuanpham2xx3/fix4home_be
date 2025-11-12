@echo off
REM Script to run Fix4Home Backend locally on Windows
REM Prerequisites: MySQL and Redis must be running in Docker

echo ==========================================
echo Fix4Home Backend - Local Development
echo ==========================================
echo.

REM Check if MySQL is running
echo Checking MySQL connection...
docker compose ps mysql | findstr /C:"Up" >nul
if errorlevel 1 (
    echo [ERROR] MySQL container is not running!
    echo Please start MySQL with: docker compose up -d mysql
    exit /b 1
)
echo [OK] MySQL is running on localhost:3307

REM Check if Redis is running
echo Checking Redis connection...
docker compose ps redis | findstr /C:"Up" >nul
if errorlevel 1 (
    echo [ERROR] Redis container is not running!
    echo Please start Redis with: docker compose up -d redis
    exit /b 1
)
echo [OK] Redis is running on localhost:6379

REM Check if email service is running (optional)
echo Checking Email Service...
docker compose ps email-service | findstr /C:"Up" >nul
if errorlevel 1 (
    echo [WARN] Email Service is not running (optional)
    echo        Start with: docker compose up -d email-service
) else (
    echo [OK] Email Service is running on localhost:8200
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

