package br.com.notificationservice.messaging;

import br.com.notificationservice.domain.dto.ClientEventDTO;
import br.com.notificationservice.domain.repository.ClientRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
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
    private final ClientRepository clientRepository;

    @KafkaListener(topics = {"client-created-topic", "client-updated-topic"}, groupId = "notification-service-group")
    public void consume(String message) throws JsonProcessingException {
        try {
            ClientEventDTO clientEvent = objectMapper.readValue(message, ClientEventDTO.class);
            log.info("Received client event: {}", clientEvent.id().toString());
            clientRepository.upsert(clientEvent);
        } catch (Exception e) {
            System.err.println("Failed to process message: " + e.getMessage());
            throw e;
        }
    }
}
