@echo off
echo ========================================
echo   Telegram Quiz - Starting Services
echo ========================================
echo.

docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker is not installed.
    echo Please install Docker Desktop from:
    echo https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

echo Checking if services are already running...
docker compose ps --format json 2>nul | findstr "running" >nul
if %errorlevel% equ 0 (
    echo.
    docker compose ps --format "table {{.Name}}\t{{.Status}}" 2>nul
    echo.
    choice /M "Services are already running. Restart?"
    if errorlevel 2 goto :skip_restart
    echo.
    echo Stopping existing services...
    docker compose down
)

echo.
echo Building and starting services...
docker compose up -d --build

echo.
echo ========================================
echo   Services started successfully!
echo ========================================
echo.
echo Frontend:  http://localhost
echo Gateway:   http://localhost:8080
echo.
echo To stop all services, run: stop.bat
echo.
docker compose ps --format "table {{.Name}}\t{{.Status}}"
echo.
pause

:skip_restart
echo.
echo Services are already running.
echo.
docker compose ps --format "table {{.Name}}\t{{.Status}}"
echo.
pause
