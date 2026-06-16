package com.payroll.mapper;

import com.payroll.dto.request.LateDeductionConfigRequestDTO;
import com.payroll.dto.response.LateDeductionConfigResponseDTO;
import com.payroll.entity.LateDeductionConfig;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LateDeductionConfigMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "code",         ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    LateDeductionConfig toEntity(LateDeductionConfigRequestDTO requestDTO);

    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.username")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.username")
    LateDeductionConfigResponseDTO toResponseDTO(LateDeductionConfig entity);

    List<LateDeductionConfigResponseDTO> toResponseDTOList(List<LateDeductionConfig> entities);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "code",         ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "formula", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    void updateEntityFromDTO(LateDeductionConfigRequestDTO requestDTO, @MappingTarget LateDeductionConfig entity);
}
