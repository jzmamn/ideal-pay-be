package com.payroll.mapper;

import com.payroll.dto.request.SalaryIncrementDetailRequest;
import com.payroll.dto.response.SalaryIncrementDetailResponse;
import com.payroll.entity.SalaryIncrementDetail;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        uses = SalaryIncrementFaMapper.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SalaryIncrementDetailMapper {

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "increment",   ignore = true)
    @Mapping(target = "employee",    ignore = true)
    @Mapping(target = "isExported",  ignore = true)
    @Mapping(target = "exportedDate",ignore = true)
    @Mapping(target = "faIncrements",ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "modifiedBy",  ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate",ignore = true)
    SalaryIncrementDetail toEntity(SalaryIncrementDetailRequest request);

    @Mapping(target = "incrementId",       source = "increment.id")
    @Mapping(target = "empId",             source = "employee.id")
    @Mapping(target = "empCode",           source = "employee.employeeNo")
    @Mapping(target = "empName",           source = "employee.payrollName")
    @Mapping(target = "designationName",   source = "employee.designation.name")
    @Mapping(target = "branchName",        source = "employee.branch.name")
    @Mapping(target = "createdById",       source = "createdBy.id")
    @Mapping(target = "createdByCode",     source = "createdBy.code")
    @Mapping(target = "createdByUserName", source = "createdBy.username")
    @Mapping(target = "modifiedById",      source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",    source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName",source = "modifiedBy.username")
    SalaryIncrementDetailResponse toResponse(SalaryIncrementDetail entity);
}
