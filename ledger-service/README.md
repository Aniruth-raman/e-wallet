# Ledger Service

The Ledger Service is a critical component of the E-Wallet Payment System responsible for maintaining an immutable audit trail of all financial transactions.

## Features

- **Transaction Recording**: Records all payment transactions with complete audit trail
- **Idempotency**: Prevents duplicate transaction entries using unique transaction IDs
- **Compensating Transactions**: Supports transaction reversals while maintaining history
- **Transaction History**: Provides paginated transaction history for customers and merchants
- **Comprehensive Auditing**: Maintains detailed audit logs for all operations
- **Production-Ready**: Built with validation, error handling, and observability

## Technology Stack

- **Spring Boot 3.2.1**: Core framework
- **Spring Data JPA**: Database access layer
- **PostgreSQL**: Primary database
- **Flyway**: Database migration management
- **Spring Cloud Netflix Eureka**: Service discovery
- **Micrometer/Zipkin**: Distributed tracing
- **SpringDoc OpenAPI**: API documentation
- **Lombok**: Boilerplate code reduction

## Architecture

### Entities

#### LedgerEntry
- **id**: Unique identifier (UUID)
- **transactionId**: Unique transaction identifier (for idempotency)
- **customerId**: Customer identifier
- **merchantId**: Merchant identifier
- **amount**: Transaction amount
- **currency**: Currency code (e.g., USD, EUR)
- **transactionType**: Type (PAYMENT, FEE, REFUND)
- **status**: Current status (PENDING, COMPLETED, REVERSED)
- **customerBalanceBefore/After**: Customer balance snapshots
- **merchantBalanceBefore/After**: Merchant balance snapshots
- **productName/Description**: Transaction details
- **createdAt/updatedAt**: Timestamps

#### AuditLog
- **id**: Unique identifier (UUID)
- **ledgerEntryId**: Reference to ledger entry
- **action**: Action performed (e.g., ENTRY_CREATED, ENTRY_REVERSED)
- **details**: Detailed description of the action
- **createdAt**: Timestamp

## API Endpoints

### Create Ledger Entry
```http
POST /api/ledger/transactions
Content-Type: application/json

{
  "transactionId": "TXN-123456",
  "customerId": "CUST-001",
  "merchantId": "MERCH-001",
  "amount": 100.00,
  "currency": "USD",
  "transactionType": "PAYMENT",
  "customerBalanceBefore": 500.00,
  "customerBalanceAfter": 400.00,
  "merchantBalanceBefore": 1000.00,
  "merchantBalanceAfter": 1100.00,
  "productName": "Premium Subscription",
  "productDescription": "Monthly premium subscription"
}
```

### Get Ledger Entry
```http
GET /api/ledger/transactions/{transactionId}
```

### Reverse Ledger Entry
```http
DELETE /api/ledger/transactions/{transactionId}
```

### Get Customer Transaction History
```http
GET /api/ledger/customers/{customerId}/transactions?page=0&size=20&sortBy=createdAt&direction=DESC
```

### Get Merchant Transaction History
```http
GET /api/ledger/merchants/{merchantId}/transactions?page=0&size=20&sortBy=createdAt&direction=DESC
```

## Configuration

### Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ledgerdb
    username: postgres
    password: postgres
```

### Service Discovery
```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

### Distributed Tracing
```yaml
management:
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

## Database Schema

### Indexes
- `idx_transaction_id`: Fast lookup by transaction ID
- `idx_customer_id`: Fast customer queries
- `idx_merchant_id`: Fast merchant queries
- `idx_created_at`: Time-based queries
- `idx_customer_created`: Composite index for customer history
- `idx_merchant_created`: Composite index for merchant history

### Constraints
- `transaction_id`: Unique constraint for idempotency
- `amount`: Check constraint (amount >= 0)
- `transaction_type`: Check constraint (PAYMENT, FEE, REFUND)
- `status`: Check constraint (PENDING, COMPLETED, REVERSED)

## Running the Service

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- Eureka Discovery Server running on port 8761

### Setup Database
```bash
# Create database
psql -U postgres
CREATE DATABASE ledgerdb;
```

### Build and Run
```bash
# Build the service
mvn clean install

# Run the service
mvn spring-boot:run
```

### Run with Docker
```bash
# Build Docker image
docker build -t ledger-service:latest .

# Run container
docker run -p 8085:8085 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/ledgerdb \
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://host.docker.internal:8761/eureka/ \
  ledger-service:latest
```

## API Documentation

Once the service is running, access the Swagger UI at:
```
http://localhost:8085/swagger-ui.html
```

API documentation in JSON format:
```
http://localhost:8085/api-docs
```

## Monitoring and Observability

### Health Check
```http
GET /actuator/health
```

### Metrics
```http
GET /actuator/metrics
```

### Prometheus Metrics
```http
GET /actuator/prometheus
```

## Key Design Decisions

### Idempotency
- Each transaction requires a unique `transactionId`
- Duplicate transaction IDs are rejected with `409 Conflict`
- Ensures exactly-once processing semantics

### Immutable Audit Trail
- Transactions are never deleted
- Reversals create new entries with `REVERSED` status
- Complete history maintained for compliance and auditing

### Balance Snapshots
- Records balance before and after each transaction
- Enables balance reconciliation and verification
- Provides complete financial audit trail

### Performance Optimization
- Strategic indexes on frequently queried columns
- Composite indexes for common query patterns
- Connection pooling with HikariCP

## Error Handling

The service provides comprehensive error handling with appropriate HTTP status codes:

- `400 Bad Request`: Validation errors, invalid state transitions
- `404 Not Found`: Ledger entry not found
- `409 Conflict`: Duplicate transaction ID
- `500 Internal Server Error`: Unexpected errors

All errors return a structured response:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Transaction with ID TXN-123456 already exists",
  "path": "/api/ledger/transactions"
}
```

## Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Run with coverage
mvn clean verify jacoco:report
```

## Security Considerations

- Input validation on all API endpoints
- SQL injection prevention through parameterized queries
- Transaction isolation for data consistency
- Audit logging for all operations

## Future Enhancements

- [ ] Event sourcing for complete transaction replay
- [ ] Read replicas for query optimization
- [ ] Archival strategy for old transactions
- [ ] Advanced analytics and reporting
- [ ] Real-time transaction monitoring
- [ ] Blockchain integration for enhanced immutability

## License

Copyright © 2024 E-Wallet Payment System
