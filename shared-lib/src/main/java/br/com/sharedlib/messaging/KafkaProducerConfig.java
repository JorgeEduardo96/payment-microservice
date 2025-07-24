package br.com.sharedlib.messaging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@EnableKafka
public class KafkaProducerConfig {

    private final ProducerFactory<String, Object> producerFactory;

    public KafkaProducerConfig(ProducerFactory<String, Object> producerFactory) {
        this.producerFactory = producerFactory;
    }

    @Bean
    public KafkaProducerInterceptor kafkaProducerInterceptor() {
        return new KafkaProducerInterceptor();
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory);
        template.setObservationEnabled(true);
        template.setProducerInterceptor(kafkaProducerInterceptor());
        return template;
    }

}
