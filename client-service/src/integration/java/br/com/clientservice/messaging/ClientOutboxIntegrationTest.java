package br.com.clientservice.messaging;

import br.com.clientservice.domain.dto.ClientCreateInputDTO;
import br.com.clientservice.domain.dto.ClientUpdateInputDTO;
import br.com.clientservice.domain.entity.OutboxJpaEntity;
import br.com.clientservice.domain.repository.jpa.crudrepository.ClientJpaEntityCrudRepository;
import br.com.clientservice.domain.repository.jpa.crudrepository.OutboxJpaEntityCrudRepository;
import br.com.clientservice.domain.service.ClientService;
import br.com.clientservice.domain.service.KeycloakUserProvisioningService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integration")
@EnableKafka
@EmbeddedKafka(
        partitions = 1,
        topics = {"client-created-topic", "client-updated-topic"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class ClientOutboxIntegrationTest {

    @Autowired
    private ClientService clientService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private ClientJpaEntityCrudRepository clientCrudRepository;

    @Autowired
    private OutboxJpaEntityCrudRepository outboxCrudRepository;

    @MockitoBean
    private KeycloakUserProvisioningService keycloakUserProvisioningService;

    @AfterEach
    void cleanUp() {
        outboxCrudRepository.deleteAll();
        clientCrudRepository.deleteAll();
    }

    @Test
    void shouldPersistAndPublishClientCreatedEvent() throws Exception {
        var topic = "client-created-topic";
        var input = new ClientCreateInputDTO(
                "Alice",
                "alice-" + UUID.randomUUID() + "@email.com",
                "50674531094"
        );
        var listener = startListener(topic);

        try {
            clientService.insert(input);

            ConsumerRecord<String, String> received = listener.records.poll(10, TimeUnit.SECONDS);
            assertThat(received).isNotNull();
            assertThat(received.topic()).isEqualTo(topic);
            assertThat(received.value()).contains("Alice", input.email());

            var outbox = awaitSingleOutbox();
            assertThat(outbox.getEventType()).isEqualTo("client-created");
            assertThat(outbox.getStatus()).isEqualTo("PUBLISHED");
        } finally {
            listener.container.stop();
        }
    }

    @Test
    void shouldPersistAndPublishClientUpdatedEvent() throws Exception {
        var created = clientService.insert(new ClientCreateInputDTO(
                "Bob",
                "bob-" + UUID.randomUUID() + "@email.com",
                "79748796027"
        ));

        var topic = "client-updated-topic";
        var listener = startListener(topic);

        try {
            clientService.update(created.id(), new ClientUpdateInputDTO(
                    "Bob Updated",
                    "bob-updated-" + UUID.randomUUID() + "@email.com"
            ));

            ConsumerRecord<String, String> received = listener.records.poll(10, TimeUnit.SECONDS);
            assertThat(received).isNotNull();
            assertThat(received.topic()).isEqualTo(topic);
            assertThat(received.value()).contains("Bob Updated");

            assertThat(outboxCrudRepository.findAll()).hasSize(2);
            assertThat(outboxCrudRepository.findAll())
                    .extracting("eventType")
                    .contains("client-created", "client-updated");
        } finally {
            listener.container.stop();
        }
    }

    private ListenerHandle startListener(String topic) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("outbox-integration-" + topic, "false", embeddedKafka);
        consumerProps.put("key.deserializer", StringDeserializer.class);
        consumerProps.put("value.deserializer", StringDeserializer.class);

        var consumerFactory = new DefaultKafkaConsumerFactory<String, String>(consumerProps);
        var container = new KafkaMessageListenerContainer<>(consumerFactory, new ContainerProperties(topic));
        var records = new LinkedBlockingQueue<ConsumerRecord<String, String>>();

        container.setupMessageListener((MessageListener<String, String>) records::offer);
        container.start();

        return new ListenerHandle(container, records);
    }

    private OutboxJpaEntity awaitSingleOutbox() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 10000L;
        while (System.currentTimeMillis() < deadline) {
            var outboxes = outboxCrudRepository.findAll();
            if (outboxes.size() == 1 && "PUBLISHED".equals(outboxes.get(0).getStatus())) {
                return outboxes.get(0);
            }
            Thread.sleep(100L);
        }
        return outboxCrudRepository.findAll().get(0);
    }

    private record ListenerHandle(
            KafkaMessageListenerContainer<String, String> container,
            LinkedBlockingQueue<ConsumerRecord<String, String>> records
    ) {
    }
}
