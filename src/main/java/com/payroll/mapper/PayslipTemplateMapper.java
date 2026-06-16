package com.payroll.mapper;

import com.payroll.dto.request.PayslipTemplateRequestDTO;
import com.payroll.dto.response.PayslipTemplateResponseDTO;
import com.payroll.entity.PayslipTemplate;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PayslipTemplateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    PayslipTemplate toEntity(PayslipTemplateRequestDTO requestDTO);

    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByUserName", source = "createdBy.username")
    @Mapping(target = "modifiedById", source = "modifiedBy.id")
    @Mapping(target = "modifiedByUserName", source = "modifiedBy.username")
    PayslipTemplateResponseDTO toResponseDTO(PayslipTemplate entity);

    List<PayslipTemplateResponseDTO> toResponseDTOList(List<PayslipTemplate> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    void updateEntityFromDTO(PayslipTemplateRequestDTO requestDTO, @MappingTarget PayslipTemplate entity);
}
