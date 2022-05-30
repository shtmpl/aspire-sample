package me.sample.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import me.sample.domain.Campaign;
import me.sample.domain.NotificationStatistic;

import java.util.Optional;
import java.util.UUID;

public interface CampaignService {

    Page<Campaign> findCampaigns(Specification<Campaign> specification, Pageable pageable);

    Optional<Campaign> findCampaign(UUID id);

    Campaign saveCampaign(Campaign data);

    Campaign updateCampaignClientsFromData(Campaign found, String clientDataContentType, String clientDataBase64);

    Optional<Campaign> startCampaign(UUID id);

    Campaign startCampaign(Campaign found);

    Optional<Campaign> pauseCampaign(UUID id);

    Campaign pauseCampaign(Campaign found);

    Campaign completeCampaign(Campaign found);

    void updateCampaignsStats();

    Optional<Campaign> updateCampaignStats(UUID id, NotificationStatistic statistic);

    Campaign updateCampaignStats(Campaign found, NotificationStatistic statistic);

    Optional<UUID> deleteCampaign(UUID id);

    UUID deleteCampaign(Campaign found);
}
