# Multi-stage build for User Service
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# First, build and install micropay-events dependency
WORKDIR /app/micropay-events
COPY micropay-events/pom.xml ./pom.xml
COPY micropay-events/src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean install -DskipTests -B

# Now build user-service
WORKDIR /app/user-service
COPY user-service/pom.xml ./

# Copy source code
COPY user-service/src ./src

# Build application
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Minimal tools for healthchecks
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy JAR from builder
COPY --from=builder /app/user-service/target/user-service-*.jar app.jar

# Change ownership
RUN chown spring:spring app.jar

USER spring:spring

# JVM memory limits
ENV JAVA_OPTS="-Xms128m -Xmx256m"

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
  CMD curl -fsS http://localhost:8082/actuator/health || exit 1

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]