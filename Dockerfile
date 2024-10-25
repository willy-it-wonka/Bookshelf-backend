FROM maven:3.9.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

COPY pom.xml .

COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar bookshelf-springboot.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "bookshelf-springboot.jar"]
