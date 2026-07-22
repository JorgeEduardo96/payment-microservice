package br.com.api_gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class SecurityConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ReactiveJwtDecoder jwtDecoder;

    @Test
    void actuatorRoutesArePublic() {
        webTestClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().value(status -> assertThat(status).isNotEqualTo(401));
    }

    @Test
    void webSocketNotificationsRouteIsPublic() {
        webTestClient.get().uri("/ws-notifications/info")
                .exchange()
                .expectStatus().value(status -> assertThat(status).isNotEqualTo(401));
    }

    @Test
    void protectedRouteWithoutTokenIsUnauthorized() {
        webTestClient.get().uri("/client/123")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void userRoleCanReadClients() {
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwtWithRoles("USER")));

        webTestClient.get().uri("/client/123")
                .header("Authorization", "Bearer fake-token")
                .exchange()
                .expectStatus().value(status -> assertThat(status).isNotIn(401, 403));
    }

    @Test
    void userRoleIsForbiddenFromCreatingClients() {
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwtWithRoles("USER")));

        webTestClient.post().uri("/client")
                .header("Authorization", "Bearer fake-token")
                .bodyValue("{}")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void adminRoleCanCreateClients() {
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(jwtWithRoles("ADMIN")));

        webTestClient.post().uri("/client")
                .header("Authorization", "Bearer fake-token")
                .bodyValue("{}")
                .exchange()
                .expectStatus().value(status -> assertThat(status).isNotIn(401, 403));
    }

    private Jwt jwtWithRoles(String... roles) {
        return Jwt.withTokenValue("fake-token")
                .header("alg", "none")
                .claim("sub", "demo")
                .claim("realm_access", Map.of("roles", List.of(roles)))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }
}
