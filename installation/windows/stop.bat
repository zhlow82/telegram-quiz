@echo off
cd /d "%~dp0"

echo ========================================
echo   Telegram Quiz - Stopping Services
echo ========================================
echo.

docker compose stop
if %errorlevel% neq 0 (
    echo.
    echo ERROR: Failed to stop the services.
    pause
    exit /b 1
)

echo.
echo All services stopped. Containers are kept and will restart quickly.
echo Data is preserved (volumes not deleted).
echo.
echo To completely stop and remove the containers, run: docker compose down
echo To completely reset all data, run: docker compose down -v
echo.
pause
