package br.com.clientservice.messaging.producer;

import br.com.clientservice.domain.dto.ClientOutputDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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

    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "fallbackSend")
    public void sendClientEvent(String topic, ClientOutputDTO client) throws Exception {
        String payload = objectMapper.writeValueAsString(client);
        kafkaTemplate.send(topic, payload).get();
        log.info("Client {} sent successfully to topic {}", client, topic);
    }

    @SuppressWarnings("unused")
    public void fallbackSend(String topic, ClientOutputDTO client, Throwable throwable) {
        log.warn("Fallback enabled - Kafka is unavailable. Client: {}", client);
    }

}
