@echo off
REM Simple HTTP server to run the frontend (Windows)
REM Usage: start-server.bat [port]

set PORT=%1
if "%PORT%"=="" set PORT=3000

echo Starting Fix4Home Frontend Server...
echo URL: http://localhost:%PORT%/activate.html
echo.
echo Test activation with:
echo   http://localhost:%PORT%/activate.html?token=YOUR_TOKEN
echo.
echo Press Ctrl+C to stop
echo.

REM Try Python first
python -m http.server %PORT% 2>nul
if %errorlevel% neq 0 (
    python3 -m http.server %PORT% 2>nul
    if %errorlevel% neq 0 (
        echo Error: Python not found!
        echo Please install Python or use: npx http-server -p %PORT%
        pause
    )
)

