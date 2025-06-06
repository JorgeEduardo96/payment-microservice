package br.com.orderservice.api.controller;

import br.com.orderservice.domain.dto.OrderInputDTO;
import br.com.orderservice.domain.dto.OrderOutputDTO;
import br.com.orderservice.domain.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    public ResponseEntity<OrderOutputDTO> createOrder(@RequestBody @Valid OrderInputDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createOrder(dto));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<OrderOutputDTO>> getOrdersByClient(@PathVariable UUID clientId) {
        return ResponseEntity.ok(service.getOrdersByClientId(clientId));
    }

}
