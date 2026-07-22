package br.com.e2e;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies the authorization boundary enforced by api-gateway's SecurityConfig:
 * every request needs a valid Keycloak JWT, and only ADMIN can create/edit clients.
 * These checks bypass RestAssured's global request spec (which BaseE2ETest wires up with an
 * admin token for every other test) since they need precise control over which token, if any,
 * is sent.
 */
class AuthE2ETest extends BaseE2ETest {

    private static final String GATEWAY_BASE_URL = "http://localhost:8080";
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void shouldRejectRequestWithoutTokenWith401() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GATEWAY_BASE_URL + "/client"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(clientBody()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(401, response.statusCode());
    }

    @Test
    void shouldRejectClientCreationByNonAdminUserWith403() throws Exception {
        String demoToken = fetchAccessToken("demo", "demo123");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GATEWAY_BASE_URL + "/client"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + demoToken)
                .POST(HttpRequest.BodyPublishers.ofString(clientBody()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(403, response.statusCode());
    }

    @Test
    void shouldAllowNonAdminUserToReadClients() throws Exception {
        String demoToken = fetchAccessToken("demo", "demo123");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GATEWAY_BASE_URL + "/client"))
                .header("Authorization", "Bearer " + demoToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    private String clientBody() {
        return """
                {
                  "name": "Should Not Be Created",
                  "email": "%s",
                  "cpf": "%s"
                }
                """.formatted(uniqueEmail(), generateValidCpf());
    }
}
