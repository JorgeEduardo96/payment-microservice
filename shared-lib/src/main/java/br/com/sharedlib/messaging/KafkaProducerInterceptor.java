package br.com.sharedlib.messaging;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.MDC;

import java.util.Map;
import java.util.Objects;

public class KafkaProducerInterceptor implements ProducerInterceptor<String, Object> {

    @Override
    public ProducerRecord<String, Object> onSend(ProducerRecord<String, Object> record) {
        if (!Objects.isNull(MDC.get("traceId"))) {
            record.headers().add("x-trace-id", MDC.get("traceId").getBytes());
        }
        if (!Objects.isNull(MDC.get("spanId"))) {
            record.headers().add("x-span-id", MDC.get("spanId").getBytes());
        }
        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> map) {
    }
}
