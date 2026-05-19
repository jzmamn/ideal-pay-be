package com.payroll.mapper;

import com.payroll.dto.request.CompanyRequestDTO;
import com.payroll.dto.response.CompanyResponseDTO;
import com.payroll.entity.Company;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompanyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    Company toEntity(CompanyRequestDTO requestDTO);

    CompanyResponseDTO toResponseDTO(Company entity);

    List<CompanyResponseDTO> toResponseDTOList(List<Company> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDTO(CompanyRequestDTO requestDTO, @MappingTarget Company entity);
}
