package br.com.orderservice.domain.repository.jpa;

import br.com.orderservice.domain.dto.OrderInputDTO;
import br.com.orderservice.domain.dto.OrderOutputDTO;
import br.com.orderservice.domain.entity.OrderJpaEntity;
import br.com.orderservice.domain.enumeration.OrderStatus;
import br.com.orderservice.domain.repository.OrderRepository;
import br.com.orderservice.domain.repository.jpa.crudrepository.OrderJpaEntityCrudRepository;
import br.com.orderservice.mapper.OrderMapper;
import br.com.sharedlib.model.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderMapper mapper;
    private final OrderJpaEntityCrudRepository repository;

    @Override
    public OrderOutputDTO findOrderById(UUID orderId) {
        return repository.findById(orderId).map(mapper::toDto).orElse(null);
    }

    @Override
    public OrderOutputDTO createOrder(OrderInputDTO dto) {
        OrderJpaEntity entity = mapper.toEntity(dto);
        entity.applyDiscount();
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public void processPayment(UUID orderId, OrderStatus status) {
        var entity = repository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId.toString()));
        entity.setStatus(status);
        repository.save(entity);
    }

    @Override
    public List<OrderOutputDTO> ordersByClientId(UUID clientId) {
        return repository.findByClientId(clientId).stream()
                .map(order -> OrderOutputDTO.builder()
                        .id(order.getId())
                        .total(order.getTotal())
                        .shippingAddress(order.getShippingAddress())
                        .clientName(order.getClient().getName())
                        .status(order.getStatus())
                        .paymentMethod(order.getPaymentMethod())
                        .build()
                )
                .collect(Collectors.toList());
    }
}
