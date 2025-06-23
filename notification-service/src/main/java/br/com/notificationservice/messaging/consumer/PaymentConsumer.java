package br.com.notificationservice.messaging.consumer;

import br.com.notificationservice.domain.dto.PaymentResponseEventDTO;
import br.com.notificationservice.domain.service.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @KafkaListener(topics = "payment-topic", groupId = "notification-service-group")
    public void consume(String message) throws JsonProcessingException {
        try {
            PaymentResponseEventDTO paymentResponseEventDTO = objectMapper.readValue(message, PaymentResponseEventDTO.class);
            log.info("Received payment event from order: {}", paymentResponseEventDTO.orderId().toString());
            notificationService.sendNotification(paymentResponseEventDTO);
        } catch (Exception e) {
            log.error("Failed to process message: {}", e.getMessage());
            throw e;
        }
    }
}
