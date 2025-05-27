package br.com.clientservice.domain.repository;

import br.com.clientservice.domain.dto.ClientInputDTO;
import br.com.clientservice.domain.dto.ClientOutputDTO;

import java.util.UUID;

public interface ClientRepository {

    ClientOutputDTO insert(ClientInputDTO inputDTO);
    ClientOutputDTO findById(UUID id);

}
