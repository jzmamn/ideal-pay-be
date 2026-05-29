package com.payroll.mapper;

import com.payroll.dto.request.DistrictRequestDTO;
import com.payroll.dto.response.DistrictResponseDTO;
import com.payroll.entity.District;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DistrictMapper {

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "code",        ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "modifiedBy",  ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate",ignore = true)
    District toEntity(DistrictRequestDTO requestDTO);

    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.userName")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.userName")
    DistrictResponseDTO toResponseDTO(District entity);

    List<DistrictResponseDTO> toResponseDTOList(List<District> entities);

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "code",        ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "modifiedBy",  ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate",ignore = true)
    void updateEntityFromDTO(DistrictRequestDTO requestDTO, @MappingTarget District entity);
}
