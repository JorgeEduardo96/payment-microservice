package br.com.orderservice.messaging.consumer;

import br.com.orderservice.domain.dto.PaymentResponseEventDTO;
import br.com.orderservice.domain.enumeration.OrderStatus;
import br.com.orderservice.domain.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {

    private final ObjectMapper objectMapper;
    private final OrderRepository repository;

    @KafkaListener(topics = {"payment-topic"}, groupId = "order-service-group")
    @Retry(name = "defaultConsumerRetry", fallbackMethod = "fallback")
    public void consume(String message) throws JsonProcessingException {
        try {
            PaymentResponseEventDTO paymentResponseEventDTO = objectMapper.readValue(message, PaymentResponseEventDTO.class);
            log.info("Received payment event from order: {}", paymentResponseEventDTO.orderId().toString());
            repository.processPayment(paymentResponseEventDTO.orderId(), OrderStatus.valueOf(paymentResponseEventDTO.status().toUpperCase()));
        } catch (Exception e) {
            System.err.println("Failed to process message: " + e.getMessage());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    public void fallback(String message, Exception ex) {
        log.error("Fallback enabled - An exception occurred when consuming message: {}", message, ex);
    }

}
