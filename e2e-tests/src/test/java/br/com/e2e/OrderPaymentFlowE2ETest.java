package br.com.e2e;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Testes E2E do fluxo completo: criação de cliente → criação de pedido → processamento de pagamento.
 * <p>
 * Lógica de pagamento do payment-service:
 * - Último caractere do orderId é letra (a-f) → status PAID
 * - Último caractere do orderId é dígito (0-9) → status FAILED
 */
class OrderPaymentFlowE2ETest extends BaseE2ETest {

    @Test
    void shouldCreateOrderWithPendingPaymentStatusInitially() {
        String clientId = createClient();

        given()
                .contentType(ContentType.JSON)
                .body(orderBody(clientId))
                .when()
                .post("/order")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("clientId", equalTo(clientId))
                .body("status", equalTo("PENDING_PAYMENT"));
    }

    @Test
    void shouldProcessPaymentAsynchronouslyAfterOrderCreation() {
        String clientId = createClient();

        String orderId = given()
                .contentType(ContentType.JSON)
                .body(orderBody(clientId))
                .post("/order")
                .then()
                .statusCode(201)
                .extract().path("id");

        // O payment-service processa via gRPC e publica evento Kafka.
        // O order-service consome o evento e atualiza o status.
        // Aguardamos até 30 segundos para o status sair de PENDING_PAYMENT.
        await()
                .atMost(30, SECONDS)
                .pollInterval(2, SECONDS)
                .untilAsserted(() ->
                        given()
                                .get("/order/client/" + clientId)
                                .then()
                                .statusCode(200)
                                .body("[0].status", not(equalTo("PENDING_PAYMENT")))
                );
    }

    @Test
    void shouldDeterminePaymentStatusBasedOnOrderIdLastChar() {
        String clientId = createClient();

        String orderId = given()
                .contentType(ContentType.JSON)
                .body(orderBody(clientId))
                .post("/order")
                .then()
                .statusCode(201)
                .extract().path("id");

        // Descobre o status esperado com base na lógica do payment-service:
        // último char do UUID → letra = PAID, dígito = FAILED
        char lastChar = orderId.charAt(orderId.length() - 1);
        String expectedStatus = Character.isLetter(lastChar) ? "PAID" : "FAILED";

        await()
                .atMost(30, SECONDS)
                .pollInterval(2, SECONDS)
                .untilAsserted(() ->
                        given()
                                .get("/order/client/" + clientId)
                                .then()
                                .statusCode(200)
                                .body("[0].status", equalTo(expectedStatus))
                );
    }

    @Test
    void shouldListAllOrdersForAClient() {
        String clientId = createClient();

        given()
                .contentType(ContentType.JSON)
                .body(orderBody(clientId))
                .post("/order")
                .then().statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(orderBody(clientId))
                .post("/order")
                .then().statusCode(201);

        given()
                .get("/order/client/" + clientId)
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("[0].id", notNullValue())
                .body("[1].id", notNullValue());
    }

    @Test
    void shouldReturn404WhenCreatingOrderForNonExistentClient() {
        given()
                .contentType(ContentType.JSON)
                .body(orderBody(UUID.randomUUID().toString()))
                .when()
                .post("/order")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn404WhenListingOrdersForNonExistentClient() {
        given()
                .get("/order/client/" + UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Cria um cliente e aguarda o evento Kafka ser consumido pelo order-service.
     * O order-service tem seu próprio banco de dados e popula os clientes via Kafka.
     * Sem essa espera, o POST /order retorna 404 porque o cliente ainda não foi propagado.
     */
    private String createClient() {
        String clientId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "Cliente E2E",
                          "email": "%s",
                          "cpf": "%s"
                        }
                        """.formatted(uniqueEmail(), generateValidCpf()))
                .post("/client")
                .then()
                .statusCode(201)
                .extract().path("id");

        // Aguarda o evento Kafka chegar ao order-service.
        // GET /order/client/{id} retorna 200 quando o cliente está disponível,
        // e 404 enquanto o evento ainda não foi consumido.
        await()
                .atMost(30, SECONDS)
                .pollInterval(2, SECONDS)
                .untilAsserted(() ->
                        given()
                                .get("/order/client/" + clientId)
                                .then()
                                .statusCode(200)
                );

        return clientId;
    }

    private String orderBody(String clientId) {
        return """
                {
                  "clientId": "%s",
                  "total": 250.00,
                  "shippingAddress": "Rua das Flores, 123 - São Paulo/SP",
                  "paymentMethod": "CARD"
                }
                """.formatted(clientId);
    }
}
