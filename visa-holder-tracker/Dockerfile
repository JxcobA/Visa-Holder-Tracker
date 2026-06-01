# =============================================================================
# Multi-stage Docker build for the Visa Holder Tracker application.
#
# Stages:
#   1. build -> compiles and packages the Spring Boot application
#   2. run   -> lightweight runtime container for production execution
#
# Benefits of multi-stage builds:
#   - smaller final image size
#   - reduced attack surface
#   - no Maven or JDK in production image
# =============================================================================

# ---- Stage 1: build ----

# Uses Eclipse Temurin JDK 17 on Alpine Linux
# for compiling the Spring Boot application.
FROM eclipse-temurin:17-jdk-alpine AS build

# Working directory inside the container.
WORKDIR /app

# Copy Maven wrapper scripts and configuration first.
#
# This improves Docker layer caching so dependencies
# are not re-downloaded unless Maven config changes.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Makes the Maven wrapper executable.
RUN chmod +x mvnw

# Downloads all Maven dependencies in advance.
#
# Improves future build speed by caching dependencies.
RUN ./mvnw dependency:go-offline -B

# Copy application source code into the container.
COPY src ./src

# Builds the Spring Boot application JAR.
#
# - clean       -> removes previous build artifacts
# - package     -> creates executable JAR
# -DskipTests   -> skips test execution during image build
# -B            -> batch mode for CI/CD environments
RUN ./mvnw clean package -DskipTests -B

# ---- Stage 2: run ----

# Lightweight JRE-only image used for production runtime.
#
# Smaller and more secure than a full JDK image.
FROM eclipse-temurin:17-jre-alpine

# Application working directory.
WORKDIR /app

# Creates a non-root user and group
# for improved container security.
RUN addgroup -S spring && adduser -S spring -G spring

# Runs the application as the non-root spring user.
USER spring:spring

# Copies the packaged JAR file
# from the build stage into the runtime image.
COPY --from=build /app/target/*.jar app.jar

# Documents the application port used by Spring Boot.
EXPOSE 9000

# Starts the Spring Boot application.
ENTRYPOINT ["java", "-jar", "app.jar"]