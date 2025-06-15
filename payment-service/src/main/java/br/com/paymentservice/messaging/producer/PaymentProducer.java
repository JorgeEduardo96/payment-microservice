package br.com.paymentservice.messaging.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentProducer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PaymentProducer(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Retry(name = "defaultProducerRetry", fallbackMethod = "fallback")
    public void sendPaymentEvent(String topic, Object paymentEvent) throws Exception {
        try {
            kafkaTemplate.send(topic, objectMapper.writeValueAsString(paymentEvent));
        } catch (Exception e) {
            logger.error("Failed to process message: {}", e.getMessage());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    public void fallback(String topic, Object payment, Throwable throwable) {
        logger.warn("Fallback enabled - Kafka is unavailable. Payment: {}", payment);
    }
}
