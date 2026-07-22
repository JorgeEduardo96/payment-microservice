package br.com.clientservice.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("client-service.keycloak")
public class KeycloakAdminProperties {

    @NotBlank
    private String tokenUri;

    @NotBlank
    private String adminBaseUri;

    @NotBlank
    private String clientId;

    @NotBlank
    private String clientSecret;

    @NotBlank
    private String defaultPassword;

    @NotBlank
    private String clientRoleName;

}
