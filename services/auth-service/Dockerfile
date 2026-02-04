# Multi-stage build for Auth Service
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# First, build and install micropay-events dependency
WORKDIR /app/micropay-events
COPY micropay-events/pom.xml ./pom.xml
COPY micropay-events/src ./src
RUN mvn clean install -DskipTests -B

# Now build auth-service
WORKDIR /app/auth-service
COPY auth-service/pom.xml ./

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY auth-service/src ./src

# Build application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy JAR from builder
COPY --from=builder /app/auth-service/target/auth-service-*.jar app.jar

# Change ownership
RUN chown spring:spring app.jar

USER spring:spring

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
