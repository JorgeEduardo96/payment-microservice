package br.com.orderservice.messaging.consumer;

import br.com.orderservice.domain.dto.ClientEventDTO;
import br.com.orderservice.domain.repository.ClientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientConsumer {

    private final ObjectMapper objectMapper;
    private final ClientRepository repository;

    @KafkaListener(topics = {"client-created-topic", "client-updated-topic"}, groupId = "order-service-group")
    public void consume(String message) {
        try {
            ClientEventDTO clientEventDTO = objectMapper.readValue(message, ClientEventDTO.class);
            log.info("Received message with client: {}", clientEventDTO.toString());
            repository.upsert(clientEventDTO);
        } catch (Exception e) {
            System.err.println("Failed to parse message: " + e.getMessage());
        }
    }

}
