package br.com.notificationservice.domain.service;

import br.com.notificationservice.config.SendNotification;
import br.com.notificationservice.domain.exception.EmailException;
import br.com.notificationservice.domain.service.email.EmailProcessorTemplate;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.objects.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private EmailProcessorTemplate template;
    @Mock
    private Email fromEmail;
    @Mock
    private SendGrid sendGrid;

    @InjectMocks
    private EmailNotificationService underTest;

    @Test
    void send_successfully() throws Exception {
        var message = mock(SendNotification.Message.class);
        var htmlBody = "<html>Testing</html>";
        var response = mock(Response.class);

        when(template.processTemplate(message)).thenReturn(htmlBody);
        when(message.getSubject()).thenReturn("Testing Email");
        when(message.getDestinations()).thenReturn(Set.of("teste@example.com"));
        when(response.getStatusCode()).thenReturn(202);
        when(sendGrid.api(any(Request.class))).thenReturn(response);

        underTest.send(message);

        verify(template).processTemplate(message);
        verify(sendGrid).api(any(Request.class));
    }

    @Test
    void send_shouldThrowEmailExceptionWhenSendGridReturnsError() throws Exception {
        var message = mock(SendNotification.Message.class);
        var htmlBody = "<html>Testing</html>";
        var response = mock(Response.class);

        when(template.processTemplate(message)).thenReturn(htmlBody);
        when(message.getSubject()).thenReturn("Testing Email");
        when(message.getDestinations()).thenReturn(Set.of("teste@example.com"));
        when(template.processTemplate(message)).thenReturn(htmlBody);
        when(sendGrid.api(any(Request.class))).thenReturn(response);
        when(response.getStatusCode()).thenReturn(400);

        assertThrows(EmailException.class, () -> underTest.send(message));

        verify(template).processTemplate(message);
        verify(sendGrid).api(any(Request.class));
    }

    @Test
    void send_shouldThrowEmailExceptionWhenExceptionOccurs() throws Exception {
        var message = mock(SendNotification.Message.class);

        when(template.processTemplate(message)).thenThrow(new RuntimeException("Error processing template"));

        assertThrows(EmailException.class, () -> underTest.send(message));

        verify(template).processTemplate(message);
        verify(sendGrid, never()).api(any(Request.class));
    }
}
