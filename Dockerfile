# Multi-stage Dockerfile for AquaTrack Spring Boot Backend (Repo Root)
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# 1. Cache Maven dependencies layer
COPY water/pom.xml .
RUN mvn dependency:go-offline -B || true

# 2. Build production JAR without tests
COPY water/src ./src
RUN mvn package -DskipTests -B

# Safely extract single fat JAR
RUN cp $(ls target/*.jar | grep -v 'original' | head -n 1) app.jar

# Runtime stage - ultra-fast Alpine JRE with tuned memory and JIT compiler flags
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy built JAR from build stage
COPY --from=build /app/app.jar app.jar

# Expose default port (Render will bind to $PORT dynamically)
EXPOSE 8080

# Environment and JVM optimization for fast cold starts on Render free tier (512MB RAM):
# -XX:+UseContainerSupport respects container cgroup limits
# -XX:MaxRAMPercentage=75.0 prevents container OOM kills
# -Xss512k reduces per-thread stack overhead
# -XX:+TieredCompilation -XX:TieredStopAtLevel=1 avoids expensive C2 JIT compiler overhead at boot, cutting startup time by over 50%
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Xss512k", \
  "-XX:+TieredCompilation", \
  "-XX:TieredStopAtLevel=1", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
