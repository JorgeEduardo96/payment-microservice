package br.com.paymentservice.messaging.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PaymentProducer(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendPaymentEvent(String topic, Object paymentEvent) {
        try {
            kafkaTemplate.send(topic, objectMapper.writeValueAsString(paymentEvent));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize PaymentEvent", e);
        }
    }
}
