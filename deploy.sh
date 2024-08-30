#!/bin/bash

# Define the path to the JAR file
WORKSPACE_DIR="/var/lib/jenkins/workspace/My project 1"  # Update this to your workspace directory
JAR_NAME="deeppoemsinc-0.0.1-SNAPSHOT.jar"          # Update this to your JAR file name
JAR_PATH="/home/springboot/app.jar"

# Define your sudo password
SUDO_PASSWORD="CleopatraBorn@69BC"

# Print the current directory
echo "Current Directory:"
pwd

# List the contents of the current directory
ls

# Print the current step
echo "Building JAR file"

# Build the JAR file using Maven
mvn clean package || { echo "Maven build failed"; exit 1; }

# Move to the target directory
cd target || { echo "Target directory not found"; exit 1; }

# List the contents of the target directory
ls

# Ensure the /home/springboot directory exists
sudo mkdir -p /home/springboot || { echo "Failed to create /home/springboot directory"; exit 1; }

# Remove existing app.jar if it exists
if [ -f "$JAR_PATH" ]; then
  sudo rm "$JAR_PATH" || { echo "Failed to remove existing $JAR_PATH"; exit 1; }
fi

# Move the JAR file to /home/springboot/app.jar
sudo mv deeppoemsinc-0.0.1-SNAPSHOT.jar "$JAR_PATH" || { echo "Failed to move JAR file"; exit 1; }

# Move the JAR file to /home/springboot/app.jar
echo $SUDO_PASSWORD | sudo -S systemctl restart springboot-app || { echo "Failed to restart springboot-app service"; exit 1; }

# Print success message
echo "JAR file successfully built, moved, and service restarted"