# Payment Service - Saga Orchestrator

## Overview
The Payment Service is the core orchestrator of the E-Wallet Payment System, implementing the **Saga Pattern** to manage distributed transactions across multiple microservices. It ensures atomicity, consistency, and reliability in payment processing through compensating transactions.

## Architecture

### Saga Pattern Implementation
The Payment Service uses **Orchestration-based Saga** where it acts as the central coordinator for the following steps:

1. **Validate Balance** - Check if customer has sufficient funds
2. **Reserve Wallet** - Reserve the payment amount in customer's wallet
3. **Credit Merchant** - Add funds to merchant's account
4. **Update Ledger** - Record transaction in the ledger service
5. **Send Notifications** - Notify both customer and merchant
6. **Collect Fee** - Deduct 1% transaction fee from customer

### Compensation Flow
In case of failure at any step, the saga executes compensation in reverse order:
- **Release Reservation** - Free reserved wallet funds
- **Debit Merchant** - Reverse merchant credit
- **Reverse Ledger** - Create reversal entry in ledger

## Key Features

### 1. Idempotency
- Every payment request includes a unique `transactionId`
- Duplicate requests are rejected with `409 Conflict`
- Ensures exactly-once processing

### 2. Retry Mechanism
- Each step retries up to 3 times on failure
- Exponential backoff strategy (1s, 2s, 3s)
- Failed steps trigger compensation

### 3. Async Execution
- Payment processing runs asynchronously using `CompletableFuture`
- Immediate response to client with `INITIATED` status
- Non-blocking saga execution

### 4. Full Audit Trail
- Every state transition logged to `audit_logs` table
- Includes action, from/to status, and details
- Enables complete transaction traceability

### 5. Step Tracking
- Each saga step tracked in `saga_steps` table
- Captures step name, status, attempt count, errors
- Supports debugging and monitoring

## Database Schema

### Tables

#### payment_transactions
```sql
- id (UUID, PK)
- transaction_id (VARCHAR, UNIQUE)
- customer_id (VARCHAR)
- merchant_id (VARCHAR)
- product_name (VARCHAR)
- product_description (VARCHAR)
- amount (DECIMAL)
- currency (ENUM: USD, EUR, INR)
- status (ENUM: INITIATED, WALLET_RESERVED, MERCHANT_CREDITED, 
          LEDGER_UPDATED, NOTIFICATION_SENT, FEE_COLLECTED, COMPLETED, FAILED)
- current_step (VARCHAR)
- error_message (VARCHAR)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

#### saga_steps
```sql
- id (UUID, PK)
- payment_transaction_id (UUID, FK)
- step_name (VARCHAR)
- status (ENUM: PENDING, COMPLETED, COMPENSATING, COMPENSATED, FAILED)
- attempt (INT)
- error_message (VARCHAR)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

#### audit_logs
```sql
- id (UUID, PK)
- payment_transaction_id (VARCHAR)
- action (VARCHAR)
- from_status (VARCHAR)
- to_status (VARCHAR)
- details (VARCHAR)
- created_at (TIMESTAMP)
```

### Indexes
- `idx_transaction_id` - Unique index on transaction_id
- `idx_customer_id` - Index on customer_id
- `idx_merchant_id` - Index on merchant_id
- `idx_status` - Index on payment status
- `idx_payment_transaction` - Index on saga steps by transaction
- `idx_created_at` - Index on audit log timestamps

## API Endpoints

### POST /api/payments/initiate
Initiates a new payment transaction.

**Request:**
```json
{
  "customerId": "CUST123",
  "merchantId": "MERCH456",
  "product": {
    "name": "Premium Subscription",
    "description": "Annual premium plan"
  },
  "amount": 99.99,
  "currency": "USD",
  "transactionId": "optional-unique-id"
}
```

**Response (201 Created):**
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "INITIATED",
  "message": "Payment initiated successfully"
}
```

**Error Responses:**
- `400 Bad Request` - Validation errors
- `409 Conflict` - Duplicate transaction
- `500 Internal Server Error` - System error

### GET /api/payments/{transactionId}/status
Retrieves the current status of a payment transaction.

**Response (200 OK):**
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "currentStep": "COMPLETED",
  "amount": 99.99,
  "currency": "USD",
  "createdAt": "2024-01-15T10:30:00"
}
```

**Error Response:**
- `404 Not Found` - Transaction not found

## Feign Clients

