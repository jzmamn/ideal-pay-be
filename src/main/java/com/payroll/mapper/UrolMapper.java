package com.payroll.mapper;

import com.payroll.dto.request.UrolRequestDTO;
import com.payroll.dto.response.UrolResponseDTO;
import com.payroll.entity.Urol;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UrolMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "code",         ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    Urol toEntity(UrolRequestDTO requestDTO);

    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.username")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.username")
    UrolResponseDTO toResponseDTO(Urol entity);

    List<UrolResponseDTO> toResponseDTOList(List<Urol> entities);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "code",         ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    void updateEntityFromDTO(UrolRequestDTO requestDTO, @MappingTarget Urol entity);
}
