FROM openjdk:17-jdk-slim

WORKDIR /app

COPY employee-management-0.0.1-SNAPSHOT.jar app.jar
COPY src/main/resources/certs/aiven.jks /app/aiven.jks

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
