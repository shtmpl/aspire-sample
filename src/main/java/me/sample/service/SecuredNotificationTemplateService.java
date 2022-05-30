package me.sample.service;

import me.sample.domain.NotificationTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface SecuredNotificationTemplateService {

    Page<NotificationTemplate> findNotificationTemplates(String search, Pageable pageable);

    Optional<NotificationTemplate> findNotificationTemplate(UUID id);

    NotificationTemplate saveNotificationTemplate(NotificationTemplate data);

    Optional<NotificationTemplate> updateNotificationTemplate(UUID id, NotificationTemplate data);

    Optional<UUID> deleteNotificationTemplate(UUID id);
}
