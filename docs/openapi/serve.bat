@echo off
REM Fix4Home API Documentation Server (Windows)
REM Serves the OpenAPI documentation with a simple HTTP server

echo.
echo ========================================
echo Fix4Home API Documentation Server
echo ========================================
echo.

REM Check if we're in the correct directory
if not exist "openapi.yaml" (
    echo ERROR: openapi.yaml not found!
    echo Please run this script from the docs\openapi directory
    pause
    exit /b 1
)

set PORT=8000

echo Starting documentation server...
echo.
echo Available viewers:
echo   * Main Hub:    http://localhost:%PORT%/index.html
echo   * Swagger UI:  http://localhost:%PORT%/swagger-ui.html
echo   * ReDoc:       http://localhost:%PORT%/redoc.html
echo   * RapiDoc:     http://localhost:%PORT%/rapidoc.html
echo.
echo Press Ctrl+C to stop the server
echo ========================================
echo.

REM Try Python 3
python --version >nul 2>&1
if %errorlevel% equ 0 (
    echo Using Python...
    python -m http.server %PORT%
    goto :end
)

REM Try PHP
php --version >nul 2>&1
if %errorlevel% equ 0 (
    echo Using PHP...
    php -S localhost:%PORT%
    goto :end
)

REM Try Node.js
npx --version >nul 2>&1
if %errorlevel% equ 0 (
    echo Using Node.js http-server...
    npx http-server -p %PORT%
    goto :end
)

echo ERROR: No suitable HTTP server found!
echo.
echo Please install one of the following:
echo   * Python 3: https://www.python.org/
echo   * PHP: https://www.php.net/
echo   * Node.js: https://nodejs.org/
echo.
echo Or manually open index.html in your browser
echo.
pause
exit /b 1

:end

