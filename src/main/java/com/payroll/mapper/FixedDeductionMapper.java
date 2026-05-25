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
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "code",        ignore = true)
    FixedDeduction toEntity(FixedDeductionRequestDTO requestDTO);

    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.userName")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.userName")
    FixedDeductionResponseDTO toResponseDTO(FixedDeduction entity);

    List<FixedDeductionResponseDTO> toResponseDTOList(List<FixedDeduction> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    void updateEntityFromDTO(FixedDeductionRequestDTO requestDTO, @MappingTarget FixedDeduction entity);
}
