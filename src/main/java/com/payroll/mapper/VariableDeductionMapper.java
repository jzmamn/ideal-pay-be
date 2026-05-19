package com.payroll.mapper;

import com.payroll.dto.request.VariableDeductionRequestDTO;
import com.payroll.dto.response.VariableDeductionResponseDTO;
import com.payroll.entity.VariableDeduction;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VariableDeductionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    VariableDeduction toEntity(VariableDeductionRequestDTO requestDTO);

    VariableDeductionResponseDTO toResponseDTO(VariableDeduction entity);

    List<VariableDeductionResponseDTO> toResponseDTOList(List<VariableDeduction> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDTO(VariableDeductionRequestDTO requestDTO, @MappingTarget VariableDeduction entity);
}
