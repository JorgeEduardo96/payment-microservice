package br.com.clientservice.messaging;

import br.com.clientservice.domain.dto.ClientOutputDTO;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"client-created-topic", "client-updated-topic"})
@EnableKafka
public class ClientProducerIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private ClientProducer clientProducer;

    @Test
    void shouldSendClientEventToKafkaTopic() throws Exception {
        String topic = "client-created-topic";
        var dto = new ClientOutputDTO(UUID.randomUUID(), "João", "joao@email.com", "12345678901", LocalDateTime.now(), null);

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "false", embeddedKafka);
        consumerProps.put("key.deserializer", StringDeserializer.class);
        consumerProps.put("value.deserializer", StringDeserializer.class);

        var cf = new DefaultKafkaConsumerFactory<String, String>(consumerProps);
        var container = new KafkaMessageListenerContainer<>(cf, new ContainerProperties(topic));
        var records = new LinkedBlockingQueue<ConsumerRecord<String, String>>();

        container.setupMessageListener((MessageListener<String, String>) records::offer);
        container.start();

        clientProducer.sendClientEvent(topic, dto);

        ConsumerRecord<String, String> received = records.poll(5, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.topic()).isEqualTo(topic);
        assertThat(received.value()).contains("João", "joao@email.com");

        container.stop();
    }
}
