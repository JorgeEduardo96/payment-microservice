package br.com.clientservice.domain.service;

import br.com.clientservice.config.properties.KeycloakAdminProperties;
import br.com.clientservice.domain.exception.KeycloakUserProvisioningException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KeycloakUserProvisioningServiceTest {

    private static final String TOKEN_URI = "http://keycloak/realms/payment-microservice/protocol/openid-connect/token";
    private static final String ADMIN_BASE_URI = "http://keycloak/admin/realms/payment-microservice";

    private MockRestServiceServer server;
    private KeycloakUserProvisioningService underTest;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);

        KeycloakAdminProperties properties = new KeycloakAdminProperties();
        properties.setTokenUri(TOKEN_URI);
        properties.setAdminBaseUri(ADMIN_BASE_URI);
        properties.setClientId("client-service");
        properties.setClientSecret("client-service-secret");
        properties.setDefaultPassword("client123");
        properties.setClientRoleName("CLIENT");

        underTest = new KeycloakUserProvisioningService(restTemplate, properties);
    }

    @Test
    void createUserAuthenticatesAndCreatesKeycloakUser() {
        server.expect(requestTo(TOKEN_URI))
                .andExpect(method(POST))
                .andRespond(withSuccess("{\"access_token\":\"svc-token\",\"expires_in\":60}", MediaType.APPLICATION_JSON));

        server.expect(requestTo(ADMIN_BASE_URI + "/users"))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .location(java.net.URI.create(ADMIN_BASE_URI + "/users/user-123")));

        String userId = underTest.createUser("Jorge Silva", "jorge@example.com", UUID.fromString("11111111-1111-1111-1111-111111111111"));

        assertEquals("user-123", userId);
        server.verify();
    }

    @Test
    void assignClientRoleLooksUpRoleAndAssignsIt() {
        server.expect(requestTo(TOKEN_URI))
                .andExpect(method(POST))
                .andRespond(withSuccess("{\"access_token\":\"svc-token\",\"expires_in\":60}", MediaType.APPLICATION_JSON));

        server.expect(requestTo(ADMIN_BASE_URI + "/roles/CLIENT"))
                .andExpect(method(GET))
                .andRespond(withSuccess("{\"id\":\"role-456\",\"name\":\"CLIENT\"}", MediaType.APPLICATION_JSON));

        server.expect(requestTo(ADMIN_BASE_URI + "/users/user-123/role-mappings/realm"))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        underTest.assignClientRole("user-123");

        server.verify();
    }

    @Test
    void deleteUserRemovesKeycloakUser() {
        server.expect(requestTo(TOKEN_URI))
                .andExpect(method(POST))
                .andRespond(withSuccess("{\"access_token\":\"svc-token\",\"expires_in\":60}", MediaType.APPLICATION_JSON));

        server.expect(requestTo(ADMIN_BASE_URI + "/users/11111111-1111-1111-1111-111111111111"))
                .andExpect(method(DELETE))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        underTest.deleteUser(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        server.verify();
    }

    @Test
    void reusesCachedTokenAcrossCallsUntilItExpires() {
        // Only ONE token request is stubbed below - if the token weren't cached and
        // reused, the second Keycloak call (assignClientRole) would try to fetch a
        // new token and fail because no matching stub exists for it.
        server.expect(requestTo(TOKEN_URI))
                .andExpect(method(POST))
                .andRespond(withSuccess("{\"access_token\":\"svc-token\",\"expires_in\":60}", MediaType.APPLICATION_JSON));

        server.expect(requestTo(ADMIN_BASE_URI + "/users"))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .location(java.net.URI.create(ADMIN_BASE_URI + "/users/user-123")));

        server.expect(requestTo(ADMIN_BASE_URI + "/roles/CLIENT"))
                .andExpect(method(GET))
                .andRespond(withSuccess("{\"id\":\"role-456\",\"name\":\"CLIENT\"}", MediaType.APPLICATION_JSON));

        server.expect(requestTo(ADMIN_BASE_URI + "/users/user-123/role-mappings/realm"))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        underTest.createUser("Jorge Silva", "jorge@example.com", UUID.fromString("11111111-1111-1111-1111-111111111111"));
        underTest.assignClientRole("user-123");

        server.verify();
    }

    @Test
    void createUserThrowsWhenTokenRequestFails() {
        server.expect(requestTo(TOKEN_URI))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        UUID clientId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        assertThrows(KeycloakUserProvisioningException.class,
                () -> underTest.createUser("Jorge Silva", "jorge@example.com", clientId));
    }
}
