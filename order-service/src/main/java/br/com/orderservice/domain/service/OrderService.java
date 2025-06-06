package br.com.orderservice.domain.service;

import br.com.orderservice.domain.dto.ClientEventDTO;
import br.com.orderservice.domain.dto.OrderInputDTO;
import br.com.orderservice.domain.dto.OrderOutputDTO;
import br.com.orderservice.domain.event.OrderCreatedEvent;
import br.com.orderservice.domain.exception.EntityNotFoundException;
import br.com.orderservice.domain.repository.ClientRepository;
import br.com.orderservice.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final ClientRepository clientRepository;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public OrderOutputDTO createOrder(OrderInputDTO dto) {
        var client = clientRepository.findById(dto.clientId());
        if (client.isEmpty()) {
            throw new EntityNotFoundException("Client", dto.clientId());
        }
        var outputDTO = repository.createOrder(dto);
        OrderOutputDTO response = buildOutputDTO(outputDTO, client.get());
        publisher.publishEvent(new OrderCreatedEvent(outputDTO));
        return response;
    }

    private OrderOutputDTO buildOutputDTO(OrderOutputDTO dto, ClientEventDTO clientEventDTO) {
        return OrderOutputDTO.builder()
                .id(dto.id())
                .clientName(clientEventDTO.name())
                .total(dto.total())
                .shippingAddress(dto.shippingAddress())
                .paymentMethod(dto.paymentMethod())
                .status(dto.status())
                .build();
    }

    public List<OrderOutputDTO> getOrdersByClientId(UUID clientId) {
        var client = clientRepository.findById(clientId);
        if (client.isEmpty()) {
            throw new EntityNotFoundException("Client", clientId);
        }
        return repository.ordersByClientId(clientId);
    }

}
