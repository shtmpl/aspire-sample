package me.sample.mapper;

import me.sample.dto.CompanyAuthorityDto;
import me.sample.domain.CompanyAuthority;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompanyAuthorityMapper {
    CompanyAuthorityDto toDto(CompanyAuthority entity);

    CompanyAuthority toEntity(CompanyAuthorityDto dto);
}
