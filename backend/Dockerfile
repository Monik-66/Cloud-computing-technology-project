# Lightweight Production Container for Java 21 Spring Boot
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy pre-built production JAR directly
# (Bypasses Maven Central 429 rate-limiting and compiles in 5 seconds on Render)
COPY deploy/app.jar app.jar

# Render assigns dynamic PORT environment variable (default 10000 or 8080)
ENV PORT=8080
EXPOSE ${PORT}

# Run Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
