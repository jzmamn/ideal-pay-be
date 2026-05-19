package com.payroll.mapper;

import com.payroll.dto.request.GradeRequestDTO;
import com.payroll.dto.response.GradeResponseDTO;
import com.payroll.entity.Grade;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GradeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    Grade toEntity(GradeRequestDTO requestDTO);

    GradeResponseDTO toResponseDTO(Grade entity);

    List<GradeResponseDTO> toResponseDTOList(List<Grade> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDTO(GradeRequestDTO requestDTO, @MappingTarget Grade entity);
}
