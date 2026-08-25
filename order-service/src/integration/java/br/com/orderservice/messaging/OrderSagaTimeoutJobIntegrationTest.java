package br.com.orderservice.messaging;

import br.com.orderservice.domain.dto.ClientEventDTO;
import br.com.orderservice.domain.dto.OrderInputDTO;
import br.com.orderservice.domain.enumeration.OrderStatus;
import br.com.orderservice.domain.enumeration.PaymentMethod;
import br.com.orderservice.domain.enumeration.SagaStatus;
import br.com.orderservice.domain.repository.ClientRepository;
import br.com.orderservice.domain.repository.OrderRepository;
import br.com.orderservice.domain.repository.OrderSagaRepository;
import br.com.orderservice.domain.repository.jpa.crudrepository.OrderSagaJpaEntityCrudRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the real @Scheduled wiring (OrderSagaTimeoutJobTest already covers the compensation
 * logic in isolation): with a near-zero timeout and a fast check interval, verifies that a saga
 * left stuck in PAYMENT_REQUESTED is actually picked up and compensated by the running scheduler,
 * closing the "order stuck forever in PENDING_PAYMENT" gap end-to-end.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "order-service.saga.timeout-minutes=0",
        "order-service.saga.check-interval-ms=500"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderSagaTimeoutJobIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private OrderSagaRepository sagaRepository;
    @Autowired
    private OrderSagaJpaEntityCrudRepository sagaCrudRepository;

    @Test
    void shouldCompensateOrderStuckInPaymentRequestedAfterTimeout() throws Exception {
        var clientId = UUID.randomUUID();
        clientRepository.upsert(new ClientEventDTO(clientId, "Jane Doe", LocalDateTime.now(), null));
        var order = orderRepository.createOrder(
                new OrderInputDTO(clientId, new BigDecimal("50.00"), "456 Oak Ave", PaymentMethod.CARD, null));

        // Simulates the gRPC call to payment-service having gone out but the payment-topic
        // confirmation never arriving.
        sagaRepository.create(order.id());
        sagaRepository.markPaymentRequested(order.id());

        var compensated = awaitSagaStatus(order.id(), SagaStatus.COMPENSATED);

        assertThat(compensated).isTrue();
        var saga = sagaCrudRepository.findByOrderId(order.id()).orElseThrow();
        assertThat(saga.getLastError()).contains("Timed out");

        var cancelledOrder = orderRepository.ordersByClientId(clientId).stream()
                .filter(o -> o.id().equals(order.id()))
                .findFirst()
                .orElseThrow();
        assertThat(cancelledOrder.status()).isEqualTo(OrderStatus.FAILED);
    }

    private boolean awaitSagaStatus(UUID orderId, SagaStatus expected) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 10000L;
        while (System.currentTimeMillis() < deadline) {
            var saga = sagaCrudRepository.findByOrderId(orderId).orElse(null);
            if (saga != null && saga.getStatus() == expected) {
                return true;
            }
            Thread.sleep(200L);
        }
        return false;
    }
}
