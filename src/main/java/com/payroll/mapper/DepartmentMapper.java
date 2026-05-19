package com.payroll.mapper;

import com.payroll.dto.request.DepartmentRequestDTO;
import com.payroll.dto.response.DepartmentResponseDTO;
import com.payroll.entity.Department;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DepartmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    Department toEntity(DepartmentRequestDTO requestDTO);

    DepartmentResponseDTO toResponseDTO(Department entity);

    List<DepartmentResponseDTO> toResponseDTOList(List<Department> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDTO(DepartmentRequestDTO requestDTO, @MappingTarget Department entity);
}
