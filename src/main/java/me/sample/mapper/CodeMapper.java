package me.sample.mapper;

import me.sample.dto.CodeDTO;
import me.sample.domain.Code;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CodeMapper {
    CodeDTO toDto(Code entity);

    @TypeAndValue
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "access", ignore = true)
    @Mapping(target = "isUsed", ignore = true)
    CodeDTO toDtoTypeAndValue(Code entity);

    Code toEntity(CodeDTO dto);
}
