package br.com.notificationservice.domain.dto;

import java.time.Instant;

public record NotificationMessage(String type, String title, String message, Instant timestamp) {

    public static NotificationMessage of(String type, String title, String message) {
        return new NotificationMessage(type, title, message, Instant.now());
    }
}
