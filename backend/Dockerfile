# Multi-stage Docker build for Java 21 Spring Boot
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven Wrapper and POM
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Ensure mvnw has execute permissions
RUN chmod +x ./mvnw

# Copy source code and build production JAR
COPY src src
RUN ./mvnw clean package -DskipTests

# Stage 2: Lightweight JRE runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the generated JAR
COPY --from=build /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE ${PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]
