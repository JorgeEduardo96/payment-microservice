package br.com.clientservice.controller;

import br.com.clientservice.domain.dto.ClientCreateInputDTO;
import br.com.clientservice.domain.dto.ClientUpdateInputDTO;
import br.com.clientservice.domain.repository.ClientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration")
public class ClientControllerIntegrationTest {

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.3"));

    @DynamicPropertySource
    static void configureKafka(DynamicPropertyRegistry registry) {
        kafkaContainer.start();
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ClientRepository clientRepository;

    @Test
    void shouldCreateClientSuccessfully() throws Exception {
        ClientCreateInputDTO input = new ClientCreateInputDTO(
                "João da Silva", "joao@email.com", "50674531094"
        );

        mockMvc.perform(post("/client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-From-Gateway", "true")
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("João da Silva"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingClientWithInvalidData() throws Exception {
        ClientCreateInputDTO input = new ClientCreateInputDTO(
                "", "invalid-email", "12345678901"
        );

        mockMvc.perform(post("/client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-From-Gateway", "true")
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.objects[*].userMessage", hasItems(
                        "must be a well-formed email address",
                        "Invalid CPF",
                        "must not be empty"
                )));
    }

    @Test
    void shouldUpdateClientSuccessfully() throws Exception {
        var client = clientRepository.insert(new ClientCreateInputDTO(
                "João da Silva", "joao2@gmail.com", "79748796027"));

        assertThat(clientRepository.findById(client.id())).isNotNull();

        var updateInput = new ClientUpdateInputDTO("João da Silva Updated", "emailnovo@email.com");

        mockMvc.perform(put("/client/{id}", client.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-From-Gateway", "true")
                        .content(objectMapper.writeValueAsString(updateInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(client.id().toString()))
                .andExpect(jsonPath("$.name").value(updateInput.name()))
                .andExpect(jsonPath("$.email").value(updateInput.email()));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentClient() throws Exception {
        var updateInput = new ClientUpdateInputDTO("João da Silva Updated", "emailnovo@email.com");
        mockMvc.perform(put("/client/{id}", "123e4567-e89b-12d3-a456-426614174000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-From-Gateway", "true")
                        .content(objectMapper.writeValueAsString(updateInput)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnClientById() throws Exception {
        var client = clientRepository.insert(new ClientCreateInputDTO(
                "João da Silva", "joao3@email.com", "31832880010"));

        mockMvc.perform(get("/client/{id}", client.id())
                        .header("X-From-Gateway", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(client.id().toString()))
                .andExpect(jsonPath("$.name").value(client.name()))
                .andExpect(jsonPath("$.cpf").value(client.cpf()));
    }

    @Test
    void shouldReturnNotFoundWhenClientDoesNotExist() throws Exception {
        mockMvc.perform(get("/client/{id}", "123e4567-e89b-12d3-a456-426614174000")
                        .header("X-From-Gateway", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
