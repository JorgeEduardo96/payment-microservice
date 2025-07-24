package br.com.sharedlib.messaging;

import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.RecordInterceptor;

import java.nio.charset.StandardCharsets;

@Configuration
public class KafkaListenerContainerFactoryConfig {

    @Bean
    public RecordInterceptor<String, Object> recordInterceptor() {
        return (record, consumer) -> {
            Header traceHeader = record.headers().lastHeader("x-trace-id");
            Header spanHeader = record.headers().lastHeader("x-span-id");

            if (traceHeader != null) {
                MDC.put("traceId", new String(traceHeader.value(), StandardCharsets.UTF_8));
            }

            if (spanHeader != null) {
                MDC.put("parentSpanId", new String(spanHeader.value(), StandardCharsets.UTF_8));
            }

            return record;
        };
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            DefaultErrorHandler errorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setObservationEnabled(true);
        factory.setCommonErrorHandler(errorHandler);
        factory.setRecordInterceptor(recordInterceptor());

        return factory;
    }
}
