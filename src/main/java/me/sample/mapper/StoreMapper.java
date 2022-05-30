package me.sample.mapper;

import me.sample.dto.StoreDTO;
import me.sample.domain.Store;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StoreMapper {

    StoreDTO toDto(Store entity);

    @Mappings({
            @Mapping(target = "partner", qualifiedBy = OnlyId.class)
    })
    Store toEntity(StoreDTO dto);

    @OnlyId
    default Store toEntityOnlyId(StoreDTO dto) {
        return Store.builder()
                .id(dto.getId())
                .build();
    }

    default String map(UUID value) {
        if (value == null) {
            return null;
        }

        return value.toString();
    }
}
