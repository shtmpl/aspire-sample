package me.sample.mapper;

import me.sample.dto.PropertyDTO;
import me.sample.domain.Property;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PropertyMapper {

    PropertyDTO toDto(Property entity);

    Property toEntity(PropertyDTO dto);
}
