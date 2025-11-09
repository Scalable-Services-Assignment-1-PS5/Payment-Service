# Payment Service

Payment processing microservice for the Event Ticketing System with mock payment gateway integration.

## Overview

The Payment Service handles payment processing, refunds, and payment record management. It includes a mock payment gateway with a 90% success rate for testing purposes.

## Tech Stack

### Core Framework

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Build Tool**: Maven 3.9

### Database

- **MySQL Connector** - Database connectivity
- **Hibernate** - ORM with DDL auto-update

### Security & Documentation

- **JWT (jjwt)**: 0.12.3 - Token-based authentication
- **SpringDoc OpenAPI**: 2.2.0 - API documentation (Swagger UI)

## Build & Run

### Prerequisites

- Java 17 or higher
- Maven 3.9+
- MySQL database (local or remote)

### 1. Build with Maven

```bash
# Clean and build
mvn clean package

# Skip tests (if needed)
mvn clean package -DskipTests
```

The JAR file will be created at: `target/payment-service-1.0.0.jar`

### 2. Run Locally

```bash
# Run with Java
java -jar target/payment-service-1.0.0.jar

# Or use Maven
mvn spring-boot:run
```

### 3. Build & Run with Docker

```bash
# Build Docker image
docker build -t payment-service:1.0.0 .

# Run container
docker run -d \
  -p 8085:8085 \
  -e DB_URL="jdbc:mysql://your-db-host:3306/payment_service_db" \
  -e DB_USERNAME="your-username" \
  -e DB_PASSWORD="your-password" \
  -e JWT_SECRET="your-secret-key-at-least-32-characters-long" \
  --name payment-service \
  payment-service:1.0.0
```

### 4. Run with Docker Compose

```bash
# From project root
docker-compose up payment-service
```

## Configuration

### Environment Variables

| Variable        | Description                   | Default             |
| --------------- | ----------------------------- | ------------------- |
| `DB_URL`      | MySQL database connection URL | See application.yml |
| `DB_USERNAME` | Database username             | `avnadmin`        |
| `DB_PASSWORD` | Database password             | -                   |
| `JWT_SECRET`  | Secret key for JWT validation | Required            |
| `SERVER_PORT` | Application port              | `8085`            |

### application.yml

```yaml
spring:
  application:
    name: payment-service
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
server:
  port: 8085
jwt:
  secret: ${JWT_SECRET}
```

## Exposed APIs

### Base URL

- **Local**: `http://localhost:8085`
- **Container**: `http://payment-service:8085`

### Authentication

All endpoints require JWT authentication via `Authorization: Bearer <token>` header (except health checks).

---

### 1. **Process Payment**

```http
POST /api/v1/payments/charge
```

**Headers:**

- `Authorization: Bearer <JWT_TOKEN>`
- `Content-Type: application/json`
- `Idempotency-Key: <unique-key>` (Required)

**Request Body:**

```json
{
  "orderId": "ORD-123",
  "amount": 1500.00
}
```

**Response (200 OK):**

```json
{
  "id": 1,
  "orderId": "ORD-123",
  "amount": 1500.00,
  "status": "SUCCESS",
  "transactionId": "TXN-abc123",
  "failureReason": null,
  "createdAt": "2025-11-09T10:30:00"
}
```

**cURL Example:**

```bash
curl -X POST http://localhost:8085/api/v1/payments/charge \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: PAY-$(date +%s)" \
  -d '{
    "orderId": "ORD-123",
    "amount": 1500.00
  }'
```

---

### 2. **Refund Payment**

```http
POST /api/v1/payments/refund
```

**Headers:**

- `Authorization: Bearer <JWT_TOKEN>`
- `Content-Type: application/json`

**Request Body:**

```json
{
  "paymentId": 1,
  "reason": "Order cancelled by customer"
}
```

**Response (200 OK):**

```json
{
  "id": 1,
  "orderId": "ORD-123",
  "amount": 1500.00,
  "status": "REFUNDED",
  "transactionId": "TXN-abc123",
  "failureReason": "Order cancelled by customer",
  "createdAt": "2025-11-09T10:30:00"
}
```

**cURL Example:**

```bash
curl -X POST http://localhost:8085/api/v1/payments/refund \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": 1,
    "reason": "Customer requested refund"
  }'
```

---

### 3. **Get Payment by ID**

```http
GET /api/v1/payments/{id}
```

**Headers:**

- `Authorization: Bearer <JWT_TOKEN>`

