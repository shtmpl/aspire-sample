package me.sample.mapper;

import me.sample.dto.PromoDTO;
import me.sample.domain.Promo;
import me.sample.domain.Source;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PromoMapper {

    PromoDTO toDto(Promo promo);

    @Mappings({
            @Mapping(target = "source", constant = Source.Values.LOCAL)
    })
    Promo toEntity(PromoDTO dto);
}
