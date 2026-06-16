package com.payroll.mapper;

import com.payroll.dto.request.EmployeeSalaryAdvanceRequestDTO;
import com.payroll.dto.response.EmployeeSalaryAdvanceResponseDTO;
import com.payroll.entity.EmployeeSalaryAdvance;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeSalaryAdvanceMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "employee",     ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    EmployeeSalaryAdvance toEntity(EmployeeSalaryAdvanceRequestDTO requestDTO);

    @Mapping(target = "empId",              source = "employee.id")
    @Mapping(target = "empCode",            source = "employee.employeeNo")
    @Mapping(target = "empName",            source = "employee.payrollName")
    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.username")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.username")
    EmployeeSalaryAdvanceResponseDTO toResponseDTO(EmployeeSalaryAdvance entity);

    List<EmployeeSalaryAdvanceResponseDTO> toResponseDTOList(List<EmployeeSalaryAdvance> entities);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "employee",     ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    void updateEntityFromDTO(EmployeeSalaryAdvanceRequestDTO requestDTO, @MappingTarget EmployeeSalaryAdvance entity);
}
