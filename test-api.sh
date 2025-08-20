#!/bin/bash

# Test script for the eBanking Transactions API

BASE_URL=${1:-http://localhost:8080}
CUSTOMER_ID="P-0123456789"

echo "🧪 Testing eBanking Transactions API"
echo "🌐 Base URL: $BASE_URL"
echo ""

# Test 1: Health check
echo "1️⃣  Testing health check..."
response=$(curl -s -w "HTTP_CODE:%{http_code}" "$BASE_URL/health")
http_code=$(echo "$response" | grep -o 'HTTP_CODE:[0-9]*' | cut -d: -f2)
body=$(echo "$response" | sed 's/HTTP_CODE:[0-9]*$//')

if [ "$http_code" = "200" ]; then
    echo "✅ Health check passed"
    echo "   Response: $body"
else
    echo "❌ Health check failed (HTTP $http_code)"
    exit 1
fi

echo ""

# Test 2: Actuator health
echo "2️⃣  Testing actuator health..."
response=$(curl -s -w "HTTP_CODE:%{http_code}" "$BASE_URL/actuator/health")
http_code=$(echo "$response" | grep -o 'HTTP_CODE:[0-9]*' | cut -d: -f2)

if [ "$http_code" = "200" ]; then
    echo "✅ Actuator health check passed"
else
    echo "❌ Actuator health check failed (HTTP $http_code)"
fi

echo ""

# Test 3: API documentation
echo "3️⃣  Testing API documentation..."
response=$(curl -s -w "HTTP_CODE:%{http_code}" "$BASE_URL/v3/api-docs")
http_code=$(echo "$response" | grep -o 'HTTP_CODE:[0-9]*' | cut -d: -f2)

if [ "$http_code" = "200" ]; then
    echo "✅ API documentation available"
else
    echo "❌ API documentation not available (HTTP $http_code)"
fi

echo ""

# Test 4: Transactions API without auth (should fail)
echo "4️⃣  Testing transactions API without authentication..."
response=$(curl -s -w "HTTP_CODE:%{http_code}" "$BASE_URL/api/v1/transactions?yearMonth=2023-10")
http_code=$(echo "$response" | grep -o 'HTTP_CODE:[0-9]*' | cut -d: -f2)

if [ "$http_code" = "401" ]; then
    echo "✅ Authentication protection working (HTTP 401)"
else
    echo "❌ Authentication protection not working (HTTP $http_code)"
fi

echo ""

# Note about JWT token
echo "📝 Note: To test authenticated endpoints, you need a valid JWT token."
echo "   Example:"
echo "   curl -H \"Authorization: Bearer <jwt-token>\" \\"
echo "        \"$BASE_URL/api/v1/transactions?yearMonth=2023-10\""

echo ""

# Test 5: Metrics endpoint
echo "5️⃣  Testing metrics endpoint..."
response=$(curl -s -w "HTTP_CODE:%{http_code}" "$BASE_URL/actuator/prometheus")
http_code=$(echo "$response" | grep -o 'HTTP_CODE:[0-9]*' | cut -d: -f2)

if [ "$http_code" = "200" ]; then
    echo "✅ Metrics endpoint available"
else
    echo "❌ Metrics endpoint not available (HTTP $http_code)"
fi

echo ""
echo "🎉 Basic API tests completed!"
echo ""
echo "📋 Manual testing checklist:"
echo "   - Swagger UI: $BASE_URL/swagger-ui.html"
echo "   - H2 Console: $BASE_URL/h2-console (if using H2)"
echo "   - Health: $BASE_URL/actuator/health"
echo "   - Metrics: $BASE_URL/actuator/prometheus"
