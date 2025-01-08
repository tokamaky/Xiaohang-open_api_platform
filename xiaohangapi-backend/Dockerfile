FROM openjdk:17-jdk-slim

WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && apt-get clean

# Copy all local dependencies and the main application into the container
COPY ./xiaohangapi-common /app/xiaohangapi-common
COPY ./xiaohangapi-client-sdk /app/xiaohangapi-client-sdk
COPY ./ /app/xiaohangapi-backend

# Install local dependencies
RUN mvn install -f /app/xiaohangapi-common/pom.xml
RUN mvn install -f /app/xiaohangapi-client-sdk/pom.xml

# Build the main application
WORKDIR /app/xiaohangapi-backend
RUN mvn clean package -DskipTests

# Expose the default Spring Boot port
EXPOSE 7529

# Run the Spring Boot application
CMD ["java", "-jar", "/app/xiaohangapi-backend/target/xiaohangapi-backend-0.0.1-SNAPSHOT.jar"]
