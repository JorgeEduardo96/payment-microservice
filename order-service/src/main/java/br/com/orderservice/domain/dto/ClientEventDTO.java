package br.com.orderservice.domain.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClientEventDTO(UUID id, String name, String email, String cpf, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
