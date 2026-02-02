# API Gateway - E-Wallet Payment System

## Overview
The API Gateway serves as the single entry point for all client requests in the E-Wallet Payment System. It provides routing, authentication, rate limiting, and cross-cutting concerns for all microservices.

## Features
- **Intelligent Routing**: Routes requests to appropriate microservices
- **JWT Authentication**: Validates JWT tokens and extracts user information
- **Rate Limiting**: Redis-based rate limiting (100 requests/minute per IP)
- **Circuit Breaker**: Resilience4j circuit breakers for fault tolerance
- **CORS Support**: Configured for cross-origin requests
- **Request Tracing**: Correlation IDs and request tracking
- **Centralized Logging**: Comprehensive request/response logging
- **Health Monitoring**: Actuator endpoints for monitoring

## Architecture

### Port Configuration
- **API Gateway**: 8080
- **Discovery Server**: 8761
- **Redis**: 6379
- **Zipkin**: 9411

### Service Routes

| Route | Service | Port | Description |
|-------|---------|------|-------------|
| `/api/wallet/**` | wallet-service | 8081 | Wallet operations and user authentication |
| `/api/payments/**` | payment-service | 8082 | Payment processing and transactions |
| `/api/notifications/**` | notification-service | 8083 | Notification management |
| `/api/merchants/**` | merchant-service | 8084 | Merchant operations |
| `/api/ledger/**` | ledger-service | 8085 | Ledger and transaction history |

## JWT Authentication

### Overview
The API Gateway uses JWT (JSON Web Token) for authentication. All requests (except public endpoints) must include a valid JWT token.

### Public Endpoints (No Authentication Required)
- `/api/wallet/auth/login`
- `/api/wallet/auth/register`
- `/api/wallet/auth/refresh`
- `/actuator/health`
- `/actuator/info`

### JWT Token Format
```
Authorization: Bearer <JWT_TOKEN>
```

### JWT Claims
The gateway extracts and forwards the following claims to downstream services:
- `userId` - User ID (forwarded as `X-User-Id` header)
- `username` - Username (forwarded as `X-Username` header)
- `role` - User role (forwarded as `X-User-Role` header)

### JWT Configuration
- **Secret Key**: Configured in `application.yml`
- **Expiration**: 24 hours (86400000 ms)
- **Algorithm**: HS256 (HMAC-SHA256)

## Sample API Requests

### 1. User Registration (No Auth)
```bash
curl -X POST http://localhost:8080/api/wallet/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "phoneNumber": "+1234567890"
  }'
```

### 2. User Login (No Auth)
```bash
curl -X POST http://localhost:8080/api/wallet/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "password": "SecurePass123!"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "user-123",
  "username": "john.doe",
  "expiresIn": 86400000
}
```

### 3. Get Wallet Balance (With Auth)
```bash
curl -X GET http://localhost:8080/api/wallet/balance \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 4. Create Payment (With Auth)
```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100.00,
    "currency": "USD",
    "recipientId": "user-456",
    "description": "Payment for services"
  }'
```

### 5. Get Transaction History (With Auth)
```bash
curl -X GET http://localhost:8080/api/ledger/transactions?page=0&size=20 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 6. Send Notification (With Auth)
```bash
curl -X POST http://localhost:8080/api/notifications/send \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-123",
    "type": "EMAIL",
    "message": "Your payment was successful"
  }'
```

### 7. Register Merchant (With Auth)
```bash
curl -X POST http://localhost:8080/api/merchants/register \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "businessName": "Acme Corporation",
    "category": "RETAIL",
    "email": "info@acme.com"
  }'
```

## Rate Limiting

### Configuration
- **Strategy**: Redis-based token bucket algorithm
- **Rate**: 100 requests per minute per IP address
- **Burst Capacity**: 120 requests
- **Key Resolver**: IP-based (can be switched to user-based)

### Rate Limit Headers
When rate limited, the response includes:
```
HTTP/1.1 429 Too Many Requests
X-RateLimit-Remaining: 0
X-RateLimit-Replenish-Rate: 100
X-RateLimit-Burst-Capacity: 120
```

### Rate Limit Response
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please try again later."
}
```

## Circuit Breaker

### Configuration
Each service has its own circuit breaker with the following configuration:
- **Sliding Window Size**: 10 requests
- **Failure Rate Threshold**: 50%
- **Wait Duration in Open State**: 10 seconds
- **Half-Open State Calls**: 3

### Circuit Breaker States
1. **Closed**: Normal operation
2. **Open**: Service unavailable, requests fail fast
3. **Half-Open**: Testing if service recovered

### Fallback Responses
When a circuit breaker opens, the gateway returns:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 503,
  "error": "Service Unavailable",
  "message": "Wallet Service is currently unavailable. Please try again later.",
  "service": "Wallet Service"
}
```

