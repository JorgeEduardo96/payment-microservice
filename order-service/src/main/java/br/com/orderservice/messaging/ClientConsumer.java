package br.com.orderservice.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ClientConsumer {

    @KafkaListener(topics = "client-created-topic", groupId = "order-service-group")
    public void consume(String message) {
        // Process the message received from the client-created-topic
        System.out.println("Received message from client-created-topic: " + message);
        // Here you can add logic to handle the message, such as creating an order based on the client data
    }

}
