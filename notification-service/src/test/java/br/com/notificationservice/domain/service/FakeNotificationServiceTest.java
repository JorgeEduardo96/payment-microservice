package br.com.notificationservice.domain.service;

import br.com.notificationservice.config.SendNotification;
import br.com.notificationservice.domain.service.email.EmailProcessorTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class FakeNotificationServiceTest {

    @Mock
    private EmailProcessorTemplate template;

    @InjectMocks
    private FakeNotificationService underTest;

    @Test
    void send() {
        var mockedMessage = mock(SendNotification.Message.class);

        when(template.processTemplate(mockedMessage)).thenReturn("fake notification body");
        when(mockedMessage.getDestinations()).thenReturn(Set.of("john.doe@email.com"));

        underTest.send(mockedMessage);

        verify(template).processTemplate(mockedMessage);
    }

}
