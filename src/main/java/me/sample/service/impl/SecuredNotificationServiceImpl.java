package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.CompanyAuthorityService;
import me.sample.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.Notification;
import me.sample.domain.NotificationSpecifications;
import me.sample.service.SecuredNotificationService;
import me.sample.service.SecurityService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@Service
public class SecuredNotificationServiceImpl implements SecuredNotificationService {

    SecurityService securityService;
    CompanyAuthorityService companyAuthorityService;

    NotificationService notificationService;

    @Transactional(readOnly = true)
    @Override
    public Page<Notification> findNotifications(Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findNotifications(), User.id: {}", userId);

        return notificationService.findNotifications(
                NotificationSpecifications.terminalApplicationCompanyAuthorityUserIdEqualTo(userId),
                pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Notification> findNotifications(Specification<Notification> specification, Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findNotifications(), User.id: {}", userId);

        return notificationService.findNotifications(
                NotificationSpecifications.terminalApplicationCompanyAuthorityUserIdEqualTo(userId)
                        .and(specification),
                pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Notification> findNotification(UUID id) {
        log.debug(".findNotification(id: {})", id);

        Notification found = notificationService.findNotification(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkRead(found.getTerminal().getApplication().getCompany().getId());

        return Optional.of(found);
    }
}
