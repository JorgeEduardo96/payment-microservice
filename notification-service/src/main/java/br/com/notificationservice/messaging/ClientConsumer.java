package br.com.notificationservice.messaging;

import br.com.notificationservice.domain.dto.ClientEventDTO;
import br.com.notificationservice.domain.dto.NotificationMessage;
import br.com.notificationservice.domain.repository.ClientRepository;
import br.com.notificationservice.domain.service.WebSocketNotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientConsumer {

    private static final String CLIENT_CREATED_TOPIC = "client-created-topic";
    public static final String CLIENT_UPDATED_TOPIC = "client-updated-topic";

    private final ObjectMapper objectMapper;
    private final ClientRepository clientRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    @KafkaListener(topics = {CLIENT_CREATED_TOPIC, CLIENT_UPDATED_TOPIC}, groupId = "notification-service-group")
    public void consume(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) throws JsonProcessingException {
        try {
            ClientEventDTO clientEvent = objectMapper.readValue(message, ClientEventDTO.class);
            log.info("Received client event: {}", clientEvent.id().toString());
            clientRepository.upsert(clientEvent);

            if (CLIENT_CREATED_TOPIC.equals(topic)) {
                webSocketNotificationService.notify(NotificationMessage.of(
                        "CLIENT_CREATED",
                        "Novo cliente cadastrado",
                        clientEvent.name()));
            }
        } catch (Exception e) {
            System.err.println("Failed to process message: " + e.getMessage());
            throw e;
        }
    }
}
