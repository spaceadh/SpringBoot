#!/bin/bash

# Define variables
SERVICE_NAME="deeppoemsinc"        # Name of your Docker service or container
IMAGE_NAME="deeppoemsinc"          # Name of your Docker image
DOCKER_COMPOSE_FILE="docker-compose.yml"  # Path to your docker-compose file

# Print the current directory
echo "Current Directory:"
pwd

# Stop and remove the running container
echo "Stopping and removing the existing Docker container..."
docker-compose down || { echo "Failed to stop and remove the Docker container"; exit 1; }

# Build the Docker image
echo "Building the Docker image..."
docker build -t $IMAGE_NAME . || { echo "Docker build failed"; exit 1; }

# Start the Docker container
echo "Starting the Docker container..."
docker-compose up -d || { echo "Failed to start the Docker container"; exit 1; }

# Check the status of the container
echo "Checking the status of the Docker container..."
docker ps | grep $SERVICE_NAME && echo "Docker service is running" || { echo "Docker service failed to start"; exit 1; }


# Print success message
echo "Docker build successful and the service is running"