package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import me.sample.repository.NotificationRepository;
import me.sample.repository.NotificationStateLogRepository;
import me.sample.service.NotificationService;
import me.sample.service.properties.PushPropertiesProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.Campaign;
import me.sample.domain.NotFoundResourceException;
import me.sample.domain.Notification;
import me.sample.domain.NotificationLimitValidationResult;
import me.sample.domain.NotificationState;
import me.sample.domain.NotificationStateLog;
import me.sample.domain.NotificationTemplate;
import me.sample.domain.Terminal;
import me.sample.service.NotificationDeliveryService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@Service
public class NotificationServiceImpl implements NotificationService {

    NotificationRepository notificationRepository;
    NotificationStateLogRepository notificationStateLogRepository;

    NotificationDeliveryService notificationDeliveryService;

    PushPropertiesProvider pushProperties;

    @NonFinal
    @Value("${notification.state-log.enabled:false}")
    boolean notificationStateLogEnabled;

    @Transactional(readOnly = true)
    @Override
    public Page<Notification> findNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Notification> findNotifications(Specification<Notification> specification, Pageable pageable) {
        return notificationRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Notification> findNotification(UUID id) {
        return notificationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public NotificationLimitValidationResult validateNotificationLimit(Campaign campaign) {
        Long limit = campaign.getNotificationLimit();
        long count = campaign.getNotifications().stream()
                .filter(Notification::wasSent)
                .count();

        return NotificationLimitValidationResult.builder()
                .failed(limit != null && limit <= count)
                .reason(String.format(
                        "Notification limit exceeded for campaign: %s. Limit: %s, Count: %s",
                        campaign.getId(),
                        limit,
                        count))
                .limit(limit)
                .count(count)
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public NotificationLimitValidationResult validateNotificationLimit(Campaign campaign, Terminal terminal) {
        Long limit = campaign.getNotificationLimitPerTerminal();
        long count = campaign.getNotifications().stream()
                .filter((Notification notification) -> terminal.getId().equals(notification.getTerminal().getId()))
                .filter(Notification::wasSent)
                .count();

        return NotificationLimitValidationResult.builder()
                .failed(limit != null && limit <= count)
                .reason(String.format(
                        "Notification limit exceeded for campaign: %s for terminal: %s. Limit: %s, Count: %s",
                        campaign.getId(),
                        terminal.getId(),
                        limit,
                        count))
                .limit(limit)
                .count(count)
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public NotificationLimitValidationResult validateNotificationLimit(Terminal terminal) {
        LocalDateTime now = LocalDateTime.now();

        NotificationLimitValidationResult validationPerDay = validateNotificationLimitPerMinute(terminal, now);
        if (validationPerDay.getFailed()) {
            return validationPerDay;
        }

        NotificationLimitValidationResult validationPerHour = validateNotificationLimitPerHour(terminal, now);
        if (validationPerHour.getFailed()) {
            return validationPerHour;
        }

        return validateNotificationLimitPerDay(terminal, now);
    }

    private NotificationLimitValidationResult validateNotificationLimitPerMinute(Terminal terminal, LocalDateTime at) {
        Integer limit = pushProperties.getMaxPushOnMinute();
        int count = countSentBetweenDates(terminal, at.minusMinutes(1), at);

        return NotificationLimitValidationResult.builder()
                .failed(limit != null && limit <= count)
                .reason(String.format(
                        "Notification limit per minute exceeded for terminal: %s. Limit: %s, Count: %s",
                        terminal.getId(),
                        limit,
                        count))
                .limit(limit == null ? null : limit.longValue())
                .count((long) count)
                .build();
    }

    private NotificationLimitValidationResult validateNotificationLimitPerHour(Terminal terminal, LocalDateTime at) {
        Integer limit = pushProperties.getMaxPushOnHour();
        int count = countSentBetweenDates(terminal, at.minusHours(1), at);

        return NotificationLimitValidationResult.builder()
                .failed(limit != null && limit <= count)
                .reason(String.format(
                        "Notification limit per hour exceeded for terminal: %s. Limit: %s, Count: %s",
                        terminal.getId(),
                        limit,
                        count))
                .limit(limit == null ? null : limit.longValue())
                .count((long) count)
                .build();
    }

    private NotificationLimitValidationResult validateNotificationLimitPerDay(Terminal terminal, LocalDateTime at) {
        Integer limit = pushProperties.getMaxPushOnDay();
        int count = countSentBetweenDates(terminal, at.minusDays(1), at);

        return NotificationLimitValidationResult.builder()
                .failed(limit != null && limit <= count)
                .reason(String.format(
                        "Notification limit per day exceeded for terminal: %s. Limit: %s, Count: %s",
                        terminal.getId(),
                        limit,
                        count))
                .limit(limit == null ? null : limit.longValue())
                .count((long) count)
                .build();
    }

    private int countSentBetweenDates(Terminal terminal, LocalDateTime from, LocalDateTime to) {
        return notificationRepository.countSentBetweenDates(terminal, from, to);
    }

    @Override
    public Notification saveNotification(Notification data) {
        Notification result = notificationRepository.save(data);

        if (notificationStateLogEnabled) {
            notificationStateLogRepository.save(NotificationStateLog.builder()
                    .notification(result)
                    .state(data.getState())
                    .build());
        }

        return result;
    }

    @Override
    public Optional<Notification> updateNotificationState(UUID id, NotificationState state) {
        return updateNotificationState(id, state, null);
    }

    @Override
    public Optional<Notification> updateNotificationState(UUID id, NotificationState state, String stateReason) {
        log.debug(".updateNotificationState(id: {}, state: {}, stateReason: {})", id, state, stateReason);

        Notification found = notificationRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        return Optional.of(updateNotificationState(found, state, stateReason));
    }

    private Notification updateNotificationState(Notification found, NotificationState state, String stateReason) {
        if (state != null && state != found.getState()) {
            found.setState(state);
        }

        Notification result = notificationRepository.save(found);

        if (notificationStateLogEnabled) {
            notificationStateLogRepository.save(NotificationStateLog.builder()
                    .notification(result)
                    .state(state)
                    .stateReason(stateReason)
                    .build());
        }

        return result;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void updateNotificationStateAsync(UUID id, NotificationState state) {
        updateNotificationState(id, state)
                .orElseThrow(() -> new NotFoundResourceException("Notification", id));
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public Notification sendNotification(Terminal terminal, Campaign campaign) {
        UUID campaignId = campaign.getId();
        UUID terminalId = terminal.getId();
        log.info(".sendNotification(Campaign.id: {}, Terminal.id: {})", campaignId, terminalId);

        NotificationTemplate campaignNotificationTemplate = campaign.getNotificationTemplate();
        if (campaignNotificationTemplate == null) {
            log.warn("Notification skipped for terminal id: {}. Reason: No notification template defined for campaign id: {}",
                    terminalId,
                    campaignId);
            // FIXME

            return null;
        }

        Notification result = saveNotification(Notification.builder()
                .campaign(campaign)
                .terminal(terminal)
                .state(NotificationState.CREATED)
                .text(campaignNotificationTemplate.getText())
                .build());

        notificationDeliveryService.sendNotification(result.getId());

        return result;
    }
}
