package me.sample.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import me.sample.domain.NotificationState;
import me.sample.domain.Campaign;
import me.sample.domain.Notification;
import me.sample.domain.NotificationLimitValidationResult;
import me.sample.domain.Terminal;

import java.util.Optional;
import java.util.UUID;

public interface NotificationService {

    Page<Notification> findNotifications(Pageable pageable);

    Page<Notification> findNotifications(Specification<Notification> specification, Pageable pageable);

    Optional<Notification> findNotification(UUID id);

    NotificationLimitValidationResult validateNotificationLimit(Campaign campaign);

    NotificationLimitValidationResult validateNotificationLimit(Campaign campaign, Terminal terminal);

    NotificationLimitValidationResult validateNotificationLimit(Terminal terminal);

    Notification saveNotification(Notification data);

    Optional<Notification> updateNotificationState(UUID id, NotificationState state);

    Optional<Notification> updateNotificationState(UUID id, NotificationState state, String stateReason);

    void updateNotificationStateAsync(UUID id, NotificationState state);

    Notification sendNotification(Terminal terminal, Campaign campaign);
}
