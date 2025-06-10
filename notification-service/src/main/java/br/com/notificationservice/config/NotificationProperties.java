package br.com.notificationservice.config;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
@Setter
@Component
@ConfigurationProperties("notification-service.notification")
public class NotificationProperties {

    @NonNull
    private String host;

    private NotificationType notificationType = NotificationType.FAKE;

    private String destinationSandbox;


    public enum NotificationType {
        EMAIL, FAKE
    }
}
