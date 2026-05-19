package com.payroll.mapper;

import com.payroll.dto.request.EmpTypeRequestDTO;
import com.payroll.dto.response.EmpTypeResponseDTO;
import com.payroll.entity.EmpType;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmpTypeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    EmpType toEntity(EmpTypeRequestDTO requestDTO);

    EmpTypeResponseDTO toResponseDTO(EmpType entity);

    List<EmpTypeResponseDTO> toResponseDTOList(List<EmpType> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDTO(EmpTypeRequestDTO requestDTO, @MappingTarget EmpType entity);
}
