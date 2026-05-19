package com.payroll.mapper;

import com.payroll.dto.request.UsrRequestDTO;
import com.payroll.dto.response.UsrResponseDTO;
import com.payroll.entity.Usr;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UsrMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    Usr toEntity(UsrRequestDTO requestDTO);

    // Exclude password from response mapping
    @Mapping(target = "id", source = "id")
    UsrResponseDTO toResponseDTO(Usr entity);

    List<UsrResponseDTO> toResponseDTOList(List<Usr> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEntityFromDTO(UsrRequestDTO requestDTO, @MappingTarget Usr entity);
}
