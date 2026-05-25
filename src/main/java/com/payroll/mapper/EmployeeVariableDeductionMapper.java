package com.payroll.mapper;

import com.payroll.dto.request.EmployeeVariableDeductionRequestDTO;
import com.payroll.dto.response.EmployeeVariableDeductionResponseDTO;
import com.payroll.entity.EmployeeVariableDeduction;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeVariableDeductionMapper {

    @Mapping(target = "id",                ignore = true)
    @Mapping(target = "employee",          ignore = true)
    @Mapping(target = "variableDeduction", ignore = true)
    @Mapping(target = "createdBy",         ignore = true)
    @Mapping(target = "modifiedBy",        ignore = true)
    @Mapping(target = "createdDate",       ignore = true)
    @Mapping(target = "modifiedDate",      ignore = true)
    EmployeeVariableDeduction toEntity(EmployeeVariableDeductionRequestDTO requestDTO);

    @Mapping(target = "empId",              source = "employee.id")
    @Mapping(target = "empCode",            source = "employee.employeeNo")
    @Mapping(target = "empName",            source = "employee.payrollName")
    @Mapping(target = "vdId",               source = "variableDeduction.id")
    @Mapping(target = "vdCode",             source = "variableDeduction.code")
    @Mapping(target = "vdName",             source = "variableDeduction.name")
    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.userName")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.userName")
    EmployeeVariableDeductionResponseDTO toResponseDTO(EmployeeVariableDeduction entity);

    List<EmployeeVariableDeductionResponseDTO> toResponseDTOList(List<EmployeeVariableDeduction> entities);

    @Mapping(target = "id",                ignore = true)
    @Mapping(target = "employee",          ignore = true)
    @Mapping(target = "variableDeduction", ignore = true)
    @Mapping(target = "createdBy",         ignore = true)
    @Mapping(target = "modifiedBy",        ignore = true)
    @Mapping(target = "createdDate",       ignore = true)
    @Mapping(target = "modifiedDate",      ignore = true)
    void updateEntityFromDTO(EmployeeVariableDeductionRequestDTO requestDTO, @MappingTarget EmployeeVariableDeduction entity);
}
