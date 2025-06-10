package br.com.notificationservice.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClientEventDTO(UUID id, String name, String email, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
