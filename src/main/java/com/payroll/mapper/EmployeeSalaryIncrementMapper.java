package com.payroll.mapper;

import com.payroll.dto.request.EmployeeSalaryIncrementRequestDTO;
import com.payroll.dto.response.EmployeeSalaryIncrementResponseDTO;
import com.payroll.entity.EmployeeSalaryIncrement;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeSalaryIncrementMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "employee",     ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    EmployeeSalaryIncrement toEntity(EmployeeSalaryIncrementRequestDTO requestDTO);

    @Mapping(target = "empId",              source = "employee.id")
    @Mapping(target = "empCode",            source = "employee.employeeNo")
    @Mapping(target = "empName",            source = "employee.payrollName")
    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.userName")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.userName")
    EmployeeSalaryIncrementResponseDTO toResponseDTO(EmployeeSalaryIncrement entity);

    List<EmployeeSalaryIncrementResponseDTO> toResponseDTOList(List<EmployeeSalaryIncrement> entities);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "employee",     ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    void updateEntityFromDTO(EmployeeSalaryIncrementRequestDTO requestDTO, @MappingTarget EmployeeSalaryIncrement entity);
}
