package com.payroll.mapper;

import com.payroll.dto.request.DesignationRequestDTO;
import com.payroll.dto.response.DesignationResponseDTO;
import com.payroll.entity.Designation;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DesignationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    Designation toEntity(DesignationRequestDTO requestDTO);

    DesignationResponseDTO toResponseDTO(Designation entity);

    List<DesignationResponseDTO> toResponseDTOList(List<Designation> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDTO(DesignationRequestDTO requestDTO, @MappingTarget Designation entity);
}
