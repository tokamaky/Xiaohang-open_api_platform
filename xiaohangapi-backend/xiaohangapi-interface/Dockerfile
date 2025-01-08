# Use Maven with JDK 17 for building the project
FROM openjdk:17-jdk-slim



WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && apt-get clean

# Copy all local dependencies and the main application into the container
COPY ../xiaohangapi-common /app/xiaohangapi-common
COPY ../xiaohangapi-client-sdk /app/xiaohangapi-client-sdk
COPY ./xiaohangapi-interface /app/xiaohangapi-interface

# Install local dependencies
RUN mvn install -f /app/xiaohangapi-common/pom.xml
RUN mvn install -f /app/xiaohangapi-client-sdk/pom.xml

# Build the main application (interface)
WORKDIR /app/xiaohangapi-interface
RUN mvn clean package -DskipTests
# Expose the application port
EXPOSE 8123


# Run the application (interface)
CMD ["java", "-jar", "/app/xiaohangapi-interface/target/xiaohangapi-interface-0.0.1-SNAPSHOT.jar"]
