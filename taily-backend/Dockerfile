#
# Multi-stage build for Render (or any container runtime).
# - Builds the Spring Boot jar inside the image
# - Runs on a lightweight JRE image
#

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache deps first
COPY taily-backend/pom.xml ./pom.xml
RUN mvn -q -DskipTests dependency:go-offline

# Build
COPY taily-backend/src ./src
RUN mvn -q -DskipTests package


FROM eclipse-temurin:21-jre
WORKDIR /app

# Create a non-root user
RUN useradd -r -u 10001 appuser
USER appuser

COPY --from=build /app/target/taily-backend-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

# Render provides $PORT; Spring reads it via server.port=${PORT:8080}
ENV PORT=8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

