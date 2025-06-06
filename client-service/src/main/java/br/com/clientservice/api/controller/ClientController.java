package br.com.clientservice.api.controller;

import br.com.clientservice.domain.dto.ClientCreateInputDTO;
import br.com.clientservice.domain.dto.ClientOutputDTO;
import br.com.clientservice.domain.dto.ClientUpdateInputDTO;
import br.com.clientservice.domain.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService service;

    @GetMapping("/{id}")
    public ResponseEntity<ClientOutputDTO> fetchClientById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findClient(id));
    }

    @PostMapping
    public ResponseEntity<ClientOutputDTO> createClient(@RequestBody @Valid ClientCreateInputDTO clientCreateInputDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.insert(clientCreateInputDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientOutputDTO> updateClient(@PathVariable UUID id, @RequestBody @Valid ClientUpdateInputDTO clientUpdateInputDTO) {
        ClientOutputDTO updatedClient = service.update(id, clientUpdateInputDTO);
        return ResponseEntity.ok(updatedClient);
    }

}
