package br.com.paymentservice.messaging;

import br.com.paymentservice.domain.dto.PaymentResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentProducer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void sendPaymentEvent(String topic, PaymentResponseDTO paymentEvent) throws Exception {
        try {
            kafkaTemplate.send(topic, paymentEvent).get();
            logger.info("Payment {} sent successfully to topic: {}", paymentEvent, topic);
        } catch (Exception e) {
            logger.error("Failed to process message: {}", e.getMessage());
            throw e;
        }
    }
}
