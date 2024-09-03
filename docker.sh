#!/bin/bash
# Define variables
SERVICE_NAME="deeppoemsinc"        # Name of your Docker service or container
IMAGE_NAME="deeppoemsinc"          # Name of your Docker image
DOCKER_COMPOSE_FILE="docker-compose.yml"  # Path to your docker-compose file

# Print the current directory
echo "Current Directory:"
pwd

# Check if the Docker container for the app is running
if [ "$(docker ps -q -f name=$SERVICE_NAME)" ]; then
  echo "Stopping and removing the existing Docker container for $SERVICE_NAME..."
  docker-compose down || { echo "Failed to stop and remove the Docker container for $SERVICE_NAME"; exit 1; }
else
  echo "No running container with name $SERVICE_NAME found."
fi

# Check if MongoDB container is running
if [ "$(docker ps -q -f name=mongodb)" ]; then
  echo "Stopping and removing the existing MongoDB container..."
  docker stop mongodb || { echo "Failed to stop the MongoDB container"; exit 1; }
  # docker rm mongodb || { echo "Failed to remove the MongoDB container"; exit 1; }
else
  echo "No running MongoDB container found."
fi

# Check if MySQL container is running
if [ "$(docker ps -q -f name=mariadb)" ]; then
  echo "Stopping and removing the existing MySQL container..."
  docker stop mariadb || { echo "Failed to stop the MySQL container"; exit 1; }
  # docker rm mariadb || { echo "Failed to remove the MySQL container"; exit 1; }
else
  echo "No running MySQL container found."
fi

# Build the Docker image
echo "Building the Docker image..."
docker build -t $IMAGE_NAME . || { echo "Docker build failed"; exit 1; }

# Start the Docker container
echo "Starting the Docker container..."
docker-compose up -d || { echo "Failed to start the Docker container"; exit 1; }

# Check the status of the Docker container
echo "Checking the status of the Docker container..."
docker ps | grep $SERVICE_NAME && echo "Docker service is running" || { echo "Docker service failed to start"; exit 1; }

# Print success message
echo "Docker build successful and the service is running"