package me.sample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import me.sample.domain.CampaignClient;
import me.sample.domain.CampaignClientId;

import java.util.List;
import java.util.UUID;

public interface CampaignClientRepository extends JpaRepository<CampaignClient, CampaignClientId> {

    boolean existsByCampaignId(UUID campaignId);

    Long countAllByCampaignId(UUID campaignId);

    List<CampaignClient> findAllByCampaignId(UUID campaignId);

    void deleteAllByCampaignId(UUID campaignId);
}
