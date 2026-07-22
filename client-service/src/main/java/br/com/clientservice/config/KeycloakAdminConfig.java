package br.com.clientservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class KeycloakAdminConfig {

    @Bean
    public RestTemplate keycloakAdminRestTemplate() {
        return new RestTemplate();
    }
}
