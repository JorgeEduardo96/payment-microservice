package br.com.notificationservice.domain.service;

import br.com.notificationservice.config.SendNotification;
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

    public void sendNotification(PaymentResponseEventDTO paymentResponseEventDTO) {
        if ("PAID".equalsIgnoreCase(paymentResponseEventDTO.status())) {
            var client = clientRepository.findById(paymentResponseEventDTO.clientId());

            var variables = new HashMap<String, String>();
            variables.put("clientName", client.name());
            variables.put("orderId", paymentResponseEventDTO.orderId().toString());
            variables.put("paymentMethod", paymentResponseEventDTO.paymentMethod());

            var message = SendNotification.Message.builder()
                    .subject("Order Confirmed - " + paymentResponseEventDTO.orderId())
                    .body("/order-paid.html")
                    .variables(variables)
                    .destination(client.email())
                    .build();

            sendNotification.send(message);
        }
    }
}
