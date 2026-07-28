package br.com.clientservice.domain.dto;

import jakarta.validation.constraints.Email;

public record ClientUpdateInputDTO(String name, @Email String email) {

}
