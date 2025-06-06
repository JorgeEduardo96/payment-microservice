package br.com.clientservice.domain.event.listener;

import br.com.clientservice.domain.dto.ClientOutputDTO;
import br.com.clientservice.domain.event.ClientCreatedEvent;
import br.com.clientservice.domain.event.ClientUpdatedEvent;
import br.com.clientservice.messaging.producer.ClientProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientEventListener {

    private final ClientProducer producer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleClientCreated(ClientCreatedEvent event) throws Exception {
        ClientOutputDTO client = event.client();
        log.info("AFTER COMMIT: Sending kafka event for client: {}", client.id());
        producer.sendClientEvent("client-created-topic", client);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleClientUpdated(ClientUpdatedEvent event) throws Exception {
        ClientOutputDTO client = event.client();
        log.info("AFTER COMMIT: Sending kafka event for updated client: {}", client.id());
        producer.sendClientEvent("client-updated-topic", client);
    }

}
