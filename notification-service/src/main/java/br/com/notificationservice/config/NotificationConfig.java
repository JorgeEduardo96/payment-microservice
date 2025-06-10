package br.com.notificationservice.config;

import br.com.notificationservice.domain.service.EmailNotificationService;
import br.com.notificationservice.domain.service.FakeNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class NotificationConfig {

    private final NotificationProperties notificationProperties;

    @Bean
    public SendNotification sendNotification() {
        return switch (notificationProperties.getNotificationType()) {
            case EMAIL -> new EmailNotificationService();
            case FAKE -> new FakeNotificationService();
        };
    }

}
