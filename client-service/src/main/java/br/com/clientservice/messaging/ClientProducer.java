package br.com.clientservice.messaging;

import br.com.clientservice.domain.dto.ClientOutputDTO;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ClientProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ClientProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Retry(name = "defaultProducerRetry", fallbackMethod = "fallback")
    public void sendClientEvent(String topic, ClientOutputDTO client) {
        try {
            kafkaTemplate.send(topic, client).get();
            log.info("Client {} sent successfully to topic {}", client, topic);
        } catch (Exception e) {
            log.error("Failed to process message: {}", e.getMessage());
            throw new IllegalStateException("Failed to send client event to Kafka", e);
        }
    }

    @SuppressWarnings("unused")
    public void fallback(String topic, ClientOutputDTO client, Throwable throwable) {
        log.warn("Fallback enabled - Kafka is unavailable. Client: {}", client);
    }

}
