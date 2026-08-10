#!/bin/bash
cd "$(dirname "$0")"

echo "========================================"
echo "  Telegram Quiz - Starting Services"
echo "========================================"
echo

if ! command -v docker >/dev/null 2>&1; then
    echo "ERROR: Docker is not installed."
    echo "Please install Docker Desktop from:"
    echo "https://www.docker.com/products/docker-desktop"
    read -p "Press Enter to exit..."
    exit 1
fi

echo "Checking if Docker is running..."
if ! docker info >/dev/null 2>&1; then
    echo "ERROR: Docker is not running."
    echo "Please open Docker Desktop and wait until the whale icon appears"
    echo "in the menu bar, then run this script again."
    read -p "Press Enter to exit..."
    exit 1
fi

echo "Checking if services are already running..."
if [ -n "$(docker compose ps --status running -q 2>/dev/null)" ]; then
    echo
    docker compose ps --format "table {{.Name}}\t{{.Status}}"
    echo
    read -p "Services are already running. Restart? [y/N] " -r answer
    if [[ ! "$answer" =~ ^[Yy]$ ]]; then
        echo
        echo "========================================"
        echo "  Telegram Quiz - Already Running"
        echo "========================================"
        echo
        echo "Access the application at:"
        echo "http://localhost/tg-quiz"
        echo
        echo "To stop all services, run: ./stop.sh"
        echo
        read -p "Press Enter to close..."
        exit 0
    fi
    echo
    echo "Stopping existing services..."
    docker compose down
fi

echo
echo "Building and starting services (first run takes a few minutes)..."
docker compose up -d --build
if [ $? -ne 0 ]; then
    echo
    echo "ERROR: Failed to start the services."
    echo "Check Docker Desktop and try again."
    read -p "Press Enter to exit..."
    exit 1
fi

echo
echo "Waiting for the application to become ready..."
attempts=0
status=""
while [ "$attempts" -lt 45 ]; do
    attempts=$((attempts + 1))
    status=$(curl -s -o /dev/null -w "%{http_code}" --max-time 3 "http://localhost/tg-quiz/" 2>/dev/null || true)
    if [ "$status" = "200" ]; then
        break
    fi
    sleep 2
done

if [ "$status" != "200" ]; then
    echo
    echo "WARNING: The application did not respond within 90 seconds."
    echo "Check Docker Desktop to see if the containers are running."
fi

echo
echo "========================================"
echo "  Telegram Quiz - Ready!"
echo "========================================"
echo
echo "Access the application at:"
echo "http://localhost/tg-quiz"
echo
echo "Default admin login: localadmin / password88"
echo
echo "To stop all services, run: ./stop.sh"
echo
docker compose ps --format "table {{.Name}}\t{{.Status}}"
echo
read -p "Press Enter to close..."
