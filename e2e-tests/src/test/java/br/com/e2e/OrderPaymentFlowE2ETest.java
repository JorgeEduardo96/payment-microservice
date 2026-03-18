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
 * E2E tests covering the complete order payment flow: client creation → order creation → asynchronous payment processing.
 * <p>
 * Payment logic in payment-service:
 * - Last char of orderId is a letter (a-f) → status PAID
 * - Last char of orderId is a digit (0-9) → status FAILED
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

        // The payment-service processes the payment asynchronously via gRPC and publishes a Kafka event.
        // The order-service consumes the Kafka event and updates the order status accordingly.
        // We wait up to 30 seconds for the order status to change from PENDING_PAYMENT, polling every 2 seconds.
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

        // Find the final payment status (PAID or FAILED) based on the last char of the orderId,
        // which is determined by the payment-service logic.
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

    // Helpers

    /**
     * Create a client and wait for the Kafka event to be consumed by the order-service.
     * The order-service has its own database and populates clients via Kafka.
     * Without this wait, POST /order returns 404 because the client has not been propagated
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

        // Wait the client-service to publish the client-created event to
        // Kafka and for the order-service to consume it and make the client available in its database.
        // It will return 404 until the event is consumed, then it will return 200. We wait up to 30 seconds, polling every 2 seconds.
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
