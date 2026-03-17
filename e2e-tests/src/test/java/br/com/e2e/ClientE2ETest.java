package br.com.e2e;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;

class ClientE2ETest extends BaseE2ETest {

    @Test
    void shouldCreateClientSuccessfully() {
        String email = uniqueEmail();
        String cpf = generateValidCpf();

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "João Silva",
                          "email": "%s",
                          "cpf": "%s"
                        }
                        """.formatted(email, cpf))
                .when()
                .post("/client")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("João Silva"))
                .body("email", equalTo(email))
                .body("createdAt", notNullValue());
    }

    @Test
    void shouldFetchClientById() {
        String email = uniqueEmail();
        String cpf = generateValidCpf();

        String clientId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "Maria Souza",
                          "email": "%s",
                          "cpf": "%s"
                        }
                        """.formatted(email, cpf))
                .post("/client")
                .then()
                .statusCode(201)
                .extract().path("id");

        given()
                .get("/client/" + clientId)
                .then()
                .statusCode(200)
                .body("id", equalTo(clientId))
                .body("name", equalTo("Maria Souzaa"))
                .body("email", equalTo(email));
    }

    @Test
    void shouldUpdateClientSuccessfully() {
        String email = uniqueEmail();
        String cpf = generateValidCpf();

        String clientId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "Carlos Antigo",
                          "email": "%s",
                          "cpf": "%s"
                        }
                        """.formatted(email, cpf))
                .post("/client")
                .then()
                .statusCode(201)
                .extract().path("id");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "Carlos Atualizado"
                        }
                        """)
                .when()
                .put("/client/" + clientId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Carlos Atualizado"));
    }

    @Test
    void shouldReturn404WhenClientNotFound() {
        given()
                .get("/client/" + UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn400WhenCreatingClientWithInvalidEmail() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "Teste",
                          "email": "email-invalido",
                          "cpf": "%s"
                        }
                        """.formatted(generateValidCpf()))
                .when()
                .post("/client")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldReturn400WhenCreatingClientWithDuplicateEmail() {
        String email = uniqueEmail();
        String cpf1 = generateValidCpf();
        String cpf2 = generateValidCpf();

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "Primeiro",
                          "email": "%s",
                          "cpf": "%s"
                        }
                        """.formatted(email, cpf1))
                .post("/client")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "Segundo",
                          "email": "%s",
                          "cpf": "%s"
                        }
                        """.formatted(email, cpf2))
                .when()
                .post("/client")
                .then()
                .statusCode(400);
    }
}
