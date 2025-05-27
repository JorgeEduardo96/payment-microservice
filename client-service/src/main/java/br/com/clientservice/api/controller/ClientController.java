package br.com.clientservice.api.controller;

import br.com.clientservice.domain.dto.ClientOutputDTO;
import br.com.clientservice.domain.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService service;

    @GetMapping
    public ResponseEntity<List<ClientOutputDTO>> fetchAllClients() {
        var test = new ClientOutputDTO(UUID.randomUUID(), "Jorge", "jorge@email.com", "123", LocalDate.now(), LocalDate.now());
        return ResponseEntity.ok(List.of(test));
    }

}
