package br.com.orderservice.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClientEventDTO(UUID id, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
