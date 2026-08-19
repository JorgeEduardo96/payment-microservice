package br.com.paymentservice.domain.repository;

import br.com.paymentservice.domain.dto.PaymentCreateInputDTO;
import br.com.paymentservice.domain.dto.PaymentResponseDTO;

import java.util.UUID;

public interface PaymentRepository {

    PaymentResponseDTO insert(PaymentCreateInputDTO inputDTO);

    /**
     * Returns {@code null} when no payment exists for the given orderId yet (mirrors the
     * findByEmail/findByCpf convention used in client-service's ClientRepository), so callers
     * can use it directly as an idempotency check before deciding/persisting a new payment.
     */
    PaymentResponseDTO findByOrderId(UUID orderId);
}
