@echo off
echo ========================================
echo   Telegram Quiz - Stopping Services
echo ========================================
echo.

docker compose down

echo.
echo All services stopped.
echo Data is preserved (volumes not deleted).
echo.
pause
