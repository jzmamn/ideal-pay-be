package com.payroll.mapper;

import com.payroll.dto.request.EmployeeFixedAllowanceRequestDTO;
import com.payroll.dto.response.EmployeeFixedAllowanceResponseDTO;
import com.payroll.entity.EmployeeFixedAllowance;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeFixedAllowanceMapper {

    @Mapping(target = "id",                ignore = true)
    @Mapping(target = "employee",          ignore = true)
    @Mapping(target = "fixedAllowance",    ignore = true)
    @Mapping(target = "createdBy",         ignore = true)
    @Mapping(target = "modifiedBy",        ignore = true)
    @Mapping(target = "createdDate",       ignore = true)
    @Mapping(target = "modifiedDate",      ignore = true)
    @Mapping(target = "formulaCalculated", ignore = true)
    @Mapping(target = "importLogId",       ignore = true)
    EmployeeFixedAllowance toEntity(EmployeeFixedAllowanceRequestDTO requestDTO);

    @Mapping(target = "empId",             source = "employee.id")
    @Mapping(target = "empCode",           source = "employee.employeeNo")
    @Mapping(target = "empName",           source = "employee.payrollName")
    @Mapping(target = "faId",              source = "fixedAllowance.id")
    @Mapping(target = "faCode",            source = "fixedAllowance.code")
    @Mapping(target = "faName",            source = "fixedAllowance.name")
    @Mapping(target = "formulaCalculated", source = "formulaCalculated")
    @Mapping(target = "createdById",       source = "createdBy.id")
    @Mapping(target = "createdByCode",     source = "createdBy.code")
    @Mapping(target = "createdByUserName", source = "createdBy.username")
    @Mapping(target = "modifiedById",      source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",    source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName",source = "modifiedBy.username")
    @Mapping(target = "importLogId",       source = "importLogId")
    EmployeeFixedAllowanceResponseDTO toResponseDTO(EmployeeFixedAllowance entity);

    List<EmployeeFixedAllowanceResponseDTO> toResponseDTOList(List<EmployeeFixedAllowance> entities);

    @Mapping(target = "id",                ignore = true)
    @Mapping(target = "employee",          ignore = true)
    @Mapping(target = "fixedAllowance",    ignore = true)
    @Mapping(target = "createdBy",         ignore = true)
    @Mapping(target = "modifiedBy",        ignore = true)
    @Mapping(target = "createdDate",       ignore = true)
    @Mapping(target = "modifiedDate",      ignore = true)
    @Mapping(target = "formulaCalculated", ignore = true)
    @Mapping(target = "importLogId",       ignore = true)
    void updateEntityFromDTO(EmployeeFixedAllowanceRequestDTO requestDTO, @MappingTarget EmployeeFixedAllowance entity);
}
