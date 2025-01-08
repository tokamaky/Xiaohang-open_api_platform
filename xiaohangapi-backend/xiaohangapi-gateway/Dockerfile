FROM openjdk:17-jdk-slim

WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && apt-get clean

# Copy all local dependencies and the main application into the container
COPY ../xiaohangapi-common /app/xiaohangapi-common
COPY ../xiaohangapi-client-sdk /app/xiaohangapi-client-sdk
COPY ./xiaohangapi-gateway /app/xiaohangapi-gateway

# Install local dependencies
RUN mvn install -f /app/xiaohangapi-common/pom.xml
RUN mvn install -f /app/xiaohangapi-client-sdk/pom.xml

# Build the main application (Gateway)
WORKDIR /app/xiaohangapi-gateway
RUN mvn clean package -DskipTests
# Expose the port that the application will run on
EXPOSE 8090

# Run the application (Gateway)
CMD ["java", "-jar", "/app/xiaohangapi-gateway/target/xiaohangapi-gateway-0.0.1-SNAPSHOT.jar"]
