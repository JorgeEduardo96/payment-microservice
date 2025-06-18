package br.com.paymentservice.configuration;

import io.grpc.protobuf.services.HealthStatusManager;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcHealthConfig {

    @Bean
    public HealthStatusManager healthStatusManager() {
        return new HealthStatusManager();
    }

    @Bean
    public GrpcServerConfigurer healthConfigurer(HealthStatusManager healthStatusManager) {
        return serverBuilder -> serverBuilder.addService(healthStatusManager.getHealthService());
    }

}
