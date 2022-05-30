package me.sample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import me.sample.domain.Notification;
import me.sample.domain.Terminal;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {

    default int countSentBetweenDates(Terminal terminal, LocalDateTime from, LocalDateTime to) {
        return countByTerminalAndUpdatedDateBetween(terminal, from, to);
    }

    int countByTerminalAndUpdatedDateBetween(Terminal terminal, LocalDateTime from, LocalDateTime to);
}
