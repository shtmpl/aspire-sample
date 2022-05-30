package me.sample.mapper;

import me.sample.dto.CompanyDTO;
import me.sample.domain.Company;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompanyMapper {

    CompanyDTO toDto(Company entity);

    Company toEntity(CompanyDTO dto);

    @OnlyId
    default Company toEntityOnlyId(CompanyDTO dto) {
        return Company.builder()
                .id(dto.getId())
                .build();
    }
}
