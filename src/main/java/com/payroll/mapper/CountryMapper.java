package com.payroll.mapper;

import com.payroll.dto.request.CountryRequestDTO;
import com.payroll.dto.response.CountryResponseDTO;
import com.payroll.entity.Country;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CountryMapper {

    @Mapping(target = "id",   ignore = true)
    @Mapping(target = "code", ignore = true)
    Country toEntity(CountryRequestDTO requestDTO);

    CountryResponseDTO toResponseDTO(Country entity);

    List<CountryResponseDTO> toResponseDTOList(List<Country> entities);

    @Mapping(target = "id",   ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateEntityFromDTO(CountryRequestDTO requestDTO, @MappingTarget Country entity);
}