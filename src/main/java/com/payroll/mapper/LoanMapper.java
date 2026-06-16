package com.payroll.mapper;

import com.payroll.dto.response.LoanResponseDTO;
import com.payroll.entity.Loan;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LoanMapper {

    @Mapping(target = "createdById",       source = "createdBy.id")
    @Mapping(target = "createdByUserName", source = "createdBy.username")
    @Mapping(target = "modifiedById",      source = "modifiedBy.id")
    @Mapping(target = "modifiedByUserName",source = "modifiedBy.username")
    LoanResponseDTO toResponseDTO(Loan entity);

    List<LoanResponseDTO> toResponseDTOList(List<Loan> entities);
}
