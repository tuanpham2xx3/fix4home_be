#!/bin/bash

# Fix4Home API Documentation Server
# Serves the OpenAPI documentation with a simple HTTP server

echo "🏠 Fix4Home API Documentation Server"
echo "===================================="
echo ""

# Check if we're in the correct directory
if [ ! -f "openapi.yaml" ]; then
    echo "❌ Error: openapi.yaml not found!"
    echo "Please run this script from the docs/openapi directory"
    exit 1
fi

# Determine which HTTP server to use
PORT=8000

echo "📚 Starting documentation server..."
echo ""
echo "Available viewers:"
echo "  • Main Hub:    http://localhost:$PORT/index.html"
echo "  • Swagger UI:  http://localhost:$PORT/swagger-ui.html"
echo "  • ReDoc:       http://localhost:$PORT/redoc.html"
echo "  • RapiDoc:     http://localhost:$PORT/rapidoc.html"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""
echo "===================================="

# Try Python 3
if command -v python3 &> /dev/null; then
    echo "Using Python 3..."
    python3 -m http.server $PORT
# Try Python 2
elif command -v python &> /dev/null; then
    echo "Using Python 2..."
    python -m SimpleHTTPServer $PORT
# Try PHP
elif command -v php &> /dev/null; then
    echo "Using PHP..."
    php -S localhost:$PORT
# Try Node.js http-server
elif command -v npx &> /dev/null; then
    echo "Using Node.js http-server..."
    npx http-server -p $PORT
else
    echo "❌ Error: No suitable HTTP server found!"
    echo ""
    echo "Please install one of the following:"
    echo "  • Python 3: https://www.python.org/"
    echo "  • PHP: https://www.php.net/"
    echo "  • Node.js: https://nodejs.org/"
    echo ""
    echo "Or manually open index.html in your browser"
    exit 1
fi

