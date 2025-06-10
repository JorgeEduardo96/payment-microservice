package br.com.notificationservice.config;

import br.com.notificationservice.domain.service.EmailNotificationService;
import br.com.notificationservice.domain.service.FakeNotificationService;
import br.com.notificationservice.domain.service.PushNotificationService;
import br.com.notificationservice.domain.service.SmsNotificationService;
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
            case SMS -> new SmsNotificationService();
            case PUSH -> new PushNotificationService();
            case FAKE -> new FakeNotificationService();
        };
    }

}
