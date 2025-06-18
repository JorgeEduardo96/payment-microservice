package br.com.paymentservice.configuration;

import io.grpc.health.v1.HealthCheckResponse;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class GrpcHealthIndicator implements HealthIndicator {

    private final GrpcHealthState grpcHealthState;

    public GrpcHealthIndicator(GrpcHealthState grpcHealthState) {
        this.grpcHealthState = grpcHealthState;
    }

    @Override
    public Health health() {
        HealthCheckResponse.ServingStatus status = grpcHealthState.getCurrentStatus();
        if (status == HealthCheckResponse.ServingStatus.SERVING) {
            return Health.up().withDetail("grpc", "SERVING").build();
        } else {
            return Health.down().withDetail("grpc", status.name()).build();
        }
    }
}
