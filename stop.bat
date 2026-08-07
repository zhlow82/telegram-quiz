@echo off
cd /d "%~dp0"

echo ========================================
echo   Telegram Quiz - Stopping Services
echo ========================================
echo.

docker compose down
if %errorlevel% neq 0 (
    echo.
    echo ERROR: Failed to stop the services.
    pause
    exit /b 1
)

echo.
echo All services stopped.
echo Data is preserved (volumes not deleted).
echo.
echo To completely reset all data, run: docker compose down -v
echo.
pause
