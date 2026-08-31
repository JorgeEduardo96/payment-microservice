package br.com.clientservice.domain.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ClientMetrics {

    private final Counter clientsRegisteredCounter;

    public ClientMetrics(MeterRegistry meterRegistry) {
        this.clientsRegisteredCounter = Counter.builder("clients_registered_total")
                .description("Total number of clients successfully registered (persisted + provisioned in Keycloak)")
                .register(meterRegistry);
    }

    public void incrementRegistered() {
        clientsRegisteredCounter.increment();
    }
}
