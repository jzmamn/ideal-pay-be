package com.payroll.mapper;

import com.payroll.dto.request.FixedDeductionRequestDTO;
import com.payroll.dto.response.FixedDeductionResponseDTO;
import com.payroll.entity.FixedDeduction;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FixedDeductionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    FixedDeduction toEntity(FixedDeductionRequestDTO requestDTO);

    FixedDeductionResponseDTO toResponseDTO(FixedDeduction entity);

    List<FixedDeductionResponseDTO> toResponseDTOList(List<FixedDeduction> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDTO(FixedDeductionRequestDTO requestDTO, @MappingTarget FixedDeduction entity);
}
