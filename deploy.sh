#!/bin/bash

# Deployment script for Kubernetes

set -e

NAMESPACE="ebanking"
IMAGE_TAG=${1:-latest}

echo "🚀 Deploying eBanking Transactions API to Kubernetes..."
echo "📦 Image tag: $IMAGE_TAG"
echo "🎯 Namespace: $NAMESPACE"

# Create namespace if it doesn't exist
echo "📁 Creating namespace..."
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Apply all Kubernetes manifests
echo "📋 Applying manifests..."
kubectl apply -f k8s/

# Update image tag in deployment
if [ "$IMAGE_TAG" != "latest" ]; then
    echo "🏷️  Updating image tag to $IMAGE_TAG..."
    # Update the deployment image
echo "Updating deployment with image: nadeemr/ebanking-transactions-api:$IMAGE_TAG"
kubectl set image deployment/ebanking-api ebanking-api=nadeemr/ebanking-transactions-api:$IMAGE_TAG -n $NAMESPACE
fi

# Wait for deployment to be ready
echo "⏳ Waiting for deployment to be ready..."
kubectl rollout status deployment/ebanking-api -n $NAMESPACE --timeout=300s

# Display deployment information
echo "📊 Deployment status:"
kubectl get pods -n $NAMESPACE
kubectl get services -n $NAMESPACE
kubectl get ingress -n $NAMESPACE

echo ""
echo "✅ Deployment completed successfully!"
echo ""
echo "📋 Useful commands:"
echo "  - Check pods: kubectl get pods -n $NAMESPACE"
echo "  - View logs: kubectl logs -f deployment/ebanking-api -n $NAMESPACE"
echo "  - Port forward: kubectl port-forward service/ebanking-api-service 8080:80 -n $NAMESPACE"
echo "  - Delete deployment: kubectl delete namespace $NAMESPACE"
