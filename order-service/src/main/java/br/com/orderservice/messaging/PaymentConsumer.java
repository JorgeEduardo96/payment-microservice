package br.com.orderservice.messaging;

import br.com.orderservice.domain.dto.PaymentResponseEventDTO;
import br.com.orderservice.domain.enumeration.OrderStatus;
import br.com.orderservice.domain.repository.OrderRepository;
import br.com.orderservice.domain.repository.OrderSagaRepository;
import br.com.orderservice.domain.service.OrderMetrics;
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
    private final OrderRepository repository;
    private final OrderSagaRepository sagaRepository;
    private final OrderMetrics orderMetrics;

    @KafkaListener(topics = {"payment-topic"}, groupId = "order-service-group")
    public void consume(String message) throws JsonProcessingException {
        try {
            PaymentResponseEventDTO paymentResponseEventDTO = objectMapper.readValue(message, PaymentResponseEventDTO.class);
            log.info("Received payment event from order: {}", paymentResponseEventDTO.orderId().toString());
            OrderStatus status = OrderStatus.valueOf(paymentResponseEventDTO.status().toUpperCase());
            repository.processPayment(paymentResponseEventDTO.orderId(), status);
            orderMetrics.incrementStatus(status);

            if (status == OrderStatus.PAID) {
                sagaRepository.markCompleted(paymentResponseEventDTO.orderId());
            } else {
                sagaRepository.markCompensated(paymentResponseEventDTO.orderId(),
                        "Payment declined by payment-service (status=" + status + ")");
            }
        } catch (Exception e) {
            System.err.println("Failed to process message: " + e.getMessage());
            throw e;
        }
    }
}
