package me.sample.mapper;

import me.sample.dto.ApplicationDTO;
import me.sample.dto.PartnerDTO;
import me.sample.dto.PartnerImportDTO;
import me.sample.domain.Application;
import me.sample.domain.Partner;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PartnerMapper {

    @Mapping(target = "storeCount", expression = "java(entity.getStores().size())")
    PartnerDTO toDto(Partner entity);

    @Mapping(target = "company", ignore = true)
    PartnerDTO toDtoNoCompany(Partner entity);

    @Mapping(target = "company", qualifiedBy = OnlyId.class)
    Partner toEntity(PartnerDTO dto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "stores", ignore = true)
    @Mapping(target = "name", source = "title")
    Partner toEntity(PartnerImportDTO importDTO);

    default String map(UUID value) {
        if (value == null) {
            return null;
        }

        return value.toString();
    }

    @OnlyId
    default Application toEntityOnlyId(ApplicationDTO dto) {
        return Application.builder()
                .id(dto.getId())
                .build();
    }
}