**Response (200 OK):**

```json
{
  "id": 1,
  "orderId": "ORD-123",
  "amount": 1500.00,
  "status": "SUCCESS",
  "transactionId": "TXN-abc123",
  "failureReason": null,
  "createdAt": "2025-11-09T10:30:00"
}
```

**cURL Example:**

```bash
curl -X GET http://localhost:8085/api/v1/payments/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 4. **Get Payment by Order ID**

```http
GET /api/v1/payments/order/{orderId}
```

**Headers:**

- `Authorization: Bearer <JWT_TOKEN>`

**Response (200 OK):**

```json
{
  "id": 1,
  "orderId": "ORD-123",
  "amount": 1500.00,
  "status": "SUCCESS",
  "transactionId": "TXN-abc123",
  "failureReason": null,
  "createdAt": "2025-11-09T10:30:00"
}
```

**cURL Example:**

```bash
curl -X GET http://localhost:8085/api/v1/payments/order/ORD-123 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 5. **Health Check** (No Auth Required)

```http
GET /actuator/health
```

**Response (200 OK):**

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

**cURL Example:**

```bash
curl http://localhost:8085/actuator/health
```

---

## Payment Status

| Status       | Description                               |
| ------------ | ----------------------------------------- |
| `PENDING`  | Payment initiated but not completed       |
| `SUCCESS`  | Payment successfully processed            |
| `FAILED`   | Payment failed (insufficient funds, etc.) |
| `REFUNDED` | Payment refunded to customer              |

## Security Features

- **JWT Authentication**: All payment endpoints require valid JWT tokens
- **Idempotency**: Duplicate payment prevention using idempotency keys
- **Validation**: Request body validation using Bean Validation

## 📚 API Documentation

### Swagger UI

Access interactive API documentation at:

```
http://localhost:8085/swagger-ui.html
```

### OpenAPI Specification

Get the OpenAPI JSON specification at:

```
http://localhost:8085/v3/api-docs
```

## Mock Payment Gateway

The service includes a mock payment gateway for testing:

- **Success Rate**: 90%
- **Failure Rate**: 10% (simulates insufficient funds)
- **Transaction ID**: Auto-generated UUID for successful payments

### Testing Scenarios

**Successful Payment:**

```bash
# Most requests will succeed (90% probability)
curl -X POST http://localhost:8085/api/v1/payments/charge \
  -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: TEST-$(date +%s)" \
  -H "Content-Type: application/json" \
  -d '{"orderId":"ORD-TEST-1","amount":100.00}'
```

**Expected Responses:**

Success:

```json
{
  "status": "SUCCESS",
  "transactionId": "TXN-uuid"
}
```

Failure (10% chance):

```json
{
  "status": "FAILED",
  "failureReason": "Insufficient funds (mock failure)"
}
```

## Database Schema

### Payments Table

```sql
CREATE TABLE payments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  payment_id VARCHAR(100) NOT NULL UNIQUE,
  order_id VARCHAR(255) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  currency VARCHAR(10) NOT NULL DEFAULT 'INR',
  payment_method VARCHAR(50),
  status ENUM('PENDING','SUCCESS','FAILED','REFUNDED') NOT NULL,
  transaction_id VARCHAR(255),
  idempotency_key VARCHAR(255) UNIQUE,
  failure_reason VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user (user_id),
  INDEX idx_status (status)
);
```

## Monitoring

### Health Checks

- **Endpoint**: `/actuator/health`
- **Interval**: 30 seconds (in Docker)
- **Checks**: Database connectivity, disk space, ping

### Logs

Application logs include:

- Payment processing events
- Success/failure notifications
- Idempotency key detections
- Refund operations

## Quick Start Example

```bash
# 1. Build the service
mvn clean package

# 2. Run the service
java -jar target/payment-service-1.0.0.jar

# 3. Check health
curl http://localhost:8085/actuator/health

# 4. Get a JWT token (from user-service)
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","password":"password123"}' | jq -r '.token')

# 5. Process a payment
curl -X POST http://localhost:8085/api/v1/payments/charge \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: PAY-$(date +%s)" \
  -d '{
    "orderId": "ORD-TEST-001",
    "amount": 2500.00
  }' | jq '.'
```

## Related Services

- **User Service** (8081): Authentication & user management
- **Order Service** (8084): Initiates payment requests
- **Catalog Service** (8082): Event information
- **Seating Service** (8083): Seat management
