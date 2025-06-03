package br.com.orderservice.domain.event;

import br.com.orderservice.domain.dto.OrderOutputDTO;

public record OrderCreatedEvent(OrderOutputDTO orderOutputDTO) {
}
