package com.payroll.mapper;

import com.payroll.dto.request.VariableAllowanceRequestDTO;
import com.payroll.dto.response.VariableAllowanceResponseDTO;
import com.payroll.entity.VariableAllowance;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VariableAllowanceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    VariableAllowance toEntity(VariableAllowanceRequestDTO requestDTO);

    VariableAllowanceResponseDTO toResponseDTO(VariableAllowance entity);

    List<VariableAllowanceResponseDTO> toResponseDTOList(List<VariableAllowance> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDTO(VariableAllowanceRequestDTO requestDTO, @MappingTarget VariableAllowance entity);
}
