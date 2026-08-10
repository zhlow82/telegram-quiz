#!/bin/bash
cd "$(dirname "$0")/../.."

echo "========================================"
echo "  Telegram Quiz - Creating Distribution Package"
echo "========================================"
echo

ZIP_NAME="telegram-quiz.zip"

echo "Removing old zip file..."
rm -f "$ZIP_NAME"

echo "Creating zip package (excluding build artifacts)..."
tar -a -c -f "$ZIP_NAME" \
    --exclude "src/frontend/node_modules" \
    --exclude "*/target" \
    --exclude "src/frontend/dist" \
    --exclude ".git" \
    --exclude ".env" \
    -C installation/mac start.sh stop.sh SETUP-GUIDE.txt \
    -C ../.. src docker-compose.yml USAGE-GUIDE.txt

if [ $? -ne 0 ]; then
    echo
    echo "ERROR: Failed to create the package."
    read -p "Press Enter to exit..."
    exit 1
fi

echo
echo "========================================"
echo "  Done! Package created: $ZIP_NAME"
echo "========================================"
echo
echo "Next, you need to:"
echo "  1. Install Docker Desktop"
echo "  2. Extract this zip"
echo "  3. Open Terminal in the extracted folder and run ./start.sh"
echo "  4. Open http://localhost/tg-quiz"
echo
read -p "Press Enter to close..."
