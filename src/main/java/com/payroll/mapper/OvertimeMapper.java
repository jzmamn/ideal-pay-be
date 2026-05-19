package com.payroll.mapper;

import com.payroll.dto.request.OvertimeRequestDTO;
import com.payroll.dto.response.OvertimeResponseDTO;
import com.payroll.entity.Overtime;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OvertimeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    Overtime toEntity(OvertimeRequestDTO requestDTO);

    OvertimeResponseDTO toResponseDTO(Overtime entity);

    List<OvertimeResponseDTO> toResponseDTOList(List<Overtime> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDTO(OvertimeRequestDTO requestDTO, @MappingTarget Overtime entity);
}
