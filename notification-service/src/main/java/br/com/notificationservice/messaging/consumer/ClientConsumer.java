package br.com.notificationservice.messaging.consumer;

import br.com.notificationservice.domain.dto.ClientEventDTO;
import br.com.notificationservice.domain.repository.ClientRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.annotation.Retry;
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
    @Retry(name = "defaultConsumerRetry", fallbackMethod = "fallback")
    public void consume(String message) throws JsonProcessingException {
        try {
            ClientEventDTO clientEvent = objectMapper.readValue(message, ClientEventDTO.class);
            log.info("Received client event: {}", clientEvent.id().toString());
            clientRepository.upsertClient(clientEvent);
        } catch (Exception e) {
            System.err.println("Failed to process message: " + e.getMessage());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    public void fallback(String message, Exception ex) {
        log.error("Fallback enabled - An exception occurred when consuming message: {}", message, ex);
    }
}
