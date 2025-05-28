package br.com.clientservice.domain.dto;

import br.com.clientservice.domain.validation.Cpf;
import br.com.clientservice.domain.validation.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record ClientCreateInputDTO(@NotEmpty String name, @UniqueEmail @NotEmpty @Email String email, @NotEmpty @Cpf String cpf) {

}
