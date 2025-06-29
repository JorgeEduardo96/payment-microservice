package br.com.orderservice.api;

import br.com.orderservice.domain.dto.OrderInputDTO;
import br.com.orderservice.domain.dto.OrderOutputDTO;
import br.com.orderservice.domain.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController underTest;

    @Test
    void createOrder() {
        var orderInputDTO = mock(OrderInputDTO.class);
        var expectedResult = mock(OrderOutputDTO.class);

        when(orderService.createOrder(orderInputDTO)).thenReturn(expectedResult);

        ResponseEntity<OrderOutputDTO> result = underTest.createOrder(orderInputDTO);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(expectedResult);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getOrdersByClient() {
        var clientId = UUID.randomUUID();
        var expectedResult = mock(List.class);

        when(orderService.getOrdersByClientId(clientId)).thenReturn(expectedResult);

        var result = underTest.getOrdersByClient(clientId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(expectedResult);
        verify(orderService).getOrdersByClientId(clientId);
    }

}
