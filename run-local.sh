#!/bin/bash

# Script to run Fix4Home Backend locally (outside Docker)
# Prerequisites: MySQL, Redis, and Email Service must be running in Docker

echo "=========================================="
echo "Fix4Home Backend - Local Development"
echo "=========================================="
echo ""

# Check if MySQL is running in Docker on port 3307
echo "Checking MySQL connection (Docker)..."
if command -v nc &> /dev/null; then
    if nc -z localhost 3307 2>/dev/null; then
        echo "✅ MySQL is accessible on localhost:3307 (Docker)"
    else
        echo "⚠️  Cannot connect to MySQL on localhost:3307"
        echo "   Please ensure MySQL container is running in Docker"
        echo "   Start with: docker compose up -d mysql"
        echo ""
        echo "   You can continue anyway, but the app may fail to start..."
        echo ""
    fi
elif command -v timeout &> /dev/null; then
    if timeout 1 bash -c "cat < /dev/null > /dev/tcp/localhost/3307" 2>/dev/null; then
        echo "✅ MySQL is accessible on localhost:3307 (Docker)"
    else
        echo "⚠️  Cannot connect to MySQL on localhost:3307"
        echo "   Please ensure MySQL container is running in Docker"
        echo "   Start with: docker compose up -d mysql"
        echo ""
        echo "   You can continue anyway, but the app may fail to start..."
        echo ""
    fi
else
    echo "⚠️  Cannot check MySQL connection (nc or timeout command not available)"
    echo "   Please ensure MySQL container is running on localhost:3307"
    echo ""
fi

# Check if Redis is running in Docker on port 6379
echo "Checking Redis connection (Docker)..."
if command -v nc &> /dev/null; then
    if nc -z localhost 6379 2>/dev/null; then
        echo "✅ Redis is accessible on localhost:6379 (Docker)"
    else
        echo "⚠️  Cannot connect to Redis on localhost:6379"
        echo "   Please ensure Redis container is running in Docker"
        echo "   Start with: docker compose up -d redis"
        echo ""
        echo "   You can continue anyway, but caching features may not work..."
        echo ""
    fi
elif command -v timeout &> /dev/null; then
    if timeout 1 bash -c "cat < /dev/null > /dev/tcp/localhost/6379" 2>/dev/null; then
        echo "✅ Redis is accessible on localhost:6379 (Docker)"
    else
        echo "⚠️  Cannot connect to Redis on localhost:6379"
        echo "   Please ensure Redis container is running in Docker"
        echo "   Start with: docker compose up -d redis"
        echo ""
        echo "   You can continue anyway, but caching features may not work..."
        echo ""
    fi
else
    echo "⚠️  Cannot check Redis connection (nc or timeout command not available)"
    echo "   Please ensure Redis container is running on localhost:6379"
    echo ""
fi

# Check if email service is running (optional)
echo "Checking Email Service (Docker)..."
if command -v nc &> /dev/null; then
    if nc -z localhost 8200 2>/dev/null; then
        echo "✅ Email Service is accessible on localhost:8200 (Docker)"
    else
        echo "⚠️  Email Service is not running (optional)"
        echo "   Start with: docker compose up -d email-service"
    fi
elif command -v timeout &> /dev/null; then
    if timeout 1 bash -c "cat < /dev/null > /dev/tcp/localhost/8200" 2>/dev/null; then
        echo "✅ Email Service is accessible on localhost:8200 (Docker)"
    else
        echo "⚠️  Email Service is not running (optional)"
        echo "   Start with: docker compose up -d email-service"
    fi
fi

echo ""
echo "Starting Spring Boot application with 'local' profile..."
echo "=========================================="
echo ""

# Detect and set JAVA_HOME if not set (for WSL users with Java on Windows)
if [ -z "$JAVA_HOME" ]; then
    echo "JAVA_HOME not set, attempting to detect Java..."
    
    # Check common Windows Java locations in WSL
    WINDOWS_JAVA_PATHS=(
        "/mnt/c/Program Files/Java/jdk-21"
        "/mnt/c/Program Files/Java/jdk-17"
        "/mnt/c/Program Files/Java/jdk-11"
        "/mnt/c/Program Files/Java/jdk"
        "/mnt/c/Program Files (x86)/Java/jdk-21"
        "/mnt/c/Program Files (x86)/Java/jdk-17"
        "/mnt/c/Program Files (x86)/Java/jdk-11"
        "/mnt/c/Program Files (x86)/Java/jdk"
    )
    
    for java_path in "${WINDOWS_JAVA_PATHS[@]}"; do
        if [ -d "$java_path" ] && [ -f "$java_path/bin/java.exe" ]; then
            # Use Windows-style path with forward slashes for WSL
            export JAVA_HOME="$java_path"
            # Also set PATH to include Java bin directory
            export PATH="$JAVA_HOME/bin:$PATH"
            echo "✅ Found Java at: $JAVA_HOME"
            break
        fi
    done
    
    # If still not found, try to find via java command
    if [ -z "$JAVA_HOME" ]; then
        JAVA_CMD=$(command -v java 2>/dev/null || echo "")
        if [ -n "$JAVA_CMD" ]; then
            JAVA_HOME=$(readlink -f "$JAVA_CMD" 2>/dev/null | sed 's|/bin/java||' || dirname "$(dirname "$JAVA_CMD")")
            export JAVA_HOME
            echo "✅ Detected JAVA_HOME from java command: $JAVA_HOME"
        fi
    fi
    
    # If still not found, show error
    if [ -z "$JAVA_HOME" ]; then
        echo "❌ JAVA_HOME not found!"
        echo ""
        echo "Please set JAVA_HOME manually:"
        echo "  export JAVA_HOME=/mnt/c/Program\ Files/Java/jdk-21"
        echo "  # or install Java in WSL:"
        echo "  sudo apt update && sudo apt install openjdk-21-jdk"
        echo ""
        exit 1
    fi
