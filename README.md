# eBanking Transactions API

A comprehensive Spring Boot microservice for managing paginated account transactions with Kafka integration, JWT authentication, and currency conversion capabilities.

## 🏗️ Architecture Overview

The application follows a layered architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    API Layer (REST)                        │
├─────────────────────────────────────────────────────────────┤
│                 Application Layer                          │
│              (Services, Security)                          │
├─────────────────────────────────────────────────────────────┤
│                 Domain Layer                               │
│         (Entities, Value Objects)                         │
├─────────────────────────────────────────────────────────────┤
│              Infrastructure Layer                          │
│    (Database, Kafka, External APIs)                       │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 Features

- **📱 REST API**: Paginated transaction retrieval with OpenAPI/Swagger documentation
- **🔐 JWT Authentication**: Secure token-based authentication and authorization
- **📊 Currency Conversion**: Real-time exchange rate integration for multi-currency support
- **🔄 Kafka Integration**: Event-driven architecture for transaction processing
- **📈 Monitoring**: Prometheus metrics, health checks, and structured logging
- **🐳 Containerization**: Docker support with multi-stage builds
- **☸️ Kubernetes**: Production-ready K8s manifests with auto-scaling
- **🧪 Testing**: Comprehensive unit, integration, and contract tests

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.1.2
- **Language**: Java 17
- **Database**: PostgreSQL (production), H2 (development/testing)
- **Message Broker**: Apache Kafka
- **Security**: Spring Security with JWT
- **Documentation**: OpenAPI 3 / Swagger
- **Monitoring**: Micrometer + Prometheus
- **Testing**: JUnit 5, Testcontainers, Mockito
- **Build Tool**: Maven
- **Containerization**: Docker
- **Orchestration**: Kubernetes

## 📋 Prerequisites

- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- PostgreSQL (for production)
- Apache Kafka (for message processing)

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd ebanking-transactions-api
```

### 2. Local Development with H2

```bash
# Run with embedded H2 database
mvn spring-boot:run
```

The application will start on `http://localhost:8080` with:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console: `http://localhost:8080/h2-console`
- Health Check: `http://localhost:8080/actuator/health`

### 3. Run with Docker Compose (Recommended)

```bash
# Start all services (PostgreSQL, Kafka, API)
docker-compose up -d

# View logs
docker-compose logs -f ebanking-api

# Stop services
docker-compose down
```

### 4. Build Docker Image

```bash
# Build the application
mvn clean package

# Build Docker image
docker build -t nadeemr/ebanking-transactions-api:latest .
```

## 🔧 Configuration

### Environment Variables

| Variable                  | Description            | Default                      |
| ------------------------- | ---------------------- | ---------------------------- |
| `DB_USERNAME`             | Database username      | `ebanking_user`              |
| `DB_PASSWORD`             | Database password      | `ebanking_password`          |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses | `localhost:9092`             |
| `JWT_SECRET`              | JWT signing secret     | (see application.properties) |
| `API_BASE_URL`            | API base URL           | `http://localhost:8080`      |

### Application Profiles

- **default**: H2 database, embedded Kafka (development)
- **prod**: PostgreSQL, external Kafka (production)
- **test**: H2 database, disabled Kafka (testing)

## 📡 API Documentation

### Authentication

All API endpoints require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

### Sample JWT Token Generation

```bash
# For testing purposes (customize the secret and payload)
curl -X POST "http://localhost:8080/auth/token" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "P-0123456789",
    "role": "CUSTOMER"
  }'
```

### Main Endpoints

#### GET /api/v1/transactions

Retrieve paginated transactions for the authenticated customer.

**Parameters:**

- `yearMonth` (required): YYYY-MM format (e.g., "2023-10")
- `page` (optional): Page number (0-based, default: 0)
- `size` (optional): Page size (1-100, default: 20)
- `baseCurrency` (optional): Currency for conversion (default: "EUR")
- `accountIban` (optional): Filter by specific account

**Example Request:**

```bash
curl -X GET "http://localhost:8080/api/v1/transactions?yearMonth=2023-10&page=0&size=20&baseCurrency=EUR" \
  -H "Authorization: Bearer <your-jwt-token>"
```

**Example Response:**

```json
{
  "transactions": [
    {
      "id": "89d3o179-abcd-465b-o9ee-e2d5f6ofEld46",
      "amount": 100.5,
      "currency": "GBP",
      "convertedAmount": 114.79,
      "baseCurrency": "EUR",
      "accountIban": "GB82WEST12345698765432",
      "valueDate": "2023-10-01",
      "description": "Online payment GBP"
    }
  ],
  "pageInfo": {
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "summary": {
    "totalCredit": 114.79,
    "totalDebit": 0.0,
    "netAmount": 114.79,
    "baseCurrency": "EUR"
  }
}
```

#### POST /api/v1/transactions/search

Advanced search with request body for complex criteria.

**Request Body:**

```json
{
  "yearMonth": "2023-10",
  "page": 0,
  "size": 20,
  "baseCurrency": "EUR",
  "accountIban": "GB82WEST12345698765432"
}
```

## 🔄 Kafka Integration

### Transaction Events

The application consumes transaction events from Kafka topic `transaction-events`.

**Event Schema:**

```json
{
  "transactionId": "89d3o179-abcd-465b-o9ee-e2d5f6ofEld46",
  "amount": 100.5,
  "currency": "GBP",
  "accountIban": "GB82WEST12345698765432",
  "valueDate": "2023-10-01",
  "description": "Online payment GBP",
  "customerId": "P-0123456789",
  "eventType": "CREATE",
  "timestamp": "2023-10-01T10:00:00Z"
}
```

