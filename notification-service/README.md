# Notification Service

The Notification Service is responsible for sending email notifications to customers and merchants in the E-Wallet Payment System. It consumes messages from Kafka topics and sends email notifications asynchronously.

## Features

- **Email Notifications**: Send notifications to customers and merchants
- **Kafka Integration**: Consumes notification requests from Kafka topics
- **Async Processing**: Non-blocking email sending with retry logic
- **Audit Logging**: Track all notification events
- **Multiple Notification Types**: Customer payments, merchant payments, payment failures, fee collection
- **Service Discovery**: Registered with Eureka for service discovery
- **Distributed Tracing**: Integrated with Zipkin for request tracing
- **Database Persistence**: Store notification history in PostgreSQL

## Architecture

### Components

1. **Controllers**
   - `NotificationController`: REST endpoints for manual notification triggers

2. **Services**
   - `NotificationService`: Core notification processing logic
   - `EmailService`: Handles actual email sending with retry logic

3. **Kafka Consumers**
   - `NotificationKafkaConsumer`: Listens to payment-notifications topic

4. **Entities**
   - `Notification`: Main notification entity
   - `AuditLog`: Tracks notification status changes

5. **DTOs**
   - `NotificationRequest`: Request payload for notifications
   - `NotificationResponse`: Response with notification status

## Configuration

### Application Properties (application.yml)

```yaml
server:
  port: 8083

spring:
  application:
    name: notification-service
  
  datasource:
    url: jdbc:postgresql://localhost:5432/notificationdb
    username: postgres
    password: postgres
  
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: notification-service
    topic:
      payment-notifications: payment-notifications
  
  mail:
    host: localhost
    port: 1025
```

### Environment Variables

- `SERVER_PORT`: Server port (default: 8083)
- `DB_URL`: PostgreSQL database URL
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka broker addresses
- `EUREKA_URL`: Eureka server URL

## API Endpoints

### Send Customer Notification
```http
POST /api/notifications/customer
Content-Type: application/json

{
  "recipientId": "customer-123",
  "recipientEmail": "customer@example.com",
  "recipientName": "John Doe",
  "type": "CUSTOMER_PAYMENT",
  "subject": "Payment Confirmation",
  "message": "Your payment was successful",
  "transactionId": "tx-123",
  "amount": 100.00,
  "currency": "USD"
}
```

### Send Merchant Notification
```http
POST /api/notifications/merchant
Content-Type: application/json

{
  "recipientId": "merchant-456",
  "recipientEmail": "merchant@example.com",
  "recipientName": "Store ABC",
  "type": "MERCHANT_PAYMENT",
  "subject": "Payment Received",
  "message": "You received a payment",
  "transactionId": "tx-123",
  "amount": 100.00,
  "currency": "USD"
}
```

### Get Notifications by Recipient
```http
GET /api/notifications/recipient/{recipientId}
```

## Notification Types

- `CUSTOMER_PAYMENT`: Payment confirmation to customer
- `MERCHANT_PAYMENT`: Payment notification to merchant
- `PAYMENT_FAILED`: Failed payment notification
- `FEE_COLLECTED`: Fee collection notification

## Notification Status

- `PENDING`: Notification created, email not sent yet
- `SENT`: Email sent successfully
- `FAILED`: Email sending failed

## Kafka Integration

### Consumer
- **Topic**: `payment-notifications`
- **Group ID**: `notification-service`
- **Configuration**: Manual acknowledgment with 3 concurrent consumers

### Message Format
```json
{
  "recipientId": "string",
  "recipientEmail": "string",
  "recipientName": "string",
  "type": "CUSTOMER_PAYMENT",
  "subject": "string",
  "message": "string",
  "transactionId": "string",
  "amount": 0.0,
  "currency": "string"
}
```

## Database Schema

### Notifications Table
- `id`: UUID (Primary Key)
- `recipient_id`: Recipient identifier
- `recipient_email`: Email address
- `recipient_name`: Recipient name
- `type`: Notification type (enum)
- `subject`: Email subject
- `message`: Email message
- `transaction_id`: Associated transaction
- `amount`: Transaction amount
- `currency`: Currency code
- `status`: Notification status (enum)
- `sent_at`: Timestamp when sent
- `created_at`: Creation timestamp
- `updated_at`: Update timestamp

### Audit Logs Table
- `id`: UUID (Primary Key)
- `notification_id`: Foreign key to notifications
- `action`: Action performed
- `status`: Action status
- `details`: Additional details
- `created_at`: Creation timestamp

### Indexes
- `idx_recipient_id`: Index on recipient_id
- `idx_transaction_id`: Index on transaction_id
- `idx_status`: Index on status
- `idx_created_at`: Index on created_at

## Email Service

The email service uses Spring Mail with:
- **Async Processing**: Non-blocking email sending
- **Retry Logic**: 3 attempts with exponential backoff (2s, 4s, 8s)
- **Mock Mode**: Currently logs emails instead of sending (for development)

To enable actual email sending, uncomment the `mailSender.send(message)` line in `EmailService.java`.

## Error Handling

- **NotificationFailedException**: Thrown when notification processing fails
- **Validation Errors**: Bean validation on request DTOs
- **Global Exception Handler**: Centralized error handling with proper HTTP status codes

## Monitoring

- **Health Endpoint**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`
- **Distributed Tracing**: Zipkin integration with trace IDs in logs

## Running the Service

### Prerequisites
- Java 17 or higher
- PostgreSQL 12 or higher
- Kafka 3.x
- Eureka Server running on port 8761

### Database Setup
```sql
CREATE DATABASE notificationdb;
```

Flyway will automatically create tables and indexes on startup.

### Start the Service
```bash
cd notification-service
mvn spring-boot:run
```

### Docker Compose (Optional)
```yaml
services:
  notification-service:
    image: notification-service:latest
    ports:
      - "8083:8083"
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/notificationdb
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      EUREKA_URL: http://eureka:8761/eureka/
```

## Testing

Run unit tests:
```bash
mvn test
```

Run integration tests:
```bash
mvn verify
```

## Development Notes

1. **Email Mock Mode**: By default, emails are logged instead of sent. Configure a real SMTP server in production.

2. **Async Processing**: Email sending is asynchronous to avoid blocking the main thread.

3. **Retry Logic**: Failed emails are automatically retried 3 times with exponential backoff.

4. **Audit Trail**: All notification events are logged in the audit_logs table.

5. **Kafka Consumer**: Uses manual acknowledgment to ensure message processing reliability.

## Production Considerations

1. Configure a real SMTP server or email service (SendGrid, AWS SES, etc.)
2. Set appropriate pool sizes for async executor based on load
3. Configure Kafka consumer concurrency based on throughput requirements
4. Set up database connection pooling
5. Enable SSL/TLS for Kafka and database connections
6. Configure proper logging levels
7. Set up alerting for failed notifications
8. Monitor queue depths and processing times

## Dependencies

- Spring Boot 3.x
- Spring Cloud Netflix Eureka Client
- Spring Kafka
- Spring Data JPA
- Spring Mail
- PostgreSQL Driver
- Flyway Core
- Lombok
- SpringDoc OpenAPI
- Micrometer Tracing (Brave)
- Zipkin Reporter

## License

Copyright © 2024 E-Wallet Team
