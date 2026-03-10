# Multi-stage build for Notification Service
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# First, build and install micropay-events dependency
WORKDIR /app/micropay-events
COPY micropay-events/pom.xml ./pom.xml
COPY micropay-events/src ./src
RUN mvn clean install -DskipTests -B

# Now build notification-service
WORKDIR /app/notification-service
COPY notification-service/pom.xml ./

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY notification-service/src ./src

# Build application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Minimal tools for container healthchecks
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy JAR from builder
COPY --from=builder /app/notification-service/target/notification-service-*.jar app.jar

# Change ownership
RUN chown spring:spring app.jar

USER spring:spring

# JVM memory limits for t2.micro (override via env if needed)
ENV JAVA_OPTS="-Xms128m -Xmx256m"

# Expose port
EXPOSE 8086

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
  CMD curl -fsS http://localhost:8086/actuator/health || exit 1

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
