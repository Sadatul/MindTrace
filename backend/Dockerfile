# Multi-stage build optimized for Docker layer caching
FROM maven:3.9.7-eclipse-temurin-21-alpine AS dependencies

# Create app directory
WORKDIR /home/app

# Copy only pom.xml first to cache dependencies
COPY ./pom.xml /home/app/pom.xml

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Build stage
FROM maven:3.9.7-eclipse-temurin-21-alpine AS build
WORKDIR /home/app

# Copy pom.xml and download dependencies (reuse cache from dependencies stage)
COPY ./pom.xml /home/app/pom.xml
RUN mvn dependency:go-offline -B

# Copy source code
COPY ./src /home/app/src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM gcr.io/distroless/java21-debian12
EXPOSE 8080

# Copy the built jar
COPY --from=build /home/app/target/*.jar backend.jar

# Set profile argument and environment
ARG profile=dev
ENV spring_profiles_active=${profile}
CMD ["-jar", "backend.jar"]
