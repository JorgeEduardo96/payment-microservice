package br.com.orderservice.messaging.consumer;

import br.com.orderservice.domain.repository.ClientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClientConsumerTest {

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientConsumer underTest;

    @Test
    void consume() {

    }

}
