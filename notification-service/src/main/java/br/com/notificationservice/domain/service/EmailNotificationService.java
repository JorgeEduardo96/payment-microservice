package br.com.notificationservice.domain.service;

import br.com.notificationservice.config.NotificationProperties;
import br.com.notificationservice.config.SendNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;

public class EmailNotificationService implements SendNotification {

    @Autowired
    private NotificationProperties notificationProperties;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void send(Message message) {

    }

}
