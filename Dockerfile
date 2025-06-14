FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy your app JAR
COPY employee-management-0.0.1-SNAPSHOT.jar app.jar

# Copy the PEM certificate
COPY ca.pem /app/ca.pem

# Convert PEM to JKS truststore
RUN keytool -importcert \
    -alias aivenmysql \
    -file /app/ca.pem \
    -keystore /app/aiven.jks \
    -storepass changeit \
    -noprompt

EXPOSE 8080

# Set trust store properties and run your app
ENTRYPOINT ["java", "-Djavax.net.ssl.trustStore=/app/aiven.jks", "-Djavax.net.ssl.trustStorePassword=changeit", "-jar", "app.jar"]
