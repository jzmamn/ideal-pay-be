package com.payroll.mapper;

import com.payroll.dto.request.DepartmentRequestDTO;
import com.payroll.dto.response.DepartmentResponseDTO;
import com.payroll.entity.Department;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DepartmentMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "code",         ignore = true)
    @Mapping(target = "isActive",     ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    Department toEntity(DepartmentRequestDTO requestDTO);

    @Mapping(target = "createdById",       source = "createdBy.id")
    @Mapping(target = "createdByCode",     source = "createdBy.code")
    @Mapping(target = "createdByUserName", source = "createdBy.userName")
    @Mapping(target = "modifiedById",      source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",    source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName",source = "modifiedBy.userName")
    DepartmentResponseDTO toResponseDTO(Department entity);

    List<DepartmentResponseDTO> toResponseDTOList(List<Department> entities);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "code",         ignore = true)
    @Mapping(target = "isActive",     ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    void updateEntityFromDTO(DepartmentRequestDTO requestDTO, @MappingTarget Department entity);
}
