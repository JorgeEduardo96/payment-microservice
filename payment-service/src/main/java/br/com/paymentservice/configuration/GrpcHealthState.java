package br.com.paymentservice.configuration;

import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.protobuf.services.HealthStatusManager;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class GrpcHealthState {

    @Value("${spring.application.name:payment-service}")
    private String applicationName;

    private final HealthStatusManager healthStatusManager;
    private final AtomicReference<HealthCheckResponse.ServingStatus> currentStatus = new AtomicReference<>(HealthCheckResponse.ServingStatus.UNKNOWN);

    public GrpcHealthState(HealthStatusManager healthStatusManager) {
        this.healthStatusManager = healthStatusManager;
    }

    @PostConstruct
    public void init() {
        HealthCheckResponse.ServingStatus status = HealthCheckResponse.ServingStatus.SERVING;
        currentStatus.set(status);
        healthStatusManager.setStatus(applicationName, status);
    }

    public HealthCheckResponse.ServingStatus getCurrentStatus() {
        return currentStatus.get();
    }
}
