#!/bin/bash
# Define variables
SERVICE_NAME="deeppoemsinc"        # Name of your Docker service or container
MONGO_NAME="mongo:5.0"            # Name of the MongoDB Docker image
MYSQL_NAME="mariadb:10.5"          # Name of the MySQL Docker image
IMAGE_NAME="deeppoemsinc"          # Name of your Docker image
DOCKER_COMPOSE_FILE="docker-compose.yml"  # Path to your docker-compose file

# Print the current directory
echo "Current Directory:"
pwd

# Check if any Docker containers are running
if [ "$(docker ps -q)" ]; then
  echo "Stopping all running Docker containers..."
  docker-compose down || { echo "Failed to stop running Docker containers"; exit 1; }

  echo "Removing all stopped Docker containers... Line 18"
  docker stop $(docker ps -q) || { echo "Failed to stop running Docker containers"; exit 1; }
#   echo "Removing all stopped Docker containers..."
#   docker rm $(docker ps -a -q) || { echo "Failed to remove Docker containers"; exit 1; }
else
  echo "No running Docker containers found."
fi

# Check if the Docker container for the app is running
if [ "$(docker ps -q -f name=$SERVICE_NAME)" ]; then
  echo "Stopping and removing the existing Docker container for $SERVICE_NAME..."
else
  echo "No running container with name $SERVICE_NAME found."
fi

# Check if MongoDB container is running
if [ "$(docker ps -q -f name=$MONGO_NAME)" ]; then
  echo "Stopping and removing the existing MongoDB container..."
  # docker stop $MONGO_NAME || { echo "Failed to stop the MongoDB container"; exit 1; }
else
  echo "No running MongoDB container found."
fi

# Check if MySQL container is running
if [ "$(docker ps -q -f name=$MYSQL_NAME)" ]; then
  echo "Stopping and removing the existing MySQL container..."
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