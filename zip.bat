@echo off
cd /d "%~dp0"

echo ========================================
echo   Telegram Quiz - Creating Distribution Package
echo ========================================
echo.

set ZIP_NAME=telegram-quiz.zip

echo Removing old zip file...
if exist "%ZIP_NAME%" del /f /q "%ZIP_NAME%"

echo Creating zip package (excluding build artifacts)...
tar -a -c -f "%ZIP_NAME%" ^
    --exclude "src/frontend/node_modules" ^
    --exclude "*/target" ^
    --exclude "src/frontend/dist" ^
    --exclude ".git" ^
    --exclude ".env" ^
    src docker-compose.yml start.bat stop.bat zip.bat SETUP-GUIDE.txt README.md

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Failed to create the package.
    pause
    exit /b 1
)

echo.
echo ========================================
echo   Done! Package created: %ZIP_NAME%
echo ========================================
echo.
echo Next, you need to:
echo   1. Install Docker Desktop
echo   2. Extract this zip
echo   3. Double-click start.bat
echo   4. Open http://localhost/tg-quiz
echo.
pause
