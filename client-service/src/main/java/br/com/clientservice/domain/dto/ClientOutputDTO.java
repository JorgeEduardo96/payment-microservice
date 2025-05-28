package br.com.clientservice.domain.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClientOutputDTO(UUID id, String name, String email, String cpf, LocalDateTime createdAt, LocalDateTime updatedAt) {

}
