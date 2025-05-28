package br.com.clientservice.domain.repository;

import br.com.clientservice.domain.dto.ClientCreateInputDTO;
import br.com.clientservice.domain.dto.ClientOutputDTO;
import br.com.clientservice.domain.dto.ClientUpdateInputDTO;

import java.util.UUID;

public interface ClientRepository {

    ClientOutputDTO insert(ClientCreateInputDTO inputDTO);
    ClientOutputDTO update(UUID id, ClientUpdateInputDTO inputDTO);
    ClientOutputDTO findById(UUID id);
    ClientOutputDTO findByEmail(String email);
    ClientOutputDTO findByCpf(String cpf);

}
