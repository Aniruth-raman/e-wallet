# Merchant Service

The Merchant Service is a microservice in the E-Wallet Payment System that manages merchant accounts, wallets, and transactions.

## Features

- **Merchant Wallet Management**: Manage merchant wallets with multiple currency support (USD, EUR, INR)
- **Transaction Processing**: Credit and debit operations with full audit trail
- **Balance Inquiry**: Real-time balance retrieval
- **Transaction Tracking**: Track transaction status and history
- **Audit Logging**: Complete audit trail for all wallet operations
- **Optimistic Locking**: Prevents concurrent modification issues using JPA versioning

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Data JPA**
- **PostgreSQL** (Database)
- **Flyway** (Database Migration)
- **Spring Cloud Netflix Eureka** (Service Discovery)
- **Micrometer Tracing** (Distributed Tracing)
- **Zipkin** (Trace Collection)
- **SpringDoc OpenAPI** (API Documentation)
- **Lombok** (Boilerplate Reduction)

## Project Structure

```
merchant-service/
├── src/
│   ├── main/
│   │   ├── java/com/ewallet/merchant/
│   │   │   ├── controller/          # REST Controllers
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── entity/              # JPA Entities
│   │   │   ├── exception/           # Custom Exceptions & Handler
│   │   │   ├── repository/          # JPA Repositories
│   │   │   ├── service/             # Business Logic
│   │   │   └── MerchantServiceApplication.java
│   │   └── resources/
│   │       ├── application.yml      # Configuration
│   │       └── db/migration/        # Flyway SQL Scripts
│   └── test/
│       └── java/                    # Test Classes
└── pom.xml
```

## Database Schema

### Tables

1. **merchants** - Merchant business information
2. **merchant_wallets** - Merchant wallet accounts with currency and balance
3. **merchant_transactions** - Transaction records (CREDIT/DEBIT)
4. **audit_logs** - Audit trail for all wallet operations

### Sample Data

The service comes pre-configured with 3 sample merchants:

1. **Electronics Store** (UUID: 550e8400-e29b-41d4-a716-446655440001)
   - Account: MWLT-ELEC-USD-001
   - Currency: USD
   - Initial Balance: 0.00

2. **Bookstore** (UUID: 550e8400-e29b-41d4-a716-446655440002)
   - Account: MWLT-BOOK-EUR-002
   - Currency: EUR
   - Initial Balance: 0.00

3. **Grocery Shop** (UUID: 550e8400-e29b-41d4-a716-446655440003)
   - Account: MWLT-GROC-INR-003
   - Currency: INR
   - Initial Balance: 0.00

## API Endpoints

### 1. Get Merchant Balance

```http
GET /api/merchants/{merchantId}/balance?currency={currency}
```

**Response:**
```json
{
  "merchantId": "550e8400-e29b-41d4-a716-446655440001",
  "name": "Electronics Store",
  "email": "electronics@example.com",
  "businessType": "Electronics",
  "accountNumber": "MWLT-ELEC-USD-001",
  "balance": 1000.00,
  "currency": "USD"
}
```

### 2. Credit Merchant Account

```http
POST /api/merchants/{merchantId}/credit
```

**Request:**
```json
{
  "amount": 500.00,
  "currency": "USD",
  "transactionId": "TXN-123456"
}
```

**Response:**
```json
{
  "transactionId": "TXN-123456",
  "status": "COMPLETED",
  "amount": 500.00
}
```

### 3. Debit Merchant Account

```http
POST /api/merchants/{merchantId}/debit
```

**Request:**
```json
{
  "amount": 200.00,
  "transactionId": "TXN-123456"
}
```

**Response:**
```json
{
  "transactionId": "COMP-TXN-123456",
  "status": "COMPLETED",
  "amount": 200.00
}
```

### 4. Get Transaction Status

```http
GET /api/merchants/transactions/{transactionId}/status
```

**Response:**
```json
{
  "transactionId": "TXN-123456",
  "status": "COMPLETED",
  "amount": 500.00
}
```

## Configuration

### Database Setup

1. Create PostgreSQL database:
```sql
CREATE DATABASE merchantdb;
```

2. Update credentials in `application.yml` if needed:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/merchantdb
    username: postgres
    password: postgres
```

### Required Services

- **PostgreSQL**: Port 5432
- **Eureka Server**: Port 8761
- **Zipkin Server**: Port 9411

## Running the Service

### Using Maven

```bash
mvn spring-boot:run
```

### Using Java

```bash
mvn clean package
java -jar target/merchant-service-1.0.0-SNAPSHOT.jar
```

The service will start on **port 8084**.

## API Documentation

Once the service is running, access the Swagger UI:

```
http://localhost:8084/swagger-ui.html
```

OpenAPI JSON specification:
```
http://localhost:8084/api-docs
```

## Health Check

Check service health:

```bash
curl http://localhost:8084/actuator/health
```

## Key Features

### Transaction Handling

- **Idempotency**: Duplicate transaction IDs are rejected
- **Atomic Operations**: All operations are transactional
- **Optimistic Locking**: Version-based concurrency control
- **Audit Trail**: Every operation is logged

### Error Handling

- **MerchantNotFoundException**: When merchant doesn't exist
- **MerchantWalletNotFoundException**: When wallet for currency doesn't exist
- **InsufficientFundsException**: When debit amount exceeds balance
- **DuplicateTransactionException**: When transaction ID already exists
- **TransactionNotFoundException**: When transaction doesn't exist
- **Validation Errors**: For invalid request data

### Security & Reliability

- Input validation using Jakarta Validation
- Comprehensive error handling with meaningful messages
- Structured logging for troubleshooting
- Distributed tracing support
- Health check endpoints
- Database migration with Flyway

## Development

### Build

```bash
mvn clean compile
```

### Test

```bash
mvn test
```

### Package

```bash
mvn clean package
```

## Integration

This service integrates with:

- **Eureka Discovery Server**: Service registration and discovery
- **Zipkin**: Distributed tracing
- **Payment Service**: Receives merchant credit requests
- **Ledger Service**: Provides transaction data for accounting

## Business Logic

### Credit Operation

1. Validate merchant and wallet existence
2. Check for duplicate transaction ID
3. Add amount to wallet balance
4. Create transaction record with COMPLETED status
5. Log audit trail with old/new balance

### Debit Operation (Compensating Transaction)

1. Validate original credit transaction exists
2. Check sufficient balance
3. Subtract amount from wallet balance
4. Create compensating transaction record
5. Log audit trail with reversal details

## Monitoring

The service exposes the following actuator endpoints:

- `/actuator/health` - Health status
- `/actuator/info` - Application info
- `/actuator/metrics` - Metrics
- `/actuator/prometheus` - Prometheus metrics

## License

This service is part of the E-Wallet Payment System.
