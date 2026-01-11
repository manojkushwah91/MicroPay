FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy JAR file
COPY target/api-gateway-*.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables
ENV EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

