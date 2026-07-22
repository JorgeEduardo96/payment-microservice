package br.com.notificationservice.domain.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationMessage(String type, String title, String message, Instant timestamp,
                                   UUID orderId, String orderStatus) {

    public static NotificationMessage of(String type, String title, String message) {
        return new NotificationMessage(type, title, message, Instant.now(), null, null);
    }

    public static NotificationMessage ofPayment(String type, String title, String message, UUID orderId, String orderStatus) {
        return new NotificationMessage(type, title, message, Instant.now(), orderId, orderStatus);
    }
}
