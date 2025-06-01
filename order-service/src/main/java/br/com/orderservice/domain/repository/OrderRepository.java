package br.com.orderservice.domain.repository;

import br.com.orderservice.domain.dto.OrderInputDTO;

public interface OrderRepository {

    void createOrder(OrderInputDTO dto);

}
