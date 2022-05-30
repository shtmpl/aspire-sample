package me.sample.mapper;

import me.sample.dto.NotificationDTO;
import me.sample.domain.Notification;
import me.sample.domain.data.NotificationData;
import me.sample.domain.data.NotificationStateData;
import me.sample.web.rest.request.NotificationSendRequest;
import me.sample.web.rest.response.NotificationSendResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    NotificationDTO toDto(Notification entity);

    NotificationData toData(NotificationSendRequest request);

    NotificationSendResponse toResponse(NotificationStateData data);
}
