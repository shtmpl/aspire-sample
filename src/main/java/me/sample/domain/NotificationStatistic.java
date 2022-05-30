package me.sample.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "notification_statistic")
@Entity
public class NotificationStatistic {

    /**
     * Id кампании ({@link Campaign})
     */
    @Id
    UUID campaignId;

    /**
     * Кол-во уведомлений в состоянии {@link NotificationState#CREATED}
     */
    Integer inStateCreated;

    /**
     * Кол-во уведомлений в состоянии {@link NotificationState#SCHEDULED}
     */
    Integer inStateScheduled;

    /**
     * Кол-во уведомлений в состоянии {@link NotificationState#QUEUED}
     */
    Integer inStateQueued;

    /**
     * Кол-во уведомлений в состоянии {@link NotificationState#ACCEPTED_BY_SERVER}
     */
    Integer inStateAccepted;

    /**
     * Кол-во уведомлений в состоянии {@link NotificationState#REJECTED_BY_SERVER}
     */
    Integer inStateRejected;

    /**
     * Кол-во уведомлений в состоянии {@link NotificationState#RECEIVED_BY_CLIENT}
     */
    Integer inStateReceived;

    /**
     * Кол-во уведомлений в состоянии {@link NotificationState#ACKNOWLEDGED_BY_CLIENT}
     */
    Integer inStateAcknowledged;

    /**
     * Кол-во уведомлений в состоянии {@link NotificationState#FAILED}
     */
    Integer inStateFailed;
}
