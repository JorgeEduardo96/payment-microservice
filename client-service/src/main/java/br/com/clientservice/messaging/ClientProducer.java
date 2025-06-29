package br.com.clientservice.messaging;

import br.com.clientservice.domain.dto.ClientOutputDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ClientProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ClientProducer(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Retry(name = "defaultProducerRetry", fallbackMethod = "fallback")
    public void sendClientEvent(String topic, ClientOutputDTO client) throws Exception {
        try {
            String payload = objectMapper.writeValueAsString(client);
            kafkaTemplate.send(topic, payload).get();
            log.info("Client {} sent successfully to topic {}", client, topic);
        } catch (Exception e) {
            log.error("Failed to process message: {}", e.getMessage());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    public void fallback(String topic, ClientOutputDTO client, Throwable throwable) {
        log.warn("Fallback enabled - Kafka is unavailable. Client: {}", client);
    }

}
