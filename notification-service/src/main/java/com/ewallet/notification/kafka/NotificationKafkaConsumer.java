package com.ewallet.notification.kafka;

import com.ewallet.notification.dto.NotificationRequest;
import com.ewallet.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationKafkaConsumer {
    
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(
        topics = "${spring.kafka.topic.payment-notifications}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeNotification(
        @Payload String message,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received message from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            log.debug("Message content: {}", message);
            
            NotificationRequest request = objectMapper.readValue(message, NotificationRequest.class);
            
            // Process based on notification type
            switch (request.getType()) {
                case CUSTOMER_PAYMENT, PAYMENT_FAILED, FEE_COLLECTED -> 
                    notificationService.sendCustomerNotification(request);
                case MERCHANT_PAYMENT -> 
                    notificationService.sendMerchantNotification(request);
                default -> 
                    log.warn("Unknown notification type: {}", request.getType());
            }
            
            acknowledgment.acknowledge();
            log.info("Successfully processed notification for recipient: {}", request.getRecipientId());
            
        } catch (Exception e) {
            log.error("Error processing notification message: {}", message, e);
            // Message will be reprocessed due to manual acknowledgment
        }
    }
}
