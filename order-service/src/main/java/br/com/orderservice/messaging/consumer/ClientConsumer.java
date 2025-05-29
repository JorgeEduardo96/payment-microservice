package br.com.orderservice.messaging.consumer;

import br.com.orderservice.domain.dto.ClientEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "client-created-topic", groupId = "order-service-group")
    public void consume(String message) {
        try {
            ClientEventDTO createdClient = objectMapper.readValue(message, ClientEventDTO.class);
            System.out.println(createdClient.toString());
        } catch (Exception e) {
            System.err.println("Failed to parse message: " + e.getMessage());
        }
    }

}