### WalletServiceClient
**Base URL:** http://localhost:8081
- `GET /api/wallets/{customerId}/balance` - Validate balance
- `POST /api/wallets/reserve` - Reserve amount
- `POST /api/wallets/confirm/{transactionId}` - Confirm reservation
- `POST /api/wallets/release/{transactionId}` - Release reservation
- `POST /api/wallets/collect-fee` - Collect transaction fee

### MerchantServiceClient
**Base URL:** http://localhost:8084
- `POST /api/merchants/credit` - Credit merchant account
- `POST /api/merchants/debit` - Debit merchant account (compensation)

### LedgerServiceClient
**Base URL:** http://localhost:8085
- `POST /api/ledger/entry` - Create ledger entry
- `POST /api/ledger/reverse/{transactionId}` - Reverse ledger entry

### NotificationServiceClient
**Base URL:** http://localhost:8083
- `POST /api/notifications/send` - Send notification

All clients have fallback implementations that throw exceptions on service unavailability.

## Configuration

### Application Properties
```yaml
server.port: 8082
spring.application.name: payment-service

# Database
spring.datasource.url: jdbc:postgresql://localhost:5432/paymentdb
spring.datasource.username: postgres
spring.datasource.password: postgres

# Flyway
spring.flyway.enabled: true
spring.flyway.baseline-on-migrate: true

# Eureka
eureka.client.service-url.defaultZone: http://localhost:8761/eureka/

# Zipkin
management.zipkin.tracing.endpoint: http://localhost:9411/api/v2/spans

# Feign
feign.client.config.default.connectTimeout: 5000
feign.client.config.default.readTimeout: 10000
```

## Error Handling

### Exception Types
- **PaymentNotFoundException** - Transaction not found (404)
- **DuplicateTransactionException** - Duplicate transaction ID (409)
- **SagaFailureException** - Saga execution failure (500)
- **MethodArgumentNotValidException** - Validation errors (400)

### GlobalExceptionHandler
Provides consistent error responses with:
- Timestamp
- HTTP status code
- Error type
- Error message
- Request path

## Monitoring & Observability

### Actuator Endpoints
- `/actuator/health` - Health check
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

### Distributed Tracing
- **Zipkin integration** for distributed tracing
- Trace ID propagation across microservices
- 100% sampling for development

### Logging
- Step-by-step saga execution logs
- Retry attempt logs
- Compensation logs
- Audit trail in database

## Transaction Fee
- **Fee Rate:** 1% of transaction amount
- **Collection:** After successful notification
- **Rounding:** Half-up to 2 decimal places
- **Transaction ID:** `{original-transaction-id}-FEE`

## Best Practices

### 1. Idempotent Operations
All external service calls should be idempotent to handle retries safely.

### 2. Timeout Management
- Feign client timeouts configured
- Avoid long-running transactions
- Use async processing

### 3. Compensation Safety
Compensation steps must be carefully designed to avoid partial rollbacks.

### 4. Monitoring
- Monitor saga completion rates
- Track step failure rates
- Alert on compensation frequency

### 5. Database Indices
Ensure proper indexing for:
- Transaction ID lookups
- Status queries
- Customer/Merchant filtering
- Audit log searches

## Running the Service

### Prerequisites
- PostgreSQL database running on port 5432
- Eureka server on port 8761
- Dependent services (Wallet, Merchant, Ledger, Notification)

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

### Docker
```bash
docker build -t payment-service .
docker run -p 8082:8082 payment-service
```

## Testing

### Unit Tests
Test individual components:
- Service layer logic
- Compensation logic
- Retry mechanism

### Integration Tests
Test with mock Feign clients:
- End-to-end saga flow
- Failure scenarios
- Compensation flow

### Load Tests
Verify performance under load:
- Concurrent payment processing
- Database connection pooling
- Async execution capacity

## Swagger UI
Access API documentation at:
```
http://localhost:8082/swagger-ui.html
```

## OpenAPI Spec
View OpenAPI specification at:
```
http://localhost:8082/api-docs
```

## Future Enhancements

1. **Event Sourcing** - Store all events for complete history
2. **CQRS** - Separate read and write models
3. **Dead Letter Queue** - Handle permanently failed transactions
4. **Circuit Breaker** - Add Resilience4j for fault tolerance
5. **Saga Timeout** - Add timeout for long-running sagas
6. **Webhook Notifications** - Notify clients of status changes
7. **Admin API** - Manual compensation trigger
8. **Metrics Dashboard** - Real-time saga monitoring

## Support
For issues or questions, contact the platform team.