**Event Types:**

- `CREATE`: New transaction
- `UPDATE`: Transaction modification
- `DELETE`: Transaction removal

### Producing Test Events

```bash
# Send a sample transaction event
echo '{
  "transactionId": "test-123",
  "amount": 150.75,
  "currency": "EUR",
  "accountIban": "DE89370400440532013000",
  "valueDate": "2023-10-15",
  "description": "Test transaction",
  "customerId": "P-0123456789",
  "eventType": "CREATE"
}' | kafka-console-producer.sh --broker-list localhost:9092 --topic transaction-events
```

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Test Categories

1. **Unit Tests**: Fast tests for individual components
2. **Integration Tests**: Test complete workflows with Testcontainers
3. **Contract Tests**: API contract validation

### Test Coverage

```bash
# Generate coverage report
mvn jacoco:report

# View report
open target/site/jacoco/index.html
```

### Test with Testcontainers

The integration tests use Testcontainers for PostgreSQL and Kafka:

```bash
# Run integration tests (requires Docker)
mvn test -Dtest=TransactionIntegrationTest
```

## ☸️ Kubernetes Deployment

### Prerequisites

- Kubernetes cluster (1.19+)
- kubectl configured
- Helm (optional, for easier management)

### Deploy to Kubernetes

```bash
# Apply all manifests
kubectl apply -f k8s/

# Check deployment status
kubectl get pods -n ebanking
kubectl get services -n ebanking

# View logs
kubectl logs -f deployment/ebanking-api -n ebanking
```

### Kubernetes Resources

The deployment includes:

- **Namespace**: Isolated environment
- **ConfigMap**: Application configuration
- **Secret**: Sensitive data (DB credentials, JWT secret)
- **Deployment**: Application pods with 3 replicas
- **Service**: Internal load balancing
- **Ingress**: External access with TLS
- **HPA**: Horizontal Pod Autoscaler (3-10 replicas)
- **PostgreSQL**: Database with persistent storage
- **Kafka**: Message broker

### Auto-scaling Configuration

```yaml
# CPU-based scaling: 70% threshold
# Memory-based scaling: 80% threshold
# Min replicas: 3, Max replicas: 10
```

### Monitoring URLs

- **Application**: `https://api.ebanking.nadeemr.com`
- **Swagger UI**: `https://api.ebanking.nadeemr.com/swagger-ui.html`
- **Health Check**: `https://api.ebanking.nadeemr.com/actuator/health`
- **Metrics**: `https://api.ebanking.nadeemr.com/actuator/prometheus`

## 📊 Monitoring & Observability

### Health Checks

```bash
# Application health
curl http://localhost:8080/actuator/health

# Detailed health with database and Kafka status
curl http://localhost:8080/actuator/health | jq
```

### Metrics

Prometheus metrics available at `/actuator/prometheus`:

- Application metrics (JVM, HTTP requests, custom business metrics)
- Database connection pool metrics
- Kafka consumer metrics
- Cache hit/miss ratios

### Logging

Structured JSON logging with correlation IDs:

```json
{
  "timestamp": "2023-10-01T10:00:00.000Z",
  "level": "INFO",
  "logger": "com.nadeemr.ebanking.api.controller.TransactionController",
  "message": "Getting transactions for customer P-0123456789",
  "customerId": "P-0123456789",
  "correlationId": "abc-123-def"
}
```

### Grafana Dashboard

Import the provided dashboard from `docker/grafana-dashboard.json` for:

- Request rate and latency
- Error rates
- JVM metrics
- Database connection pools
- Kafka consumer lag

## 🔒 Security

### Authentication & Authorization

- JWT-based authentication
- Role-based access control (RBAC)
- Customer isolation (customers can only access their own data)
- Secure headers and CORS configuration

### Security Headers

- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Strict-Transport-Security` (HTTPS only)

### Secure Configuration

- Non-root container user
- Read-only root filesystem
- Dropped capabilities
- Resource limits
- Network policies (in production)

## 🚨 Error Handling

### HTTP Status Codes

- `200 OK`: Successful request
- `400 Bad Request`: Invalid input parameters
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Unexpected error

### Error Response Format

```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": "Date must be in YYYY-MM format",
  "timestamp": "2023-10-01T10:00:00.000Z"
}
```

## 🔄 Data Models

### Transaction Entity

```java
{
  "id": "UUID",
  "amount": "BigDecimal",
  "currency": "String (3 chars)",
  "accountIban": "String (IBAN format)",
  "valueDate": "LocalDate",
  "description": "String (max 500 chars)",
  "customerId": "String (P-xxxxxxxxxx format)",
  "createdAt": "LocalDateTime",
  "updatedAt": "LocalDateTime"
}
```

### Supported Currencies

- EUR (Euro)
- USD (US Dollar)
- GBP (British Pound)
- CHF (Swiss Franc)
- JPY (Japanese Yen)

Additional currencies can be added by extending the `ExchangeRateProvider`.

## 🚀 Performance Considerations

### Optimizations

1. **Database Indexing**: Optimized indexes for customer + date queries
2. **Connection Pooling**: HikariCP with optimal pool size
3. **Caching**: Exchange rates cached for 1 hour
4. **Pagination**: Efficient offset-based pagination
5. **Async Processing**: Kafka consumers run asynchronously

### Scaling Recommendations

- **Horizontal Scaling**: Use Kubernetes HPA for auto-scaling
- **Database**: Read replicas for read-heavy workloads
- **Kafka**: Partition by customer ID for parallel processing
- **Caching**: Redis for distributed caching in multi-instance setups

**By Nadeem Rasheed**
