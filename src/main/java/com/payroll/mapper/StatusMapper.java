package com.payroll.mapper;

import com.payroll.dto.request.StatusRequestDTO;
import com.payroll.dto.response.StatusResponseDTO;
import com.payroll.entity.Status;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StatusMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "code",         ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    Status toEntity(StatusRequestDTO requestDTO);

    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.userName")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.userName")
    StatusResponseDTO toResponseDTO(Status entity);

    List<StatusResponseDTO> toResponseDTOList(List<Status> entities);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "code",         ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    void updateEntityFromDTO(StatusRequestDTO requestDTO, @MappingTarget Status entity);
}
