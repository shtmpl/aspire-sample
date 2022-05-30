package me.sample.service;

import me.sample.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.UUID;

public interface SecuredNotificationService {

    Page<Notification> findNotifications(Pageable pageable);

    Page<Notification> findNotifications(Specification<Notification> specification, Pageable pageable);

    Optional<Notification> findNotification(UUID id);
}
