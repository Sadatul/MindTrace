FROM maven:3.9.7-eclipse-temurin-21-alpine AS build
WORKDIR /home/app

COPY ./pom.xml /home/app/pom.xml
COPY ./src/main/java/com/sadi/backend/configs/SecretsPropertySource.java /home/app/src/main/java/com/sadi/backend/configs/SecretsPropertySource.java
COPY ./src/main/java/com/sadi/backend/BackendApplication.java /home/app/src/main/java/com/sadi/backend/BackendApplication.java
RUN mvn -f /home/app/pom.xml clean package

COPY . /home/app
RUN mvn -f /home/app/pom.xml clean package -DskipTests

FROM gcr.io/distroless/java21-debian12
EXPOSE 8080
COPY --from=build /home/app/target/*.jar backend.jar
ARG profile=dev
ENV spring_profiles_active=${profile}
CMD ["-jar", "backend.jar"]
