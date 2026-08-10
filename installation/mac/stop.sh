#!/bin/bash
cd "$(dirname "$0")"

echo "========================================"
echo "  Telegram Quiz - Stopping Services"
echo "========================================"
echo

docker compose stop
if [ $? -ne 0 ]; then
    echo
    echo "ERROR: Failed to stop the services."
    read -p "Press Enter to exit..."
    exit 1
fi

echo
echo "All services stopped. Containers are kept and will restart quickly."
echo "Data is preserved (volumes not deleted)."
echo
echo "To completely stop and remove the containers, run: docker compose down"
echo "To completely reset all data, run: docker compose down -v"
echo
read -p "Press Enter to close..."
