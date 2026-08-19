package br.com.paymentservice.domain.repository.jpa;

import br.com.paymentservice.domain.dto.PaymentCreateInputDTO;
import br.com.paymentservice.domain.dto.PaymentResponseDTO;
import br.com.paymentservice.domain.entity.PaymentJpaEntity;
import br.com.paymentservice.domain.repository.PaymentRepository;
import br.com.paymentservice.domain.repository.jpa.crudrepository.PaymentJpaEntityCrudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaEntityCrudRepository repository;

    @Override
    public PaymentResponseDTO insert(PaymentCreateInputDTO inputDTO) {
        var entity = new PaymentJpaEntity();
        entity.setOrderId(inputDTO.orderId());
        entity.setClientId(inputDTO.clientId());
        entity.setPaymentMethod(inputDTO.paymentMethod());
        entity.setStatus(inputDTO.status());
        return toDTO(repository.save(entity));
    }

    @Override
    public PaymentResponseDTO findByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId).map(this::toDTO).orElse(null);
    }

    private PaymentResponseDTO toDTO(PaymentJpaEntity entity) {
        return new PaymentResponseDTO(entity.getOrderId(), entity.getStatus(), entity.getPaymentMethod(), entity.getClientId());
    }
}
