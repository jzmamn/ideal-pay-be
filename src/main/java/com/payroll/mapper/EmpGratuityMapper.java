package com.payroll.mapper;

import com.payroll.dto.response.EmpGratuityResponse;
import com.payroll.entity.EmpGratuity;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmpGratuityMapper {

    @Mapping(target = "empId",            source = "employee.id")
    @Mapping(target = "empCode",          source = "employee.employeeNo")
    @Mapping(target = "empName",          source = "employee.payrollName")
    @Mapping(target = "designationName",  source = "employee.designation.name")
    @Mapping(target = "branchName",       source = "employee.branch.name")
    @Mapping(target = "createdById",       source = "createdBy.id")
    @Mapping(target = "createdByCode",     source = "createdBy.code")
    @Mapping(target = "createdByUserName", source = "createdBy.userName")
    @Mapping(target = "modifiedById",      source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",    source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName",source = "modifiedBy.userName")
    EmpGratuityResponse toResponse(EmpGratuity entity);
}
