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
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "code",        ignore = true)
    VariableAllowance toEntity(VariableAllowanceRequestDTO requestDTO);

    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.userName")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.userName")
    VariableAllowanceResponseDTO toResponseDTO(VariableAllowance entity);

    List<VariableAllowanceResponseDTO> toResponseDTOList(List<VariableAllowance> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    void updateEntityFromDTO(VariableAllowanceRequestDTO requestDTO, @MappingTarget VariableAllowance entity);
}
