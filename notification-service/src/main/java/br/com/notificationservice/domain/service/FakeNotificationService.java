package br.com.notificationservice.domain.service;

import br.com.notificationservice.config.SendNotification;
import br.com.notificationservice.domain.service.email.EmailProcessorTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class FakeNotificationService implements SendNotification {

    @Autowired
    private EmailProcessorTemplate template;

    @Override
    public void send(Message message) {
        var body = template.processTemplate(message);
        log.info("[FAKE NOTIFICATION] To: {}\n{}", message.getDestinations(), body);
    }
}
