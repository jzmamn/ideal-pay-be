package com.payroll.mapper;

import com.payroll.dto.request.FixedAllowanceRequestDTO;
import com.payroll.dto.response.FixedAllowanceResponseDTO;
import com.payroll.entity.FixedAllowance;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FixedAllowanceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "code",        ignore = true)
    FixedAllowance toEntity(FixedAllowanceRequestDTO requestDTO);

    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.userName")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.userName")
    FixedAllowanceResponseDTO toResponseDTO(FixedAllowance entity);

    List<FixedAllowanceResponseDTO> toResponseDTOList(List<FixedAllowance> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    void updateEntityFromDTO(FixedAllowanceRequestDTO requestDTO, @MappingTarget FixedAllowance entity);
}
