package me.sample.service;

import me.sample.domain.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.UUID;

public interface SecuredCampaignService {

    Page<Campaign> findCampaigns(Pageable pageable);

    Page<Campaign> findCampaigns(Specification<Campaign> specification, Pageable pageable);

    Optional<Campaign> findCampaign(UUID id);

    Campaign saveCampaign(Campaign data, String clientIdDataContentType, String clientIdDataBase64);

    Optional<Campaign> startCampaign(UUID id);

    Optional<Campaign> pauseCampaign(UUID id);

    Optional<Campaign> completeCampaign(UUID id);

    Optional<UUID> deleteCampaign(UUID id);
}
