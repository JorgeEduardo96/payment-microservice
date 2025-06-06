package br.com.orderservice.api.controller;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

}
