#!/bin/bash

# Build script for the eBanking Transactions API

set -e

echo "🏗️  Building eBanking Transactions API..."

# Clean and compile
echo "📦 Cleaning and compiling..."
mvn clean compile

# Run tests
echo "🧪 Running tests..."
mvn test

# Generate test coverage report
echo "📊 Generating test coverage report..."
mvn jacoco:report

# Package the application
echo "📦 Packaging application..."
mvn package -DskipTests

# Build Docker image
echo "Building Docker image..."
docker build -t nadeemr/ebanking-transactions-api:latest .

# Check if version tag was provided
if [ ! -z "$1" ]; then
    echo "Tagging with version: $1"
    docker tag nadeemr/ebanking-transactions-api:latest nadeemr/ebanking-transactions-api:$1
    echo "Image tagged as nadeemr/ebanking-transactions-api:$1"
fi

echo "Build completed successfully!"
echo "Available images:"
docker images | grep nadeemr/ebanking-transactions-api
echo ""
echo "Next steps:"
echo "  - Run locally: docker run -p 8080:8080 nadeemr/ebanking-transactions-api:latest"
echo "  - Deploy with compose: docker-compose up -d"
echo "  - Deploy to K8s: kubectl apply -f k8s/"
echo "  - View logs: docker logs <container-id>"
