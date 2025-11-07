#!/bin/bash

# Fix4Home API Client SDK Generator
# Generates client SDKs from OpenAPI specification

echo "🏠 Fix4Home API - Client SDK Generator"
echo "========================================"
echo ""

# Check if openapi-generator-cli is installed
if ! command -v openapi-generator-cli &> /dev/null; then
    echo "❌ openapi-generator-cli not found!"
    echo ""
    echo "Installing openapi-generator-cli..."
    npm install -g @openapitools/openapi-generator-cli
    
    if [ $? -ne 0 ]; then
        echo ""
        echo "❌ Failed to install openapi-generator-cli"
        echo "Please install manually: npm install -g @openapitools/openapi-generator-cli"
        exit 1
    fi
fi

# Check if openapi.yaml exists
if [ ! -f "openapi.yaml" ]; then
    echo "❌ Error: openapi.yaml not found!"
    echo "Please run this script from the docs/openapi directory"
    exit 1
fi

# Create clients directory
mkdir -p clients

echo "📦 Available client generators:"
echo "  1. TypeScript/Axios (React, Vue, Angular)"
echo "  2. JavaScript"
echo "  3. Java"
echo "  4. Python"
echo "  5. Go"
echo "  6. PHP"
echo "  7. Swift (iOS)"
echo "  8. Kotlin (Android)"
echo "  9. C#"
echo "  10. All of the above"
echo "  0. Custom generator"
echo ""

read -p "Select option [1-10, 0]: " choice

case $choice in
    1)
        echo "Generating TypeScript/Axios client..."
        openapi-generator-cli generate \
            -i openapi.yaml \
            -g typescript-axios \
            -o clients/typescript \
            --additional-properties=npmName=fix4home-api-client,npmVersion=1.0.0
        echo "✅ TypeScript client generated in clients/typescript/"
        ;;
    2)
        echo "Generating JavaScript client..."
        openapi-generator-cli generate \
            -i openapi.yaml \
            -g javascript \
            -o clients/javascript \
            --additional-properties=projectName=fix4home-api-client
        echo "✅ JavaScript client generated in clients/javascript/"
        ;;
    3)
        echo "Generating Java client..."
        openapi-generator-cli generate \
            -i openapi.yaml \
            -g java \
            -o clients/java \
            --additional-properties=groupId=com.fix4home,artifactId=fix4home-api-client,artifactVersion=1.0.0
        echo "✅ Java client generated in clients/java/"
        ;;
    4)
        echo "Generating Python client..."
        openapi-generator-cli generate \
            -i openapi.yaml \
            -g python \
            -o clients/python \
            --additional-properties=packageName=fix4home_api_client,projectName=fix4home-api-client
        echo "✅ Python client generated in clients/python/"
        ;;
    5)
        echo "Generating Go client..."
        openapi-generator-cli generate \
            -i openapi.yaml \
            -g go \
            -o clients/go \
            --additional-properties=packageName=fix4home
        echo "✅ Go client generated in clients/go/"
        ;;
    6)
        echo "Generating PHP client..."
        openapi-generator-cli generate \
            -i openapi.yaml \
            -g php \
            -o clients/php \
            --additional-properties=packageName=Fix4HomeAPI
        echo "✅ PHP client generated in clients/php/"
        ;;
    7)
        echo "Generating Swift client..."
        openapi-generator-cli generate \
            -i openapi.yaml \
            -g swift5 \
            -o clients/swift \
            --additional-properties=projectName=Fix4HomeAPI
        echo "✅ Swift client generated in clients/swift/"
        ;;
    8)
        echo "Generating Kotlin client..."
        openapi-generator-cli generate \
            -i openapi.yaml \
            -g kotlin \
            -o clients/kotlin \
            --additional-properties=packageName=com.fix4home.api
        echo "✅ Kotlin client generated in clients/kotlin/"
        ;;
    9)
        echo "Generating C# client..."
        openapi-generator-cli generate \
            -i openapi.yaml \
            -g csharp-netcore \
            -o clients/csharp \
            --additional-properties=packageName=Fix4Home.ApiClient
        echo "✅ C# client generated in clients/csharp/"
        ;;
    10)
        echo "Generating all clients..."
        echo ""
        
        generators=("typescript-axios:typescript" "javascript:javascript" "java:java" "python:python" "go:go" "php:php" "swift5:swift" "kotlin:kotlin" "csharp-netcore:csharp")
        
        for gen in "${generators[@]}"; do
            IFS=':' read -r generator dir <<< "$gen"
            echo "Generating $dir client..."
            openapi-generator-cli generate \
                -i openapi.yaml \
                -g "$generator" \
                -o "clients/$dir" \
                2>/dev/null
            echo "✅ $dir client generated"
        done
        
        echo ""
        echo "✅ All clients generated successfully!"
        ;;
    0)
        echo ""
        echo "Available generators:"
        openapi-generator-cli list
        echo ""
        read -p "Enter generator name: " custom_gen
        read -p "Enter output directory name: " custom_dir
        
        echo "Generating $custom_gen client..."
        openapi-generator-cli generate \
            -i openapi.yaml \
            -g "$custom_gen" \
            -o "clients/$custom_dir"
        echo "✅ Client generated in clients/$custom_dir/"
        ;;
    *)
        echo "❌ Invalid option"
        exit 1
        ;;
esac

echo ""
echo "========================================"
echo "✅ Client generation complete!"
echo ""
echo "Generated clients are in: ./clients/"
echo ""
echo "Usage instructions:"
echo "  • TypeScript: See clients/typescript/README.md"
echo "  • JavaScript: See clients/javascript/README.md"
echo "  • Java: See clients/java/README.md"
echo "  • Python: See clients/python/README.md"
echo ""
echo "Happy coding! 🚀"
echo "========================================"

