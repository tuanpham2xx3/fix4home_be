@echo off
REM Script to build Fix4Home Backend for local development

echo ==========================================
echo Fix4Home Backend - Build for Local
echo ==========================================
echo.

REM Clean and build the project
echo Cleaning previous build...
if exist "mvnw.cmd" (
    call mvnw.cmd clean
) else (
    mvn clean
)

echo.
echo Building project...
if exist "mvnw.cmd" (
    call mvnw.cmd package -DskipTests
) else (
    mvn package -DskipTests
)

if errorlevel 1 (
    echo.
    echo [ERROR] Build failed!
    exit /b 1
)

echo.
echo [SUCCESS] Build completed!
echo.
echo JAR file location: target\fix4home-0.0.1-SNAPSHOT.jar
echo.
echo To run the application:
echo   java -jar target\fix4home-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
echo.
echo Or use: run-local.bat
echo.


