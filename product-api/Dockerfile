# ==================== BUILD STAGE ====================
# Use Maven with Java 17 to build the application
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Copy the pom.xml and download dependencies
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:resolve

# Copy the entire project source code
COPY . .

# Build the application (skip tests for faster build in Docker)
RUN mvn clean package -DskipTests

# ==================== RUNTIME STAGE ====================
# Use slim JDK 17 for the runtime (smaller image size)
FROM eclipse-temurin:17.0.11_9-jdk-alpine

# Set working directory for runtime
WORKDIR /app

# Create a non-root user for security best practices
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser

# Copy the JAR file from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Set ownership of the app directory to appuser
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose the port the application runs on
EXPOSE 8080

# Environment variables (can be overridden at runtime)
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Health check to monitor container status
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
