# Wallet Service

## Overview
The Wallet Service is a core microservice of the E-Wallet Payment System responsible for managing customer wallets, account balances, and fund reservations. It provides RESTful APIs for balance validation, amount reservation, and fee collection.

## Features
- **Customer Wallet Management**: Create and manage customer wallets with multi-currency support (USD, EUR, INR)
- **Balance Validation**: Check if customers have sufficient funds before transactions
- **Amount Reservation**: Reserve funds for pending transactions with distributed locking
- **Reservation Confirmation**: Confirm or release reserved amounts
- **Fee Collection**: Deduct transaction fees from customer wallets
- **Audit Logging**: Track all wallet operations for compliance
- **Optimistic Locking**: Prevent concurrent update conflicts
- **Distributed Locking**: Redis-based locking for concurrent operations

## Technology Stack
- **Framework**: Spring Boot 3.2.1
- **Database**: PostgreSQL with Flyway migrations
- **Cache/Lock**: Redis for distributed locking
- **Service Discovery**: Netflix Eureka
- **Tracing**: Micrometer with Zipkin
- **API Documentation**: SpringDoc OpenAPI
- **Build Tool**: Maven

## Architecture

### Entities
1. **Customer**: User information (id, name, email)
2. **Wallet**: Customer wallet with balance and currency
3. **WalletReservation**: Temporary fund reservations
4. **AuditLog**: Complete audit trail of all operations

### Key Components
- **WalletController**: REST API endpoints
- **WalletService**: Business logic and transaction management
- **RedisLockService**: Distributed locking mechanism
- **GlobalExceptionHandler**: Centralized error handling

## API Endpoints

### Get Wallet
```
GET /api/wallet/{customerId}
```
Returns wallet details for a customer.

### Validate Balance
```
POST /api/wallet/validate-balance
Body: {
  "customerId": "uuid",
  "amount": 100.00,
  "currency": "USD"
}
```
Checks if customer has sufficient balance.

### Reserve Amount
```
POST /api/wallet/reserve
Body: {
  "customerId": "uuid",
  "amount": 100.00,
  "currency": "USD",
  "transactionId": "TXN123"
}
```
Reserves amount from wallet for a transaction.

### Confirm Reservation
```
POST /api/wallet/confirm-reservation/{reservationId}
```
Confirms a pending reservation.

### Release Reservation
```
POST /api/wallet/release-reservation/{reservationId}
```
Releases a reservation and refunds the amount.

### Collect Fee
```
POST /api/wallet/collect-fee
Body: {
  "customerId": "uuid",
  "amount": 5.00,
  "transactionId": "TXN123"
}
```
Deducts fee from customer wallet.

## Configuration

### Application Properties (application.yml)
```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/walletdb
    username: postgres
    password: postgres
  
  data:
    redis:
      host: localhost
      port: 6379

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

## Database Schema

### Sample Data
The service comes pre-loaded with sample data:
- **Customer 1**: John Doe - $1000 USD
- **Customer 2**: Jane Smith - €800 EUR
- **Customer 3**: Bob Johnson - ₹50,000 INR

## Running the Service

### Prerequisites
- JDK 17+
- PostgreSQL 12+
- Redis 6+
- Maven 3.6+

### Setup Database
```bash
# Create database
createdb walletdb

# Flyway will automatically run migrations on startup
```

### Build and Run
```bash
# Build
mvn clean package

# Run
mvn spring-boot:run
```

The service will be available at `http://localhost:8081`

### API Documentation
Once running, access Swagger UI at:
```
http://localhost:8081/swagger-ui.html
```

## Error Handling
The service includes comprehensive error handling for:
- **InsufficientBalanceException**: When balance is insufficient
- **WalletNotFoundException**: When wallet is not found
- **ReservationNotFoundException**: When reservation is not found
- **InvalidCurrencyException**: When currency doesn't match
- **LockAcquisitionException**: When distributed lock cannot be acquired

## Security & Reliability

### Concurrency Control
- **Optimistic Locking**: JPA versioning prevents lost updates
- **Pessimistic Locking**: Database-level locking for critical reads
- **Distributed Locking**: Redis locks prevent race conditions

### Transaction Management
- All state-changing operations are transactional
- Automatic rollback on exceptions
- Audit logging for all operations

## Monitoring
- **Health Check**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Distributed Tracing**: Integrated with Zipkin

## Testing
```bash
# Run tests
mvn test

# Run with coverage
mvn clean verify
```

## Development

### Package Structure
```
com.ewallet.wallet
├── config          # Configuration classes
├── controller      # REST controllers
├── dto             # Data Transfer Objects
├── entity          # JPA entities
├── exception       # Custom exceptions
├── repository      # Spring Data repositories
└── service         # Business logic
```

## License
Part of E-Wallet Payment System - Internal Use Only
