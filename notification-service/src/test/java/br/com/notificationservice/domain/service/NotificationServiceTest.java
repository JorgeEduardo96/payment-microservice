package br.com.notificationservice.domain.service;

import br.com.notificationservice.config.SendNotification;
import br.com.notificationservice.domain.dto.ClientEventDTO;
import br.com.notificationservice.domain.dto.NotificationMessage;
import br.com.notificationservice.domain.dto.PaymentResponseEventDTO;
import br.com.notificationservice.domain.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private SendNotification sendNotification;
    @Mock
    private WebSocketNotificationService webSocketNotificationService;

    @InjectMocks
    private NotificationService underTest;

    @Test
    void sendNotification_shouldSendEmailAndWebSocketNotificationWhenPaymentIsPaid() {
        var mockedEventResponse = mock(PaymentResponseEventDTO.class);
        var mockedClient = mock(ClientEventDTO.class);
        var orderId = UUID.randomUUID();
        var clientId = UUID.randomUUID();

        when(mockedEventResponse.status()).thenReturn("PAID");
        when(mockedEventResponse.clientId()).thenReturn(clientId);
        when(mockedEventResponse.orderId()).thenReturn(orderId);
        when(mockedEventResponse.paymentMethod()).thenReturn("CARD");

        when(mockedClient.name()).thenReturn("John Doe");
        when(mockedClient.email()).thenReturn("john.doe@email.com");

        when(clientRepository.findById(clientId)).thenReturn(mockedClient);

        underTest.sendNotification(mockedEventResponse);

        verify(clientRepository).findById(clientId);
        verify(sendNotification).send(any(SendNotification.Message.class));
        verify(webSocketNotificationService).notifyClient(eq(clientId.toString()), any(NotificationMessage.class));
    }

    @Test
    void sendNotification_shouldOnlySendWebSocketNotificationWhenPaymentFails() {
        var mockedEventResponse = mock(PaymentResponseEventDTO.class);
        var orderId = UUID.randomUUID();
        var clientId = UUID.randomUUID();

        when(mockedEventResponse.status()).thenReturn("FAILED");
        when(mockedEventResponse.clientId()).thenReturn(clientId);
        when(mockedEventResponse.orderId()).thenReturn(orderId);

        underTest.sendNotification(mockedEventResponse);

        verify(clientRepository, never()).findById(any(UUID.class));
        verify(sendNotification, never()).send(any(SendNotification.Message.class));
        verify(webSocketNotificationService).notifyClient(eq(clientId.toString()), any(NotificationMessage.class));
    }
}
