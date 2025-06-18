package br.com.paymentservice.grpc;

import br.com.orderservice.grpc.client.stub.PaymentRequest;
import br.com.paymentservice.domain.dto.PaymentResponseDTO;
import br.com.paymentservice.messaging.producer.PaymentProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentProducer paymentProducer;

    @InjectMocks
    private PaymentServiceImpl underTest;

    @Test
    void processPayment() throws Exception {
        var paymentRequest = mock(PaymentRequest.class);
        var orderId = UUID.randomUUID().toString();
        orderId = orderId.substring(0, orderId.length() - 1) + "A";
        var clientId = UUID.randomUUID();


        when(paymentRequest.getOrderId()).thenReturn(orderId);
        when(paymentRequest.getPaymentMethod()).thenReturn("CARD");
        when(paymentRequest.getClientId()).thenReturn(clientId.toString());

        underTest.processPayment(paymentRequest, mock(io.grpc.stub.StreamObserver.class));

        ArgumentCaptor<PaymentResponseDTO> captor = ArgumentCaptor.forClass(PaymentResponseDTO.class);

        verify(paymentProducer).sendPaymentEvent(eq("payment-topic"), captor.capture());

        PaymentResponseDTO capturedPaymentResponse = captor.getValue();
        assertThat(capturedPaymentResponse.orderId().equals(UUID.fromString(orderId))).isTrue();
        assertThat(capturedPaymentResponse.status()).isEqualTo("PAID");
        assertThat(capturedPaymentResponse.paymentMethod()).isEqualTo("CARD");
        assertThat(capturedPaymentResponse.clientId()).isEqualTo(clientId);
    }

}
