package br.com.orderservice.domain.service;

import br.com.orderservice.domain.dto.OrderInputDTO;
import br.com.orderservice.domain.dto.OrderOutputDTO;
import br.com.orderservice.domain.enumeration.OrderStatus;
import br.com.orderservice.domain.event.OrderCreatedEvent;
import br.com.orderservice.domain.repository.ClientRepository;
import br.com.orderservice.domain.repository.OrderRepository;
import br.com.sharedlib.model.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
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
            throw new EntityNotFoundException("Client", dto.clientId().toString());
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
            throw new EntityNotFoundException("Client", clientId.toString());
        }
        return repository.ordersByClientId(clientId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelOrder(UUID orderId) {
        var order = repository.findOrderById(orderId);

        if (Objects.isNull(order)) {
            throw new EntityNotFoundException("Order", orderId.toString());
        }

        if (OrderStatus.PENDING_PAYMENT.equals(order.status())) {
            repository.processPayment(orderId, OrderStatus.FAILED);
        }
    }

}
