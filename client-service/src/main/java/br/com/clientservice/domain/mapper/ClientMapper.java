package br.com.clientservice.domain.mapper;

import br.com.clientservice.domain.dto.ClientInputDTO;
import br.com.clientservice.domain.dto.ClientOutputDTO;
import br.com.clientservice.domain.entity.ClientJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ClientJpaEntity toEntity(UUID id, ClientInputDTO dto);

    ClientOutputDTO toDTO(ClientJpaEntity entity);

}
