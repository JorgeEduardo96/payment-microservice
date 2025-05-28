package br.com.clientservice.domain.dto;

import br.com.clientservice.domain.validation.UniqueEmail;
import jakarta.validation.constraints.Email;

public record ClientUpdateInputDTO(String name, @UniqueEmail @Email String email) {

}
