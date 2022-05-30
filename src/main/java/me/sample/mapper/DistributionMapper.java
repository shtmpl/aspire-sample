package me.sample.mapper;

import me.sample.dto.DistributionDTO;
import me.sample.domain.DisseminationSchedule;
import me.sample.domain.Distribution;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DistributionMapper {

    DistributionDTO toDto(Distribution entity);

    @AfterMapping
    default void setSchedule(@MappingTarget DistributionDTO result,
                             Distribution dissemination) {
        result.setSchedule(DisseminationSchedule.fromCronExpression(dissemination.getCron()));
    }

    Distribution toEntity(DistributionDTO dto);

    @AfterMapping
    default void setCronExpression(@MappingTarget Distribution result,
                                   DistributionDTO request) {
        result.setCron(request.getSchedule().getCronExpression());
    }
}
