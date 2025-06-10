package br.com.orderservice.controller;

import br.com.orderservice.domain.dto.ClientEventDTO;
import br.com.orderservice.domain.dto.OrderInputDTO;
import br.com.orderservice.domain.enumeration.PaymentMethod;
import br.com.orderservice.domain.repository.ClientRepository;
import br.com.orderservice.domain.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration")
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ClientRepository clientRepository;

    private UUID clientId;
    private final String clientName = "João da Silva";

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        var clientEvent = new ClientEventDTO(
                clientId,
                clientName,
                LocalDateTime.now(),
                null
        );
        clientRepository.upsert(clientEvent);
    }

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {
        var orderInputDTO = new OrderInputDTO(clientId, new BigDecimal("100.00"), "Portugal", PaymentMethod.CARD, null);

        mockMvc.perform(post("/order")
                        .contentType("application/json")
                        .header("X-From-Gateway", "true")
                        .content(objectMapper.writeValueAsString(orderInputDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(100.00))
                .andExpect(jsonPath("$.shippingAddress").value("Portugal"))
                .andExpect(jsonPath("$.clientName").value(clientName))
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.paymentMethod").value("CARD"));
    }

    @Test
    void shouldReturnNotFoundWhenCreatingOrderForNotExistingClient() throws Exception {
        var orderInputDTO = new OrderInputDTO(UUID.randomUUID(), new BigDecimal("100.00"), "Portugal", PaymentMethod.CARD, null);

        mockMvc.perform(post("/order")
                        .contentType("application/json")
                        .header("X-From-Gateway", "true")
                        .content(objectMapper.writeValueAsString(orderInputDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnListOfOrdersByClient() throws Exception {
        var orderInputDTO = new OrderInputDTO(clientId, new BigDecimal("100.00"), "Portugal", PaymentMethod.CARD, null);
        orderRepository.createOrder(orderInputDTO);

        mockMvc.perform(get("/order/client/" + clientId)
                        .contentType("application/json")
                        .header("X-From-Gateway", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientName").value(clientName))
                .andExpect(jsonPath("$[0].total").value(100.00))
                .andExpect(jsonPath("$[0].shippingAddress").value("Portugal"))
                .andExpect(jsonPath("$[0].status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$[0].paymentMethod").value("CARD"));
    }


    @Test
    void shouldReturnNotFoundWhenSearchingForUnexistingClient() throws Exception {
        mockMvc.perform(get("/order/client/" + UUID.randomUUID())
                        .contentType("application/json")
                        .header("X-From-Gateway", "true"))
                .andExpect(status().isNotFound());
    }

}
