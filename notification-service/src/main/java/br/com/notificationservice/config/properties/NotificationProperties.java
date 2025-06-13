package br.com.notificationservice.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("notification-service.notification")
public class NotificationProperties {

    private NotificationType notificationType = NotificationType.FAKE;

    public enum NotificationType {
        EMAIL, FAKE
    }
}
