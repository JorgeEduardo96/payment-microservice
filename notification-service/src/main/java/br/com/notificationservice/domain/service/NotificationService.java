package br.com.notificationservice.domain.service;

import br.com.notificationservice.config.SendNotification;
import br.com.notificationservice.domain.dto.NotificationMessage;
import br.com.notificationservice.domain.dto.PaymentResponseEventDTO;
import br.com.notificationservice.domain.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ClientRepository clientRepository;
    private final SendNotification sendNotification;
    private final WebSocketNotificationService webSocketNotificationService;

    public void sendNotification(PaymentResponseEventDTO paymentResponseEventDTO) {
        boolean paid = "PAID".equalsIgnoreCase(paymentResponseEventDTO.status());

        if (paid) {
            sendEmail(paymentResponseEventDTO);
        }

        notifyClientOverWebSocket(paymentResponseEventDTO, paid);
    }

    private void sendEmail(PaymentResponseEventDTO event) {
        var client = clientRepository.findById(event.clientId());

        var variables = new HashMap<String, String>();
        variables.put("clientName", client.name());
        variables.put("orderId", event.orderId().toString());
        variables.put("paymentMethod", event.paymentMethod());

        var message = SendNotification.Message.builder()
                .subject("Order Confirmed - " + event.orderId())
                .body("/order-paid.html")
                .variables(variables)
                .destination(client.email())
                .build();

        sendNotification.send(message);
    }

    private void notifyClientOverWebSocket(PaymentResponseEventDTO event, boolean paid) {
        var wsMessage = NotificationMessage.ofPayment(
                paid ? "PAYMENT_CONFIRMED" : "PAYMENT_FAILED",
                paid ? "Payment confirmed" : "Payment failed",
                "Your order " + event.orderId() + (paid ? " has been paid." : " payment has failed."),
                event.orderId(),
                event.status());

        webSocketNotificationService.notifyClient(event.clientId().toString(), wsMessage);
    }
}
