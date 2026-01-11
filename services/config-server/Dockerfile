FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create config-repo directory
RUN mkdir -p /app/config-repo

# Copy JAR file
COPY target/config-server-*.jar app.jar

# Expose port
EXPOSE 8888

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=native
ENV CONFIG_REPO_PATH=/app/config-repo

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8888/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

