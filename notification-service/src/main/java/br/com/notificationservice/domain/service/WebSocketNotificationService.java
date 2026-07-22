package br.com.notificationservice.domain.service;

import br.com.notificationservice.domain.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private static final String BROADCAST_DESTINATION = "/topic/notifications";
    private static final String USER_DESTINATION = "/queue/notifications";

    private final SimpMessagingTemplate messagingTemplate;

    public void notify(NotificationMessage message) {
        messagingTemplate.convertAndSend(BROADCAST_DESTINATION, message);
    }

    public void notifyClient(String clientId, NotificationMessage message) {
        messagingTemplate.convertAndSendToUser(clientId, USER_DESTINATION, message);
    }
}
