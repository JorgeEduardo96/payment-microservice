package br.com.orderservice.mapper;


import br.com.orderservice.domain.dto.ClientEventDTO;
import br.com.orderservice.domain.entity.ClientJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "orders", ignore = true)
    ClientJpaEntity toEntity(ClientEventDTO dto);

    ClientEventDTO toDTO(ClientJpaEntity entity);

}
