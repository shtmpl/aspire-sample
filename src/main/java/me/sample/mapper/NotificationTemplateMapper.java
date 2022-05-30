package me.sample.mapper;

import me.sample.dto.NotificationTemplateDTO;
import me.sample.domain.Campaign;
import me.sample.domain.NotificationTemplate;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationTemplateMapper {
    NotificationTemplateDTO toDto(NotificationTemplate entity);

    @AfterMapping
    default void setDisseminationIds(@MappingTarget NotificationTemplateDTO result,
                                     NotificationTemplate notificationTemplate) {
        if (notificationTemplate == null) {
            return;
        }

        result.setDisseminationIds(notificationTemplate.getCampaigns().stream()
                .map(Campaign::getId)
                .collect(Collectors.toList()));
    }

    NotificationTemplate toEntity(NotificationTemplateDTO dto);

    @OnlyId
    default NotificationTemplate toEntityOnlyId(NotificationTemplateDTO dto) {
        return NotificationTemplate.builder()
                .id(dto.getId())
                .build();
    }
}
