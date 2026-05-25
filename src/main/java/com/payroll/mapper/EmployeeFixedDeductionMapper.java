package com.payroll.mapper;

import com.payroll.dto.request.EmployeeFixedDeductionRequestDTO;
import com.payroll.dto.response.EmployeeFixedDeductionResponseDTO;
import com.payroll.entity.EmployeeFixedDeduction;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeFixedDeductionMapper {

    @Mapping(target = "id",             ignore = true)
    @Mapping(target = "employee",       ignore = true)
    @Mapping(target = "fixedDeduction", ignore = true)
    @Mapping(target = "createdBy",      ignore = true)
    @Mapping(target = "modifiedBy",     ignore = true)
    @Mapping(target = "createdDate",    ignore = true)
    @Mapping(target = "modifiedDate",   ignore = true)
    EmployeeFixedDeduction toEntity(EmployeeFixedDeductionRequestDTO requestDTO);

    @Mapping(target = "empId",              source = "employee.id")
    @Mapping(target = "empCode",            source = "employee.employeeNo")
    @Mapping(target = "empName",            source = "employee.payrollName")
    @Mapping(target = "fdId",               source = "fixedDeduction.id")
    @Mapping(target = "fdCode",             source = "fixedDeduction.code")
    @Mapping(target = "fdName",             source = "fixedDeduction.name")
    @Mapping(target = "createdById",        source = "createdBy.id")
    @Mapping(target = "createdByCode",      source = "createdBy.code")
    @Mapping(target = "createdByUserName",  source = "createdBy.userName")
    @Mapping(target = "modifiedById",       source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",     source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.userName")
    EmployeeFixedDeductionResponseDTO toResponseDTO(EmployeeFixedDeduction entity);

    List<EmployeeFixedDeductionResponseDTO> toResponseDTOList(List<EmployeeFixedDeduction> entities);

    @Mapping(target = "id",             ignore = true)
    @Mapping(target = "employee",       ignore = true)
    @Mapping(target = "fixedDeduction", ignore = true)
    @Mapping(target = "createdBy",      ignore = true)
    @Mapping(target = "modifiedBy",     ignore = true)
    @Mapping(target = "createdDate",    ignore = true)
    @Mapping(target = "modifiedDate",   ignore = true)
    void updateEntityFromDTO(EmployeeFixedDeductionRequestDTO requestDTO, @MappingTarget EmployeeFixedDeduction entity);
}
