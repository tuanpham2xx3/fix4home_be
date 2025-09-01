# Multi-stage Docker build for Fix4Home Backend
# Stage 1: Build application
FROM openjdk:21-jdk-slim AS builder

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies (this layer will be cached)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application (skip tests in Docker build for faster builds)
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime image
FROM openjdk:21-jre-slim AS runtime

# Install necessary packages for MySQL client (for healthchecks and debugging)
RUN apt-get update && \
    apt-get install -y curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create app user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create uploads directory and set permissions
RUN mkdir -p uploads && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8100

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8100/actuator/health || exit 1

# Start application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Default environment variables (can be overridden)
ENV SPRING_PROFILES_ACTIVE=production
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Add JAVA_OPTS to entrypoint
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
