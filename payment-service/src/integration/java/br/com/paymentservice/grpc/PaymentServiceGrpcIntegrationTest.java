package br.com.paymentservice.grpc;

import br.com.orderservice.grpc.client.stub.PaymentRequest;
import br.com.orderservice.grpc.client.stub.PaymentServiceGrpc;
import br.com.paymentservice.domain.dto.PaymentResponseDTO;
import br.com.paymentservice.messaging.PaymentProducer;
import com.google.protobuf.Empty;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class PaymentServiceGrpcIntegrationTest {

    @GrpcClient("local-grpc-server")
    private PaymentServiceGrpc.PaymentServiceBlockingStub paymentServiceStub;

    @MockitoBean
    private PaymentProducer paymentProducer;

    @Test
    void shouldSendKafkaEventAndReturnEmpty() throws Exception {
        String orderId = UUID.randomUUID().toString().replaceFirst("\\d$", "a");
        UUID clientId = UUID.randomUUID();

        PaymentRequest request = PaymentRequest.newBuilder()
                .setOrderId(orderId)
                .setClientId(clientId.toString())
                .setPaymentMethod("CARD")
                .build();

        Empty response = paymentServiceStub.processPayment(request);

        assertThat(response).isNotNull();

        ArgumentCaptor<PaymentResponseDTO> captor = ArgumentCaptor.forClass(PaymentResponseDTO.class);
        verify(paymentProducer).sendPaymentEvent(eq("payment-topic"), captor.capture());

        PaymentResponseDTO sentEvent = captor.getValue();
        assertThat(sentEvent.status()).isEqualTo("PAID");
        assertThat(sentEvent.paymentMethod()).isEqualTo("CARD");
        assertThat(sentEvent.orderId().toString()).startsWith(orderId.substring(0, 8));
    }

    @Test
    void shouldSetStatusFailed_whenLastCharIsNotLetter() throws Exception {
        var orderId = UUID.randomUUID().toString().replaceFirst("[A-Za-z]$", "1");
        UUID clientId = UUID.randomUUID();

        PaymentRequest request = PaymentRequest.newBuilder()
                .setOrderId(orderId)
                .setClientId(clientId.toString())
                .setPaymentMethod("CASH")
                .build();

        Empty response = paymentServiceStub.processPayment(request);

        assertThat(response).isNotNull();

        ArgumentCaptor<PaymentResponseDTO> captor = ArgumentCaptor.forClass(PaymentResponseDTO.class);
        verify(paymentProducer).sendPaymentEvent(eq("payment-topic"), captor.capture());

        PaymentResponseDTO sentEvent = captor.getValue();
        assertThat(sentEvent.status()).isEqualTo("FAILED");
    }
}
