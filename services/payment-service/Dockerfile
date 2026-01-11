FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy JAR file (assumes JAR is built before Docker build)
COPY target/payment-service-*.jar app.jar

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose port
EXPOSE 8084

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=production
ENV DB_URL=jdbc:postgresql://postgres:5432/micropay_payment_db
ENV KAFKA_BOOTSTRAP_SERVERS=kafka:9092
ENV EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]




