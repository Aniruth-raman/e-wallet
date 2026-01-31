# Adding Dependencies to Services

This document demonstrates how each service is independent and can have its own dependencies added.

## Example: Adding a Database Dependency to Wallet Service

To add JPA and H2 database to wallet-service only:

```xml
<!-- Add to wallet-service/pom.xml under <dependencies> -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

## Example: Adding Messaging to Notification Service

To add Kafka to notification-service only:

```xml
<!-- Add to notification-service/pom.xml under <dependencies> -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

## Example: Adding Security to Payment Service

To add Spring Security to payment-service only:

```xml
<!-- Add to payment-service/pom.xml under <dependencies> -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

## Benefits

- Each service has its own `pom.xml` where dependencies can be added
- Parent POM manages Spring Boot dependency versions via `spring-boot-starter-parent`
- No version conflicts between services
- Services can be built and deployed independently
- Each service runs on its own port (8081, 8082, 8083)
