package br.com.paymentservice.messaging;

import br.com.paymentservice.domain.dto.PaymentResponseDTO;
import br.com.paymentservice.messaging.producer.PaymentProducer;
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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"payment-topic"})
@EnableKafka
public class PaymentProducerIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private PaymentProducer paymentProducer;

    @Test
    void shouldSendPaymentEventToKafkaTopic() throws Exception {
        String topic = "payment-topic";
        var dto = new PaymentResponseDTO(UUID.randomUUID(), "PAID", "CASH", UUID.randomUUID());

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "false", embeddedKafka);
        consumerProps.put("key.deserializer", StringDeserializer.class);
        consumerProps.put("value.deserializer", StringDeserializer.class);

        var cf = new DefaultKafkaConsumerFactory<String, String>(consumerProps);
        var container = new KafkaMessageListenerContainer<>(cf, new ContainerProperties(topic));
        var records = new LinkedBlockingQueue<ConsumerRecord<String, String>>();

        container.setupMessageListener((MessageListener<String, String>) records::offer);
        container.start();

        paymentProducer.sendPaymentEvent(topic, dto);

        ConsumerRecord<String, String> received = records.poll(5, java.util.concurrent.TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.topic()).isEqualTo(topic);
        assertThat(received.value()).contains(dto.orderId().toString(), dto.status(), dto.paymentMethod(), dto.clientId().toString());

        container.stop();
    }

}
