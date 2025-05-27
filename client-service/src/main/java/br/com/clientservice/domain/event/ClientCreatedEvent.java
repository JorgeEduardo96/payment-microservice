package br.com.clientservice.domain.event;

import br.com.clientservice.domain.dto.ClientOutputDTO;
import lombok.Data;


@Data
public class ClientCreatedEvent  {

    private final ClientOutputDTO client;

}
