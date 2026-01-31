# e-wallet

A multi-module Spring Boot Maven project for an E-Wallet system.

## Project Structure

This is a parent Maven project with three independent Spring Boot microservices:

- **wallet-service**: Handles wallet operations (Port: 8081)
- **payment-service**: Manages payment processing (Port: 8082)
- **notification-service**: Handles notifications (Port: 8083)

Each service is an independent Spring Boot application with:
- Spring Boot Web starter for REST APIs
- Spring Boot Actuator for health monitoring
- Ability to add additional dependencies as needed

## Building the Project

To build all modules:
```bash
mvn clean install
```

To build a specific module:
```bash
cd <module-name>
mvn clean install
```

## Running Services

Each service can be run independently using Spring Boot Maven plugin:

```bash
# Wallet Service (runs on port 8081)
cd wallet-service
mvn spring-boot:run

# Payment Service (runs on port 8082)
cd payment-service
mvn spring-boot:run

# Notification Service (runs on port 8083)
cd notification-service
mvn spring-boot:run
```

Or run the packaged JAR:

```bash
# Wallet Service
java -jar wallet-service/target/wallet-service-1.0.0-SNAPSHOT.jar

# Payment Service
java -jar payment-service/target/payment-service-1.0.0-SNAPSHOT.jar

# Notification Service
java -jar notification-service/target/notification-service-1.0.0-SNAPSHOT.jar
```

## Health Check

Each service has actuator endpoints for health monitoring:

- Wallet Service: http://localhost:8081/actuator/health
- Payment Service: http://localhost:8082/actuator/health
- Notification Service: http://localhost:8083/actuator/health

## Adding Dependencies

Each service is independent, so you can add service-specific dependencies directly to the respective service's `pom.xml` file. The parent POM manages Spring Boot dependencies, allowing each service to use them without version specifications.