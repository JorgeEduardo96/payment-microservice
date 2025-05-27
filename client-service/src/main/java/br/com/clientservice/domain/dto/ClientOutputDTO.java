package br.com.clientservice.domain.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ClientOutputDTO(UUID id, String name, String email, String cpf, LocalDate createdAt, LocalDate updatedAt) {

}
