# Use ARM64 compatible Java runtime
FROM eclipse-temurin:17-jre-jammy

# Install curl tool for health checks
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    # Create dedicated user to run application with non-root privileges
    groupadd -r spring && \
    useradd -r -g spring spring

# Define build argument for application version
ARG APP_VERSION=1.0.0

# Metadata labels
LABEL maintainer="qchen"
LABEL application="transaction-management"
LABEL version="${APP_VERSION}"

# Switch to non-root user
USER spring
WORKDIR /app

# Copy local JAR file to container using version argument
COPY ./target/transaction-management-${APP_VERSION}.jar app.jar

# JVM configuration optimization
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"
# Set application context path
ENV SPRING_APPLICATION_JSON='{"server.servlet.context-path":"/transaction-management"}'

# Health check (adjust according to actual health check endpoint)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/transaction-management/actuator/health || exit 1

# Expose application port
EXPOSE 8080

# Startup command (Fixed: using JSON array format)
ENTRYPOINT ["java", "-jar", "app.jar"]