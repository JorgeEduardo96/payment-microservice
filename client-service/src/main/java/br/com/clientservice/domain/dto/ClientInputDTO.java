package br.com.clientservice.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record ClientInputDTO(@NotEmpty String name, @NotEmpty @Email String email, @NotEmpty String cpf) {

}
