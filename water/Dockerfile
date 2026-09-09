# Multi-stage Dockerfile for Spring Boot deployment on Render / Koyeb / Cloud
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build production JAR
RUN mvn clean package -DskipTests -B

# Safely extract single fat JAR
RUN cp $(ls target/*.jar | grep -v 'original' | head -n 1) app.jar

# Runtime stage - use slim JRE image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy built JAR from build stage
COPY --from=build /app/app.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
