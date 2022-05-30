package me.sample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import me.sample.domain.NotificationStatistic;

import java.util.Optional;
import java.util.UUID;

public interface NotificationStatisticRepository extends JpaRepository<NotificationStatistic, UUID> {

    @Query(
            value = "SELECT campaign_id                                                  AS campaign_id, " +
                    "       count(CASE WHEN state = 'CREATED' THEN 1 END)                AS in_state_created, " +
                    "       count(CASE WHEN state = 'SCHEDULED' THEN 1 END)              AS in_state_scheduled, " +
                    "       count(CASE WHEN state = 'QUEUED' THEN 1 END)                 AS in_state_queued, " +
                    "       count(CASE WHEN state = 'ACCEPTED_BY_SERVER' THEN 1 END)     AS in_state_accepted, " +
                    "       count(CASE WHEN state = 'REJECTED_BY_SERVER' THEN 1 END)     AS in_state_rejected, " +
                    "       count(CASE WHEN state = 'RECEIVED_BY_CLIENT' THEN 1 END)     AS in_state_received, " +
                    "       count(CASE WHEN state = 'ACKNOWLEDGED_BY_CLIENT' THEN 1 END) AS in_state_acknowledged, " +
                    "       count(CASE WHEN state = 'FAILED' THEN 1 END)                 AS in_state_failed " +
                    "FROM notification " +
                    "WHERE campaign_id = :campaignId " +
                    "GROUP BY campaign_id",
            nativeQuery = true
    )
    Optional<NotificationStatistic> findByCampaignId(@Param("campaignId") UUID campaignId);
}