else
    echo "✅ Using JAVA_HOME: $JAVA_HOME"
fi

# Verify Java is accessible
if [ -n "$JAVA_HOME" ] && [ -f "$JAVA_HOME/bin/java.exe" ]; then
    # Windows Java in WSL
    JAVA_CMD="$JAVA_HOME/bin/java.exe"
elif [ -n "$JAVA_HOME" ] && [ -f "$JAVA_HOME/bin/java" ]; then
    # Linux Java
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    JAVA_CMD=$(command -v java 2>/dev/null || echo "")
fi

if [ -z "$JAVA_CMD" ] || [ ! -f "$JAVA_CMD" ]; then
    echo "❌ Java executable not found at: $JAVA_HOME/bin/java"
    exit 1
fi

# Verify JAVA_HOME is accessible by Maven wrapper
echo "Verifying Java setup..."
if [ -n "$JAVA_HOME" ] && [ -f "$JAVA_HOME/bin/java.exe" ]; then
    echo "✅ Java executable found: $JAVA_HOME/bin/java.exe"
    "$JAVA_HOME/bin/java.exe" -version 2>&1 | head -1
    
    # Note: Cannot create symlinks in Windows filesystem from WSL
    # Create temporary wrapper scripts in project directory (Linux filesystem) for Maven compatibility
    # Maven wrapper expects JAVA_HOME/bin/java and JAVA_HOME/bin/javac
    WRAPPER_DIR="$(pwd)/.java-wrapper"
    WRAPPER_BIN_DIR="$WRAPPER_DIR/bin"
    
    if [ ! -f "$WRAPPER_BIN_DIR/java" ]; then
        mkdir -p "$WRAPPER_BIN_DIR"
        
        # Store original Windows Java path
        ORIGINAL_JAVA_HOME="$JAVA_HOME"
        
        cat > "$WRAPPER_BIN_DIR/java" << EOF
#!/bin/bash
# Wrapper script for java.exe from Windows Java in WSL
ORIG_JAVA_HOME="${ORIGINAL_JAVA_HOME}"
exec "\$ORIG_JAVA_HOME/bin/java.exe" "\$@"
EOF
        chmod +x "$WRAPPER_BIN_DIR/java"
        
        if [ -f "$ORIGINAL_JAVA_HOME/bin/javac.exe" ]; then
            cat > "$WRAPPER_BIN_DIR/javac" << EOF
#!/bin/bash
# Wrapper script for javac.exe from Windows Java in WSL
ORIG_JAVA_HOME="${ORIGINAL_JAVA_HOME}"
exec "\$ORIG_JAVA_HOME/bin/javac.exe" "\$@"
EOF
            chmod +x "$WRAPPER_BIN_DIR/javac"
        fi
    fi
    
    # Set JAVA_HOME to wrapper directory so Maven wrapper finds java/javac in bin/
    export JAVA_HOME="$WRAPPER_DIR"
    echo "✅ Created Java wrapper scripts for Maven compatibility"
    echo "   JAVA_HOME set to: $JAVA_HOME"
    echo "   Java wrapper: $WRAPPER_BIN_DIR/java"
    
elif [ -n "$JAVA_HOME" ] && [ -f "$JAVA_HOME/bin/java" ]; then
    echo "✅ Java executable found: $JAVA_HOME/bin/java"
    "$JAVA_HOME/bin/java" -version 2>&1 | head -1
fi

# Use Maven wrapper if available, otherwise use system Maven
if [ -f "./mvnw" ]; then
    echo ""
    echo "Using Maven wrapper (./mvnw)..."
    echo "JAVA_HOME=$JAVA_HOME"
    
    # For WSL with Windows Java, ensure JAVA_HOME is properly set
    # Maven wrapper may have issues with spaces in path, so we ensure it's exported
    export JAVA_HOME
    
    # Run mvnw with proper JAVA_HOME
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=local || {
        echo ""
        echo "❌ Maven wrapper failed. This might be due to JAVA_HOME path with spaces."
        echo ""
        echo "Workaround options:"
        echo "1. Install Java in WSL:"
        echo "   sudo apt update && sudo apt install openjdk-21-jdk"
        echo "   export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64"
        echo ""
        echo "2. Or set JAVA_HOME manually before running:"
        echo "   export JAVA_HOME=\"/mnt/c/Program Files/Java/jdk-21\""
        echo "   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local"
        exit 1
    }
elif command -v mvn &> /dev/null; then
    echo "Using system Maven..."
    JAVA_HOME="$JAVA_HOME" mvn spring-boot:run -Dspring-boot.run.profiles=local
else
    echo "❌ Maven not found!"
    echo "Please install Maven or use the Maven wrapper (./mvnw)"
    exit 1
fi

