package br.com.clientservice.api;

import br.com.clientservice.domain.dto.ClientCreateInputDTO;
import br.com.clientservice.domain.dto.ClientOutputDTO;
import br.com.clientservice.domain.dto.ClientUpdateInputDTO;
import br.com.clientservice.domain.service.ClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClientControllerTest {

    @Mock
    private ClientService service;

    @InjectMocks
    private ClientController underTest;

    @Test
    void fetchClientById() {
        var id = UUID.randomUUID();
        var client = mock(ClientOutputDTO.class);

        when(service.findClient(id)).thenReturn(client);

        var result = underTest.fetchClientById(id);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isSameAs(client);
    }

    @Test
    void createClient() {
        var clientCreateInputDTO = mock(ClientCreateInputDTO.class);
        var createdClient = mock(ClientOutputDTO.class);

        when(service.insert(clientCreateInputDTO)).thenReturn(createdClient);

        var result = underTest.createClient(clientCreateInputDTO);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isSameAs(createdClient);
    }

    @Test
    void updateClient() {
        var id = UUID.randomUUID();
        var clientUpdateInputDTO = mock(ClientUpdateInputDTO.class);
        var updatedClient = mock(ClientOutputDTO.class);

        when(service.update(id, clientUpdateInputDTO)).thenReturn(updatedClient);

        var result = underTest.updateClient(id, clientUpdateInputDTO);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isSameAs(updatedClient);
    }

}
