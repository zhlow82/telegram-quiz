@echo off
cd /d "%~dp0"

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

echo Checking if Docker is running...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker is not running.
    echo Please open Docker Desktop and wait until the whale icon appears
    echo in the system tray, then run this script again.
    pause
    exit /b 1
)

echo Checking if services are already running...
docker compose ps --status running -q 2>nul | findstr . >nul
if %errorlevel% equ 0 (
    echo.
    docker compose ps --format "table {{.Name}}\t{{.Status}}" 2>nul
    echo.
    choice /M "Services are already running. Restart?"
    if errorlevel 2 goto :skip_start
    echo.
    echo Stopping existing services...
    docker compose down
)

echo.
echo Building and starting services (first run takes a few minutes)...
docker compose up -d --build
if %errorlevel% neq 0 (
    echo.
    echo ERROR: Failed to start the services.
    echo Check Docker Desktop and try again.
    pause
    exit /b 1
)

echo.
echo Waiting for the application to become ready...
set /a attempts=0
:wait_loop
set /a attempts+=1
if %attempts% gtr 45 goto :wait_timeout
powershell -Command "try { $r = Invoke-WebRequest -Uri 'http://localhost/tg-quiz' -UseBasicParsing -TimeoutSec 3; if ($r.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }" >nul 2>&1
if %errorlevel% equ 0 goto :ready
timeout /t 2 /nobreak >nul
goto :wait_loop

:wait_timeout
echo.
echo WARNING: The application did not respond within 90 seconds.
echo Check Docker Desktop to see if the containers are running.

:ready
echo.
echo ========================================
echo   Telegram Quiz - Ready!
echo ========================================
echo.
echo Access the application at:
echo http://localhost/tg-quiz
echo.
echo Default admin login: localadmin / password88
echo.
echo To stop all services, run: stop.bat
echo.
docker compose ps --format "table {{.Name}}\t{{.Status}}" 2>nul
echo.
pause

:skip_start
echo.
echo ========================================
echo   Telegram Quiz - Already Running
echo ========================================
echo.
echo Access the application at:
echo http://localhost/tg-quiz
echo.
echo To stop all services, run: stop.bat
echo.
pause
