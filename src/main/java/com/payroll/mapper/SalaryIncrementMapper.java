package com.payroll.mapper;

import com.payroll.dto.request.SalaryIncrementRequest;
import com.payroll.dto.response.SalaryIncrementResponse;
import com.payroll.entity.SalaryIncrement;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        uses = SalaryIncrementDetailMapper.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SalaryIncrementMapper {

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "status",      ignore = true)
    @Mapping(target = "details",     ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "modifiedBy",  ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate",ignore = true)
    SalaryIncrement toEntity(SalaryIncrementRequest request);

    @Mapping(target = "createdById",       source = "createdBy.id")
    @Mapping(target = "createdByCode",     source = "createdBy.code")
    @Mapping(target = "createdByUserName", source = "createdBy.userName")
    @Mapping(target = "modifiedById",      source = "modifiedBy.id")
    @Mapping(target = "modifiedByCode",    source = "modifiedBy.code")
    @Mapping(target = "modifiedByUserName",source = "modifiedBy.userName")
    SalaryIncrementResponse toResponse(SalaryIncrement entity);
}
