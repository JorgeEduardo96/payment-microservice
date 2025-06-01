package br.com.orderservice.domain.repository.jpa;

import br.com.orderservice.domain.dto.OrderInputDTO;
import br.com.orderservice.domain.repository.OrderRepository;
import br.com.orderservice.domain.repository.jpa.crudrepository.OrderJpaEntityCrudRepository;
import br.com.orderservice.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderMapper mapper;
    private final OrderJpaEntityCrudRepository repository;

    @Override
    public void createOrder(OrderInputDTO dto) {
        repository.save(mapper.toEntity(dto));
    }
}
