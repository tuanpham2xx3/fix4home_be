#!/bin/bash

# Script to build Fix4Home Backend for local development

echo "=========================================="
echo "Fix4Home Backend - Build for Local"
echo "=========================================="
echo ""

# Clean and build the project
echo "Cleaning previous build..."
if [ -f "./mvnw" ]; then
    ./mvnw clean
else
    mvn clean
fi

echo ""
echo "Building project..."
if [ -f "./mvnw" ]; then
    ./mvnw package -DskipTests
else
    mvn package -DskipTests
fi

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ Build failed!"
    exit 1
fi

echo ""
echo "✅ Build completed!"
echo ""
echo "JAR file location: target/fix4home-0.0.1-SNAPSHOT.jar"
echo ""
echo "To run the application:"
echo "  java -jar target/fix4home-0.0.1-SNAPSHOT.jar --spring.profiles.active=local"
echo ""
echo "Or use: ./run-local.sh"
echo ""





