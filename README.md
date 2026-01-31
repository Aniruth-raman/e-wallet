# e-wallet

A multi-module Maven project for an E-Wallet system.

## Project Structure

This is a parent Maven project with three child services:

- **wallet-service**: Handles wallet operations
- **payment-service**: Manages payment processing
- **notification-service**: Handles notifications

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

After building, you can run each service:

```bash
# Wallet Service
java -cp wallet-service/target/wallet-service-1.0.0-SNAPSHOT.jar com.ewallet.wallet.WalletService

# Payment Service
java -cp payment-service/target/payment-service-1.0.0-SNAPSHOT.jar com.ewallet.payment.PaymentService

# Notification Service
java -cp notification-service/target/notification-service-1.0.0-SNAPSHOT.jar com.ewallet.notification.NotificationService
```