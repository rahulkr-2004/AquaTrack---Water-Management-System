# Multi-stage Dockerfile for AquaTrack Spring Boot Backend (Repo Root)
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy Maven files from water directory
COPY water/pom.xml .
COPY water/src ./src

# Build production JAR without tests
RUN mvn clean package -DskipTests -B

# Extract fat JAR safely to app.jar
RUN cp $(ls target/*.jar | grep -v 'original' | head -n 1) app.jar

# Runtime stage - slim JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy built JAR
COPY --from=build /app/app.jar app.jar

# Expose default port
EXPOSE 8080

# Run Spring Boot app with urandom entropy for fast startup
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
