package me.sample.mapper;

import me.sample.dto.ApplicationDTO;
import me.sample.domain.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ApplicationMapper {

    ApplicationDTO toDto(Application entity);

    @Mapping(target = "company", qualifiedBy = OnlyId.class)
    Application toEntity(ApplicationDTO dto);

    @NoNestedEntity
    @Mapping(target = "company", ignore = true)
    ApplicationDTO toDtoNoNested(Application entity);

    @OnlyId
    default Application toEntityOnlyId(ApplicationDTO entity) {
        return Application.builder()
                .id(entity.getId())
                .build();
    }
}
