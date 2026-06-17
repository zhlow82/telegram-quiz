@echo off
echo ========================================
echo   Telegram Quiz - Creating Distribution Package
echo ========================================
echo.

set ZIP_NAME=telegram-quiz.zip

echo Removing old zip file...
if exist "%ZIP_NAME%" del /f /q "%ZIP_NAME%"

echo Creating zip package...
powershell -Command "Compress-Archive -Path 'src', 'docker-compose.yml', 'start.bat', 'stop.bat', 'README.md' -DestinationPath '%ZIP_NAME%' -Force"

echo.
echo ========================================
echo   Done! Package created: %ZIP_NAME%
echo ========================================
echo.
echo Next, you need to:
echo   1. Install Docker Desktop
echo   2. Extract this zip
echo   3. Double-click start.bat
echo   4. Open http://localhost
echo.
pause
