## --- Build stage ---
FROM maven:3.9-amazoncorretto-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests clean package

## --- Runtime stage ---
FROM amazoncorretto:25
WORKDIR /app
COPY --from=build /app/target/account-v1-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