## Request Tracing

### Correlation IDs
Every request gets assigned a unique correlation ID for distributed tracing:
- **X-Correlation-Id**: Tracks request across all services
- **X-Request-Id**: Unique identifier for each request

### Example Request Headers
```
X-Correlation-Id: a1b2c3d4-e5f6-7890-abcd-ef1234567890
X-Request-Id: x9y8z7w6-v5u4-t3s2-r1q0-p9o8n7m6l5k4
X-User-Id: user-123
X-Username: john.doe
X-User-Role: USER
```

## Monitoring & Health

### Actuator Endpoints
- **Health Check**: `GET /actuator/health`
- **Gateway Routes**: `GET /actuator/gateway/routes`
- **Circuit Breakers**: `GET /actuator/circuitbreakers`
- **Metrics**: `GET /actuator/metrics`
- **Rate Limiters**: `GET /actuator/ratelimiters`

### Health Check Response
```bash
curl http://localhost:8080/actuator/health
```

```json
{
  "status": "UP",
  "components": {
    "discoveryComposite": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    },
    "redis": {
      "status": "UP"
    }
  }
}
```

### Gateway Routes
```bash
curl http://localhost:8080/actuator/gateway/routes
```

## Security Best Practices

### Production Deployment
1. **Change JWT Secret**: Use a strong, randomly generated secret
2. **Enable HTTPS**: Configure SSL/TLS certificates
3. **Restrict CORS**: Limit allowed origins to specific domains
4. **Rate Limiting**: Adjust based on your traffic patterns
5. **API Keys**: Consider adding API key authentication for service-to-service calls
6. **Firewall Rules**: Restrict access to internal services

### Environment Variables
For production, externalize sensitive configuration:
```bash
export JWT_SECRET=your-production-secret-key
export REDIS_HOST=production-redis-host
export REDIS_PORT=6379
export EUREKA_URI=http://production-eureka:8761/eureka/
```

## Error Handling

### Standard Error Response
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/wallet/balance",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired JWT token",
  "requestId": "x9y8z7w6-v5u4-t3s2-r1q0-p9o8n7m6l5k4"
}
```

### Common HTTP Status Codes
- **200 OK**: Successful request
- **400 Bad Request**: Invalid request parameters
- **401 Unauthorized**: Missing or invalid JWT token
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **429 Too Many Requests**: Rate limit exceeded
- **503 Service Unavailable**: Circuit breaker open or service down

## Running the Gateway

### Prerequisites
- Java 17+
- Maven 3.6+
- Redis Server
- Eureka Discovery Server

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

Or with custom properties:
```bash
java -jar target/api-gateway-1.0.0-SNAPSHOT.jar \
  --server.port=8080 \
  --jwt.secret=your-secret-key
```

## Testing

### Test JWT Authentication
```bash
# Should fail (401 Unauthorized)
curl -X GET http://localhost:8080/api/wallet/balance

# Should succeed with valid token
curl -X GET http://localhost:8080/api/wallet/balance \
  -H "Authorization: Bearer VALID_JWT_TOKEN"
```

### Test Rate Limiting
```bash
# Run 150 requests quickly
for i in {1..150}; do
  curl -X GET http://localhost:8080/actuator/health
done
# Some requests will return 429 Too Many Requests
```

### Test Circuit Breaker
```bash
# Stop a backend service and make requests
# After 5 failures, circuit breaker opens
# Fallback response will be returned
```

## Troubleshooting

### Common Issues

#### 1. 401 Unauthorized
- Check if JWT token is valid
- Verify token is not expired
- Ensure Authorization header format: `Bearer <token>`

#### 2. 503 Service Unavailable
- Check if backend services are running
- Verify Eureka registration
- Check circuit breaker status

#### 3. 429 Too Many Requests
- Wait for rate limit to reset (1 minute)
- Reduce request frequency
- Consider implementing client-side rate limiting

#### 4. Redis Connection Error
- Ensure Redis is running: `redis-cli ping`
- Check Redis host/port configuration
- Verify network connectivity

## Configuration Reference

### application.yml Key Properties
```yaml
server.port: 8080                    # Gateway port
jwt.secret: <secret-key>             # JWT secret key
jwt.expiration: 86400000             # Token expiration (ms)
spring.data.redis.host: localhost    # Redis host
spring.data.redis.port: 6379         # Redis port
eureka.client.service-url.defaultZone: http://localhost:8761/eureka/
```

## Contributing
For issues or improvements, please submit a pull request or create an issue in the repository.

## License
Part of the E-Wallet Payment System - Microservices Architecture
