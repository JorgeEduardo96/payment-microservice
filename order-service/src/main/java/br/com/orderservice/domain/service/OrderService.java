package br.com.orderservice.domain.service;

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
        var optionalClient = clientRepository.findById(dto.clientId());
        if (optionalClient.isEmpty()) {
            throw new EntityNotFoundException("Client", dto.clientId());
        }
        var outputDTO = repository.createOrder(dto);
        var client = optionalClient.get();

        var response = new OrderOutputDTO(outputDTO.id(), outputDTO.total(), outputDTO.shippingAddress(),
                client.id(), client.name(), outputDTO.status(), outputDTO.paymentMethod());
        publisher.publishEvent(new OrderCreatedEvent(response));
        return response;
    }

    public List<OrderOutputDTO> getOrdersByClientId(UUID clientId) {
        var client = clientRepository.findById(clientId);
        if (client.isEmpty()) {
            throw new EntityNotFoundException("Client", clientId);
        }
        return repository.ordersByClientId(clientId);
    }

}
