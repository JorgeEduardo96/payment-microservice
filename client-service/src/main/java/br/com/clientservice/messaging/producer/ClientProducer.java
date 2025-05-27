package br.com.clientservice.messaging.producer;

import br.com.clientservice.domain.dto.ClientOutputDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ClientProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public ClientProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendClientEvent(String topic, ClientOutputDTO client) {
        kafkaTemplate.send(topic, client.toString());
    }

}
