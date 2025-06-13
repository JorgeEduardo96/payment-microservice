package br.com.notificationservice.config;

import br.com.notificationservice.config.properties.NotificationProperties;
import br.com.notificationservice.config.properties.SendGridConfigurationProperties;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SendGridConfigurationProperties.class)
@RequiredArgsConstructor
public class SendGridConfiguration {

    private final NotificationProperties notificationProperties;
    private final SendGridConfigurationProperties sendGridConfigurationProperties;

    @Bean
    public SendGrid sendGrid() {
        var apiKey = sendGridConfigurationProperties.getApiKey();
        if ((apiKey == null || apiKey.isBlank()) && notificationProperties.getNotificationType() == NotificationProperties.NotificationType.EMAIL) {
            throw new IllegalArgumentException("SendGrid API key must not be blank");
        }
        return new SendGrid(apiKey);
    }

    @Bean
    public Email fromEmail() {
        String fromEmail = sendGridConfigurationProperties.getFromEmail();
        String fromName = sendGridConfigurationProperties.getFromName();
        return new Email(fromEmail, fromName);
    }

}
