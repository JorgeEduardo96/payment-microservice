package br.com.orderservice.domain.service;

import br.com.orderservice.domain.enumeration.OrderStatus;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class OrderMetrics {

    private final MeterRegistry meterRegistry;

    public OrderMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementStatus(OrderStatus status) {
        meterRegistry.counter("orders_status_total", "status", status.name()).increment();
    }
}
