package com.payroll.mapper;

import com.payroll.dto.request.EmployeeLateRequestDTO;
import com.payroll.dto.response.EmployeeLateResponseDTO;
import com.payroll.entity.EmployeeLate;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeLateMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "rate",         ignore = true)
    @Mapping(target = "lateConfig",   ignore = true)
    @Mapping(target = "employee",     ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    EmployeeLate toEntity(EmployeeLateRequestDTO requestDTO);

    @Mapping(target = "empId",              source = "employee.id")
    @Mapping(target = "empCode",            source = "employee.employeeNo")
    @Mapping(target = "empName",            source = "employee.payrollName")
    @Mapping(target = "lateConfigId",       source = "lateConfig.id")
    @Mapping(target = "lateConfigCode",     source = "lateConfig.code")
    @Mapping(target = "lateConfigName",     source = "lateConfig.name")
    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.username")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.username")
    EmployeeLateResponseDTO toResponseDTO(EmployeeLate entity);

    List<EmployeeLateResponseDTO> toResponseDTOList(List<EmployeeLate> entities);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "rate",         ignore = true)
    @Mapping(target = "lateConfig",   ignore = true)
    @Mapping(target = "employee",     ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    void updateEntityFromDTO(EmployeeLateRequestDTO requestDTO, @MappingTarget EmployeeLate entity);
}
