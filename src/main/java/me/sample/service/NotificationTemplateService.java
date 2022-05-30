package me.sample.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import me.sample.domain.NotificationTemplate;

import java.util.Optional;
import java.util.UUID;

public interface NotificationTemplateService {

    Page<NotificationTemplate> findNotificationTemplates(Specification<NotificationTemplate> specification, Pageable pageable);

    Optional<NotificationTemplate> findNotificationTemplate(UUID id);

    NotificationTemplate saveNotificationTemplate(NotificationTemplate data);

    Optional<NotificationTemplate> updateNotificationTemplate(UUID id, NotificationTemplate data);

    Optional<UUID> deleteNotificationTemplate(UUID id);
}
