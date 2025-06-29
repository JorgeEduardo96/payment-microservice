package br.com.orderservice.domain.service;

import br.com.orderservice.domain.dto.ClientEventDTO;
import br.com.orderservice.domain.dto.OrderInputDTO;
import br.com.orderservice.domain.dto.OrderOutputDTO;
import br.com.orderservice.domain.enumeration.OrderStatus;
import br.com.orderservice.domain.enumeration.PaymentMethod;
import br.com.orderservice.domain.event.OrderCreatedEvent;
import br.com.orderservice.domain.repository.ClientRepository;
import br.com.orderservice.domain.repository.OrderRepository;
import br.com.sharedlib.model.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository repository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private OrderService underTest;

    @Test
    void createOrder() {
        var orderId = UUID.randomUUID();
        var clientId = UUID.randomUUID();
        var mockedInput = mock(OrderInputDTO.class);
        var mockedOutput = mock(OrderOutputDTO.class);
        var mockedClient = mock(ClientEventDTO.class);

        when(clientRepository.findById(mockedInput.clientId())).thenReturn(Optional.of(mockedClient));
        when(repository.createOrder(mockedInput)).thenReturn(mockedOutput);

        when(mockedClient.id()).thenReturn(clientId);
        when(mockedClient.name()).thenReturn("John Doe");

        when(mockedOutput.id()).thenReturn(orderId);
        when(mockedOutput.total()).thenReturn(new BigDecimal("100.00"));
        when(mockedOutput.shippingAddress()).thenReturn("123 Main St, Springfield");
        when(mockedOutput.paymentMethod()).thenReturn(PaymentMethod.CARD);
        when(mockedOutput.status()).thenReturn(OrderStatus.PENDING_PAYMENT);

        var result = underTest.createOrder(mockedInput);

        assertThat(result.id()).isEqualTo(orderId);
        assertThat(result.clientId()).isEqualTo(clientId);
        assertThat(result.clientName()).isEqualTo("John Doe");
        assertThat(result.total()).isEqualTo(new BigDecimal("100.00"));
        assertThat(result.shippingAddress()).isEqualTo("123 Main St, Springfield");
        assertThat(result.paymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(result.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);

        verify(publisher).publishEvent(any(OrderCreatedEvent.class));
        verify(clientRepository).findById(mockedInput.clientId());
        verify(repository).createOrder(mockedInput);
    }

    @Test
    void createOrder_throwsExceptionWhenClientNotFound() {
        var mockedInput = mock(OrderInputDTO.class);
        when(mockedInput.clientId()).thenReturn(UUID.randomUUID());
        when(clientRepository.findById(mockedInput.clientId())).thenReturn(Optional.empty());

        EntityNotFoundException thrownException = assertThrows(EntityNotFoundException.class, () -> underTest.createOrder(mockedInput));

        assertThat(thrownException).isInstanceOf(EntityNotFoundException.class);
        assertThat(thrownException.getMessage()).contains("Client");

        verify(clientRepository).findById(mockedInput.clientId());
        verifyNoMoreInteractions(clientRepository);
    }

    @Test
    void getOrdersByClientId() {
        var clientId = UUID.randomUUID();
        var mockedClient = mock(ClientEventDTO.class);
        var mockedOutput = mock(OrderOutputDTO.class);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(mockedClient));
        when(repository.ordersByClientId(clientId)).thenReturn(List.of(mockedOutput));

        List<OrderOutputDTO> result = underTest.getOrdersByClientId(clientId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(mockedOutput);
        verify(clientRepository).findById(clientId);
        verify(repository).ordersByClientId(clientId);
    }

    @Test
    void getOrdersByClientId_throwsExceptionWhenClientNotFound() {
        var clientId = UUID.randomUUID();
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        EntityNotFoundException thrownException = assertThrows(EntityNotFoundException.class, () -> underTest.getOrdersByClientId(clientId));

        assertThat(thrownException).isInstanceOf(EntityNotFoundException.class);
        assertThat(thrownException.getMessage()).contains("Client");

        verify(clientRepository).findById(clientId);
        verifyNoMoreInteractions(clientRepository);
    }
}
