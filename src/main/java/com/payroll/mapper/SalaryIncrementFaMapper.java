package com.payroll.mapper;

import com.payroll.dto.request.SalaryIncrementFaRequest;
import com.payroll.dto.response.SalaryIncrementFaResponse;
import com.payroll.entity.SalaryIncrementFa;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SalaryIncrementFaMapper {

    @Mapping(target = "id",            ignore = true)
    @Mapping(target = "detail",        ignore = true)
    @Mapping(target = "fixedAllowance",ignore = true)
    @Mapping(target = "createdBy",     ignore = true)
    @Mapping(target = "modifiedBy",    ignore = true)
    @Mapping(target = "createdDate",   ignore = true)
    @Mapping(target = "modifiedDate",  ignore = true)
    SalaryIncrementFa toEntity(SalaryIncrementFaRequest request);

    @Mapping(target = "faId",              source = "fixedAllowance.id")
    @Mapping(target = "faCode",            source = "fixedAllowance.code")
    @Mapping(target = "faName",            source = "fixedAllowance.name")
    @Mapping(target = "createdById",       source = "createdBy.id")
    @Mapping(target = "createdByCode",     source = "createdBy.code")
    @Mapping(target = "createdByUserName", source = "createdBy.userName")
    @Mapping(target = "modifiedById",      source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",    source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName",source = "modifiedBy.userName")
    SalaryIncrementFaResponse toResponse(SalaryIncrementFa entity);
}
