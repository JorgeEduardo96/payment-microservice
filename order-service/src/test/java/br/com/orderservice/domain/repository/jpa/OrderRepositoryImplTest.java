package br.com.orderservice.domain.repository.jpa;

import br.com.orderservice.domain.dto.OrderInputDTO;
import br.com.orderservice.domain.dto.OrderOutputDTO;
import br.com.orderservice.domain.entity.ClientJpaEntity;
import br.com.orderservice.domain.entity.OrderJpaEntity;
import br.com.orderservice.domain.enumeration.OrderStatus;
import br.com.orderservice.domain.repository.jpa.crudrepository.OrderJpaEntityCrudRepository;
import br.com.orderservice.mapper.OrderMapper;
import br.com.sharedlib.model.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderRepositoryImplTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderJpaEntityCrudRepository repository;

    @InjectMocks
    private OrderRepositoryImpl underTest;

    @Test
    void createOrder() {
        var inputDto = mock(OrderInputDTO.class);
        var mockedEntity = mock(OrderJpaEntity.class);
        var expectedResult = mock(OrderOutputDTO.class);

        when(orderMapper.toEntity(inputDto)).thenReturn(mockedEntity);
        when(orderMapper.toDto(mockedEntity)).thenReturn(expectedResult);
        when(repository.save(mockedEntity)).thenReturn(mockedEntity);

        var result = underTest.createOrder(inputDto);

        verify(repository).save(mockedEntity);
        verify(mockedEntity).applyDiscount();
        verify(orderMapper).toEntity(inputDto);
        verify(orderMapper).toDto(mockedEntity);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void processPayment() {
        var orderId = UUID.randomUUID();
        var status = OrderStatus.PAID;
        var entity = mock(OrderJpaEntity.class);

        when(repository.findById(orderId)).thenReturn(Optional.of(entity));

        underTest.processPayment(orderId, status);

        verify(repository).findById(orderId);
        verify(entity).setStatus(status);
        verify(repository).save(entity);
    }

    @Test
    void processPayment_throwsExceptionWhenOrderNotFound() {
        var orderId = UUID.randomUUID();
        var status = OrderStatus.PAID;

        when(repository.findById(orderId)).thenReturn(Optional.empty());

        EntityNotFoundException thrownException = assertThrows(EntityNotFoundException.class, () -> underTest.processPayment(orderId, status));

        assertThat(thrownException).isInstanceOf(EntityNotFoundException.class);
        assertThat(thrownException.getMessage()).contains("Order");

        verify(repository).findById(orderId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void ordersByClientId() {
        var clientId = UUID.randomUUID();
        var entity1 = mock(OrderJpaEntity.class);
        var entity2 = mock(OrderJpaEntity.class);

        var outputDto1 = mock(OrderOutputDTO.class);
        var outputDto2 = mock(OrderOutputDTO.class);

        when(repository.findByClientId(clientId)).thenReturn(List.of(entity1, entity2));
        when(entity1.getClient()).thenReturn(mock(ClientJpaEntity.class));
        when(entity2.getClient()).thenReturn(mock(ClientJpaEntity.class));

        List<OrderOutputDTO> result = underTest.ordersByClientId(clientId);

        verify(repository).findByClientId(clientId);
        assertThat(result).containsExactly(outputDto1, outputDto2);
        verifyNoMoreInteractions(repository, orderMapper);
    }

}
