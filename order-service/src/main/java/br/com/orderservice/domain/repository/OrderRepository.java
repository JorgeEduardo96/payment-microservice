package br.com.orderservice.domain.repository;

import br.com.orderservice.domain.dto.OrderInputDTO;
import br.com.orderservice.domain.dto.OrderOutputDTO;
import br.com.orderservice.domain.enumeration.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderRepository {

    OrderOutputDTO createOrder(OrderInputDTO dto);

    void processPayment(UUID orderId, OrderStatus status);

    List<OrderOutputDTO> ordersByClientId(UUID clientId);

}
