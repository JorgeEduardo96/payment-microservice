package br.com.notificationservice.domain.mapper;

import br.com.notificationservice.domain.dto.ClientEventDTO;
import br.com.notificationservice.domain.entity.ClientJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClientMapper {


    ClientJpaEntity toEntity(ClientEventDTO dto);

}
