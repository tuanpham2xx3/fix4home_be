#!/bin/bash

# Simple HTTP server to run the frontend
# Usage: ./start-server.sh [port]

PORT=${1:-3000}

echo "🚀 Starting Fix4Home Frontend Server..."
echo "📍 URL: http://localhost:$PORT/activate.html"
echo ""
echo "Test activation with:"
echo "  http://localhost:$PORT/activate.html?token=YOUR_TOKEN"
echo ""
echo "Press Ctrl+C to stop"
echo ""

# Try different methods to start server
if command -v python3 &> /dev/null; then
    python3 -m http.server $PORT
elif command -v python &> /dev/null; then
    python -m http.server $PORT
elif command -v php &> /dev/null; then
    php -S localhost:$PORT
else
    echo "❌ Error: No HTTP server found!"
    echo "Please install Python or PHP, or use:"
    echo "  npx http-server -p $PORT"
    exit 1
fi

