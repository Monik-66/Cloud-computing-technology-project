# Stage 1: Build with pre-installed Maven 3.9 & Java 21
# (Prevents HTTP 429 'Too Many Requests' error from Maven Central during wrapper download)
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom and source
COPY pom.xml .
COPY src src

# Build production JAR using pre-installed Maven
RUN mvn clean package -DskipTests

# Stage 2: Minimal Java 21 JRE runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy built JAR from builder stage
COPY --from=build /app/target/*.jar app.jar

# Render dynamic port
ENV PORT=8080
EXPOSE ${PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]
