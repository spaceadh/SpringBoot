# Stage 1: Build the application
FROM maven:3.8.1-openjdk-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and source code into the container
COPY pom.xml .
COPY src ./src

# Package the application without running tests
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM openjdk:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from the build stage to the run stage
COPY --from=build /app/target/deeppoemsinc-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 7072

# docker login -u spaceadh
# dckr_pat_np-WrjfARWekaybuQw7RBW572T8
# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]