package br.com.orderservice.mapper;

import br.com.orderservice.domain.dto.OrderInputDTO;
import br.com.orderservice.domain.dto.OrderOutputDTO;
import br.com.orderservice.domain.entity.ClientJpaEntity;
import br.com.orderservice.domain.entity.OrderJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", qualifiedByName = "mapClientIdToClient", source = "clientId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", expression = "java(br.com.orderservice.domain.enumeration.OrderStatus.PENDING_PAYMENT)")
    OrderJpaEntity toEntity(OrderInputDTO dto);

    @Mapping(target = "clientName", ignore = true)
    OrderOutputDTO toDto(OrderJpaEntity entity);

    @Named("mapClientIdToClient")
    static ClientJpaEntity mapClientIdToClient(java.util.UUID clientId) {
        ClientJpaEntity client = new ClientJpaEntity();
        client.setId(clientId);
        return client;
    }

}
