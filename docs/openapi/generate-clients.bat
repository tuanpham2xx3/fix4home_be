@echo off
REM Fix4Home API Client SDK Generator (Windows)
REM Generates client SDKs from OpenAPI specification

echo.
echo ========================================
echo Fix4Home API - Client SDK Generator
echo ========================================
echo.

REM Check if openapi.yaml exists
if not exist "openapi.yaml" (
    echo ERROR: openapi.yaml not found!
    echo Please run this script from the docs\openapi directory
    pause
    exit /b 1
)

REM Check if openapi-generator-cli is installed
where openapi-generator-cli >nul 2>&1
if %errorlevel% neq 0 (
    echo openapi-generator-cli not found!
    echo.
    echo Installing openapi-generator-cli...
    call npm install -g @openapitools/openapi-generator-cli
    
    if %errorlevel% neq 0 (
        echo.
        echo ERROR: Failed to install openapi-generator-cli
        echo Please install manually: npm install -g @openapitools/openapi-generator-cli
        pause
        exit /b 1
    )
)

REM Create clients directory
if not exist "clients" mkdir clients

echo Available client generators:
echo   1. TypeScript/Axios (React, Vue, Angular)
echo   2. JavaScript
echo   3. Java
echo   4. Python
echo   5. Go
echo   6. PHP
echo   7. Swift (iOS)
echo   8. Kotlin (Android)
echo   9. C#
echo   10. All of the above
echo.

set /p choice="Select option [1-10]: "

if "%choice%"=="1" (
    echo Generating TypeScript/Axios client...
    call openapi-generator-cli generate -i openapi.yaml -g typescript-axios -o clients\typescript --additional-properties=npmName=fix4home-api-client,npmVersion=1.0.0
    echo TypeScript client generated in clients\typescript\
) else if "%choice%"=="2" (
    echo Generating JavaScript client...
    call openapi-generator-cli generate -i openapi.yaml -g javascript -o clients\javascript --additional-properties=projectName=fix4home-api-client
    echo JavaScript client generated in clients\javascript\
) else if "%choice%"=="3" (
    echo Generating Java client...
    call openapi-generator-cli generate -i openapi.yaml -g java -o clients\java --additional-properties=groupId=com.fix4home,artifactId=fix4home-api-client,artifactVersion=1.0.0
    echo Java client generated in clients\java\
) else if "%choice%"=="4" (
    echo Generating Python client...
    call openapi-generator-cli generate -i openapi.yaml -g python -o clients\python --additional-properties=packageName=fix4home_api_client,projectName=fix4home-api-client
    echo Python client generated in clients\python\
) else if "%choice%"=="5" (
    echo Generating Go client...
    call openapi-generator-cli generate -i openapi.yaml -g go -o clients\go --additional-properties=packageName=fix4home
    echo Go client generated in clients\go\
) else if "%choice%"=="6" (
    echo Generating PHP client...
    call openapi-generator-cli generate -i openapi.yaml -g php -o clients\php --additional-properties=packageName=Fix4HomeAPI
    echo PHP client generated in clients\php\
) else if "%choice%"=="7" (
    echo Generating Swift client...
    call openapi-generator-cli generate -i openapi.yaml -g swift5 -o clients\swift --additional-properties=projectName=Fix4HomeAPI
    echo Swift client generated in clients\swift\
) else if "%choice%"=="8" (
    echo Generating Kotlin client...
    call openapi-generator-cli generate -i openapi.yaml -g kotlin -o clients\kotlin --additional-properties=packageName=com.fix4home.api
    echo Kotlin client generated in clients\kotlin\
) else if "%choice%"=="9" (
    echo Generating C# client...
    call openapi-generator-cli generate -i openapi.yaml -g csharp-netcore -o clients\csharp --additional-properties=packageName=Fix4Home.ApiClient
    echo C# client generated in clients\csharp\
) else if "%choice%"=="10" (
    echo Generating all clients...
    echo.
    
    echo Generating TypeScript client...
    call openapi-generator-cli generate -i openapi.yaml -g typescript-axios -o clients\typescript 2>nul
    
    echo Generating JavaScript client...
    call openapi-generator-cli generate -i openapi.yaml -g javascript -o clients\javascript 2>nul
    
    echo Generating Java client...
    call openapi-generator-cli generate -i openapi.yaml -g java -o clients\java 2>nul
    
    echo Generating Python client...
    call openapi-generator-cli generate -i openapi.yaml -g python -o clients\python 2>nul
    
    echo Generating Go client...
    call openapi-generator-cli generate -i openapi.yaml -g go -o clients\go 2>nul
    
    echo Generating PHP client...
    call openapi-generator-cli generate -i openapi.yaml -g php -o clients\php 2>nul
    
    echo Generating Swift client...
    call openapi-generator-cli generate -i openapi.yaml -g swift5 -o clients\swift 2>nul
    
    echo Generating Kotlin client...
    call openapi-generator-cli generate -i openapi.yaml -g kotlin -o clients\kotlin 2>nul
    
    echo Generating C# client...
    call openapi-generator-cli generate -i openapi.yaml -g csharp-netcore -o clients\csharp 2>nul
    
    echo.
    echo All clients generated successfully!
) else (
    echo Invalid option
    pause
    exit /b 1
)

echo.
echo ========================================
echo Client generation complete!
echo.
echo Generated clients are in: .\clients\
echo.
echo Usage instructions:
echo   * TypeScript: See clients\typescript\README.md
echo   * JavaScript: See clients\javascript\README.md
echo   * Java: See clients\java\README.md
echo   * Python: See clients\python\README.md
echo.
echo Happy coding!
echo ========================================
echo.
pause

