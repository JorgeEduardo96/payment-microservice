package br.com.orderservice.domain.service;

import br.com.orderservice.domain.dto.OrderInputDTO;
import br.com.orderservice.domain.dto.OrderOutputDTO;
import br.com.orderservice.domain.exception.ClientNotFoundException;
import br.com.orderservice.domain.repository.ClientRepository;
import br.com.orderservice.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final ClientRepository clientRepository;

    @Transactional
    public OrderOutputDTO createOrder(OrderInputDTO dto) {
        var client = clientRepository.findById(dto.clientId());
        if (client.isEmpty()) {
            throw new ClientNotFoundException(dto.clientId());
        }
        repository.createOrder(dto);
        // chamar o outro serviço para confirmar o pagamento

        return null;
    }

}
