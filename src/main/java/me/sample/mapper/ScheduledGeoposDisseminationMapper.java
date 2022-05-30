package me.sample.mapper;

import me.sample.dto.ScheduledGeoposDisseminationDTO;
import me.sample.domain.DisseminationSchedule;
import me.sample.domain.ScheduledGeoposDissemination;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        uses = {
                StoreMapper.class,
                NotificationTemplateMapper.class,
                CodeMapper.class,
                CategoryMapper.class
        },
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ScheduledGeoposDisseminationMapper {

    ScheduledGeoposDisseminationDTO toDto(ScheduledGeoposDissemination entity);

    @AfterMapping
    default void setSchedule(@MappingTarget ScheduledGeoposDisseminationDTO result,
                             ScheduledGeoposDissemination dissemination) {
        result.setSchedule(DisseminationSchedule.fromCronExpression(dissemination.getCron()));
    }

    @NoStore
    @Mapping(target = "stores", ignore = true)
    ScheduledGeoposDisseminationDTO toDtoNoStore(ScheduledGeoposDissemination entity);

    @Mapping(target = "stores", qualifiedBy = OnlyId.class)
    ScheduledGeoposDissemination toEntity(ScheduledGeoposDisseminationDTO dto);

    @AfterMapping
    default void setCronExpression(@MappingTarget ScheduledGeoposDissemination result,
                                   ScheduledGeoposDisseminationDTO request) {
        result.setCron(request.getSchedule().getCronExpression());
    }
}
