package br.com.clientservice.domain.event;

import br.com.clientservice.domain.dto.ClientOutputDTO;

public record ClientUpdatedEvent(ClientOutputDTO client) {

}
