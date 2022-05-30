package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.NotificationTemplateService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.enumeration.Language;
import me.sample.enumeration.TemplateType;
import me.sample.domain.BadResourceException;
import me.sample.domain.NotificationTemplate;
import me.sample.repository.NotificationTemplateRepository;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@CacheConfig(cacheNames = NotificationTemplateServiceImpl.NOTIFICATION_TEMPLATE_CACHE)
@Transactional
@Service
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    public static final String NOTIFICATION_TEMPLATE_CACHE = "notificationTemplate";

    NotificationTemplateRepository notificationTemplateRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<NotificationTemplate> findNotificationTemplates(Specification<NotificationTemplate> specification, Pageable pageable) {
        return notificationTemplateRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<NotificationTemplate> findNotificationTemplate(UUID id) {
        return notificationTemplateRepository.findById(id);
    }

    @Override
    public NotificationTemplate saveNotificationTemplate(NotificationTemplate data) {
        UUID id = data.getId();
        if (id != null && notificationTemplateRepository.existsById(id)) {
            throw new BadResourceException(String.format("Notification template already exists for id: %s", id));
        }

        return notificationTemplateRepository.save(data);
    }

    @Override
    public Optional<NotificationTemplate> updateNotificationTemplate(UUID id, NotificationTemplate data) {
        return notificationTemplateRepository.findById(id)
                .map((NotificationTemplate found) -> updateNotificationTemplate(found, data));
    }

    private NotificationTemplate updateNotificationTemplate(NotificationTemplate found, NotificationTemplate data) {
        TemplateType type = data.getType();
        if (type != null && type != found.getType()) {
            found.setType(type);
        }

        Language language = data.getLanguage();
        if (language != null && language != found.getLanguage()) {
            found.setLanguage(language);
        }

        String name = data.getName();
        if (name != null && !name.equals(found.getName())) {
            found.setName(name);
        }

        String subject = data.getSubject();
        if (subject != null && !subject.equals(found.getSubject())) {
            found.setSubject(subject);
        }

        String contentType = data.getContentType();
        if (contentType != null && !contentType.equals(found.getContentType())) {
            found.setContentType(contentType);
        }

        String text = data.getText();
        if (text != null && !text.equals(found.getText())) {
            found.setText(text);
        }

        String customPushPartName = data.getCustomPushPartName();
        if (customPushPartName != null && !customPushPartName.equals(found.getCustomPushPartName())) {
            found.setCustomPushPartName(customPushPartName);
        }

        String customPushPartValue = data.getCustomPushPartValue();
        if (customPushPartValue != null && !customPushPartValue.equals(found.getCustomPushPartValue())) {
            found.setCustomPushPartValue(customPushPartValue);
        }

        return notificationTemplateRepository.save(found);
    }

    @Override
    public Optional<UUID> deleteNotificationTemplate(UUID id) {
        NotificationTemplate found = notificationTemplateRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        notificationTemplateRepository.deleteById(id);

        return Optional.of(id);
    }
}
