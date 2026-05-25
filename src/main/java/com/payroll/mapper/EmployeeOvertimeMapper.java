package com.payroll.mapper;

import com.payroll.dto.request.EmployeeOvertimeRequestDTO;
import com.payroll.dto.response.EmployeeOvertimeResponseDTO;
import com.payroll.entity.EmployeeOvertime;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeOvertimeMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "employee",     ignore = true)
    @Mapping(target = "overtime",     ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    EmployeeOvertime toEntity(EmployeeOvertimeRequestDTO requestDTO);

    @Mapping(target = "empId",              source = "employee.id")
    @Mapping(target = "empCode",            source = "employee.employeeNo")
    @Mapping(target = "empName",            source = "employee.payrollName")
    @Mapping(target = "overtimeId",         source = "overtime.id")
    @Mapping(target = "overtimeCode",       source = "overtime.code")
    @Mapping(target = "overtimeName",       source = "overtime.name")
    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.userName")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.userName")
    EmployeeOvertimeResponseDTO toResponseDTO(EmployeeOvertime entity);

    List<EmployeeOvertimeResponseDTO> toResponseDTOList(List<EmployeeOvertime> entities);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "employee",     ignore = true)
    @Mapping(target = "overtime",     ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    void updateEntityFromDTO(EmployeeOvertimeRequestDTO requestDTO, @MappingTarget EmployeeOvertime entity);
}
