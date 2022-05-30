package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.NotificationTemplate;
import me.sample.domain.NotificationTemplateSpecifications;
import me.sample.service.CompanyAuthorityService;
import me.sample.service.NotificationTemplateService;
import me.sample.service.SecuredNotificationTemplateService;
import me.sample.service.SecurityService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@CacheConfig(cacheNames = SecuredNotificationTemplateServiceImpl.NOTIFICATION_TEMPLATE_CACHE)
@Transactional
@Service
public class SecuredNotificationTemplateServiceImpl implements SecuredNotificationTemplateService {

    public static final String NOTIFICATION_TEMPLATE_CACHE = "notificationTemplate";

    SecurityService securityService;
    CompanyAuthorityService companyAuthorityService;

    NotificationTemplateService notificationTemplateService;

    @Transactional(readOnly = true)
    @Override
    public Page<NotificationTemplate> findNotificationTemplates(String search, Pageable pageable) {
        Long userId = securityService.getUserId();
        log.debug(".findNotificationTemplates(), User.id: {}", userId);

        if (search == null || search.trim().isEmpty()) {
            return notificationTemplateService.findNotificationTemplates(NotificationTemplateSpecifications.companyAuthorityUserIdEqualTo(userId), pageable);
        }

        return notificationTemplateService.findNotificationTemplates(
                NotificationTemplateSpecifications.companyAuthorityUserIdEqualTo(userId)
                        .and(NotificationTemplateSpecifications.nameLike(search).or(NotificationTemplateSpecifications.subjectLike(search))),
                pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<NotificationTemplate> findNotificationTemplate(UUID id) {
        log.debug(".findNotificationTemplate(id: {})", id);

        NotificationTemplate found = notificationTemplateService.findNotificationTemplate(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkRead(found.getCompany().getId());

        return Optional.of(found);
    }

    @Override
    public NotificationTemplate saveNotificationTemplate(NotificationTemplate data) {
        log.debug(".saveNotificationTemplate()");

        companyAuthorityService.checkWrite(data.getCompany().getId());

        return notificationTemplateService.saveNotificationTemplate(data);
    }

    @Override
    public Optional<NotificationTemplate> updateNotificationTemplate(UUID id, NotificationTemplate data) {
        log.debug(".updateNotificationTemplate(id: {})", id);

        NotificationTemplate found = notificationTemplateService.findNotificationTemplate(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkWrite(data.getCompany().getId());
        companyAuthorityService.checkWrite(found.getCompany().getId());

        return notificationTemplateService.updateNotificationTemplate(id, data);
    }

    @Override
    public Optional<UUID> deleteNotificationTemplate(UUID id) {
        log.debug(".deleteNotificationTemplate(id: {})", id);

        NotificationTemplate found = notificationTemplateService.findNotificationTemplate(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkWrite(found.getCompany().getId());

        return notificationTemplateService.deleteNotificationTemplate(id);
    }
}
