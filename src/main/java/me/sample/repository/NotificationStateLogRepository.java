package me.sample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import me.sample.domain.NotificationStateLog;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationStateLogRepository extends JpaRepository<NotificationStateLog, UUID> {

    List<NotificationStateLog> findAllByNotificationIdOrderByCreatedAtAsc(UUID notificationId);
}
