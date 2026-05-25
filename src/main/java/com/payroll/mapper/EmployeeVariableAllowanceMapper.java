package com.payroll.mapper;

import com.payroll.dto.request.EmployeeVariableAllowanceRequestDTO;
import com.payroll.dto.response.EmployeeVariableAllowanceResponseDTO;
import com.payroll.entity.EmployeeVariableAllowance;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeVariableAllowanceMapper {

    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "employee",         ignore = true)
    @Mapping(target = "variableAllowance",ignore = true)
    @Mapping(target = "createdBy",        ignore = true)
    @Mapping(target = "modifiedBy",       ignore = true)
    @Mapping(target = "createdDate",      ignore = true)
    @Mapping(target = "modifiedDate",     ignore = true)
    EmployeeVariableAllowance toEntity(EmployeeVariableAllowanceRequestDTO requestDTO);

    @Mapping(target = "empId",              source = "employee.id")
    @Mapping(target = "empCode",            source = "employee.employeeNo")
    @Mapping(target = "empName",            source = "employee.payrollName")
    @Mapping(target = "vaId",               source = "variableAllowance.id")
    @Mapping(target = "vaCode",             source = "variableAllowance.code")
    @Mapping(target = "vaName",             source = "variableAllowance.name")
    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.userName")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.userName")
    EmployeeVariableAllowanceResponseDTO toResponseDTO(EmployeeVariableAllowance entity);

    List<EmployeeVariableAllowanceResponseDTO> toResponseDTOList(List<EmployeeVariableAllowance> entities);

    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "employee",         ignore = true)
    @Mapping(target = "variableAllowance",ignore = true)
    @Mapping(target = "createdBy",        ignore = true)
    @Mapping(target = "modifiedBy",       ignore = true)
    @Mapping(target = "createdDate",      ignore = true)
    @Mapping(target = "modifiedDate",     ignore = true)
    void updateEntityFromDTO(EmployeeVariableAllowanceRequestDTO requestDTO, @MappingTarget EmployeeVariableAllowance entity);
}
