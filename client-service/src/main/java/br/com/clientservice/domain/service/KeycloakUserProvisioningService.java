package br.com.clientservice.domain.service;

import br.com.clientservice.config.properties.KeycloakAdminProperties;
import br.com.clientservice.domain.exception.KeycloakUserProvisioningException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserProvisioningService {

    private final RestTemplate keycloakAdminRestTemplate;
    private final KeycloakAdminProperties properties;

    private String cachedAccessToken;
    private Instant cachedAccessTokenExpiresAt;

    public String createUser(String name, String email, UUID clientId) {
        var accessToken = fetchServiceAccountToken();
        URI userLocation = createKeycloakUser(accessToken, name, email, clientId);
        String userId = extractUserId(userLocation);

        log.info("Keycloak user provisioned for client, email: {}, keycloakUserId: {}", email, userId);

        return userId;
    }

    public void deleteUser(UUID userId) {
        var accessToken = fetchServiceAccountToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        try {
            keycloakAdminRestTemplate.exchange(
                    properties.getAdminBaseUri() + "/users/" + userId,
                    HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
            log.info("Keycloak user deleted, keycloakUserId: {}", userId);
        } catch (Exception e) {
            throw new KeycloakUserProvisioningException("Failed to delete Keycloak user with id " + userId, e);
        }
    }

    private String fetchServiceAccountToken() {
        if (cachedAccessToken != null && Instant.now().isBefore(cachedAccessTokenExpiresAt)) {
            return cachedAccessToken;
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            var response = keycloakAdminRestTemplate.postForEntity(
                    properties.getTokenUri(), new HttpEntity<>(form, headers), Map.class);

            Map<?, ?> body = response.getBody();
            Object token = body != null ? body.get("access_token") : null;
            if (token == null) {
                throw new KeycloakUserProvisioningException("Keycloak token response had no access_token");
            }

            long expiresInSeconds = body.get("expires_in") instanceof Number n ? n.longValue() : 60;
            cachedAccessToken = token.toString();
            cachedAccessTokenExpiresAt = Instant.now().plusSeconds(expiresInSeconds - 10);

            return cachedAccessToken;
        } catch (KeycloakUserProvisioningException e) {
            throw e;
        } catch (Exception e) {
            throw new KeycloakUserProvisioningException("Failed to authenticate client-service against Keycloak", e);
        }
    }

    private URI createKeycloakUser(String accessToken, String name, String email, UUID clientId) {
        Map<String, Object> credential = Map.of(
                "type", "password",
                "value", properties.getDefaultPassword(),
                "temporary", false
        );

        Map<String, Object> user = Map.of(
                "username", email,
                "email", email,
                "firstName", name,
                "enabled", true,
                "emailVerified", true,
                "credentials", List.of(credential),
                "attributes", Map.of("clientId", List.of(clientId.toString()))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            var response = keycloakAdminRestTemplate.postForEntity(
                    properties.getAdminBaseUri() + "/users", new HttpEntity<>(user, headers), Void.class);

            URI location = response.getHeaders().getLocation();
            if (location == null) {
                throw new KeycloakUserProvisioningException("Keycloak did not return a Location header for the created user");
            }
            return location;
        } catch (Exception e) {
            throw new KeycloakUserProvisioningException("Failed to create Keycloak user for email " + email, e);
        }
    }

    public void assignClientRole(String userId) {
        var accessToken = fetchServiceAccountToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        Map<String, Object> role;
        try {
            var roleResponse = keycloakAdminRestTemplate.exchange(
                    properties.getAdminBaseUri() + "/roles/" + properties.getClientRoleName(),
                    HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            role = roleResponse.getBody();
        } catch (Exception e) {
            throw new KeycloakUserProvisioningException("Failed to look up the " + properties.getClientRoleName() + " realm role", e);
        }

        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            keycloakAdminRestTemplate.postForEntity(
                    properties.getAdminBaseUri() + "/users/" + userId + "/role-mappings/realm",
                    new HttpEntity<>(List.of(role), headers), Void.class);
        } catch (Exception e) {
            throw new KeycloakUserProvisioningException("Failed to assign role " + properties.getClientRoleName() + " to Keycloak user " + userId, e);
        }
    }

    private String extractUserId(URI userLocation) {
        String path = userLocation.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
