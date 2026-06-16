package com.payroll.mapper;

import com.payroll.dto.request.DesignationRequestDTO;
import com.payroll.dto.response.DesignationResponseDTO;
import com.payroll.entity.Designation;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DesignationMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "code",         ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    Designation toEntity(DesignationRequestDTO requestDTO);

    @Mapping(target = "createdById",       source = "createdBy.id")
    @Mapping(target = "createdByCode",     source = "createdBy.code")
    @Mapping(target = "createdByUserName", source = "createdBy.username")
    @Mapping(target = "modifiedById",      source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",    source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName",source = "modifiedBy.username")
    DesignationResponseDTO toResponseDTO(Designation entity);

    List<DesignationResponseDTO> toResponseDTOList(List<Designation> entities);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "code",         ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    void updateEntityFromDTO(DesignationRequestDTO requestDTO, @MappingTarget Designation entity);
}
