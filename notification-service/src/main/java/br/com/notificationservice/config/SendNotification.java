package br.com.notificationservice.config;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import java.util.Map;
import java.util.Set;

public interface SendNotification {

    void send(Message message);

    @Builder
    @Getter
    class Message {

        @Singular
        private Set<String> destinations;
        @NonNull
        private String subject;
        @NonNull
        private String body;

        @Singular("variable")
        private Map<String, String> variables;
    }

}
