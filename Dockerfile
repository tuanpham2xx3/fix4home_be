FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies first for better caching
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source files and build the project
COPY src ./src
RUN mvn package -DskipTests

# Use a smaller JRE image for the runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port for the application
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
