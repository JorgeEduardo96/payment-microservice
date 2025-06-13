package br.com.notificationservice.domain.service;

import br.com.notificationservice.config.SendNotification;
import br.com.notificationservice.domain.exception.EmailException;
import br.com.notificationservice.domain.service.email.EmailProcessorTemplate;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Autowired;

public class EmailNotificationService implements SendNotification {

    @Autowired
    private EmailProcessorTemplate emailProcessorTemplate;
    @Autowired
    private Email fromEmail;
    @Autowired
    private SendGrid sendGrid;

    @Override
    public void send(Message message) {
        try {
            String htmlBody = emailProcessorTemplate.processTemplate(message);

            Email to = new Email(message.getDestinations().toArray()[0].toString());
            Content content = new Content("text/html", htmlBody);
            Mail mail = new Mail(fromEmail, message.getSubject(), to, content);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            if (response.getStatusCode() >= 400) {
                throw new EmailException("Error sending email: " + response.getStatusCode() + " - " + response.getBody());
            }

        } catch (Exception e) {
            throw new EmailException("Failed to send email" + e.getMessage(), e);
        }
    }

}
