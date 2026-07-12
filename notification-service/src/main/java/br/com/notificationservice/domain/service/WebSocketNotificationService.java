package br.com.notificationservice.domain.service;

import br.com.notificationservice.domain.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private static final String DESTINATION = "/topic/notifications";

    private final SimpMessagingTemplate messagingTemplate;

    public void notify(NotificationMessage message) {
        messagingTemplate.convertAndSend(DESTINATION, message);
    }
}
