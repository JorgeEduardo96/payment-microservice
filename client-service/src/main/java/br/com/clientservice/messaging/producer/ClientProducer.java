package br.com.clientservice.messaging.producer;

import br.com.clientservice.domain.dto.ClientOutputDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ClientProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ClientProducer(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendClientEvent(String topic, ClientOutputDTO client) {
        try {
            kafkaTemplate.send(topic, objectMapper.writeValueAsString(client));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize ClientEventDTO", e);
        }
    }

}
