package com.payroll.mapper;

import com.payroll.dto.request.NopayDaysRequestDTO;
import com.payroll.dto.response.NopayDaysResponseDTO;
import com.payroll.entity.NopayDays;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NopayDaysMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    NopayDays toEntity(NopayDaysRequestDTO requestDTO);

    NopayDaysResponseDTO toResponseDTO(NopayDays entity);

    List<NopayDaysResponseDTO> toResponseDTOList(List<NopayDays> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDTO(NopayDaysRequestDTO requestDTO, @MappingTarget NopayDays entity);
}
