package me.sample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import me.sample.domain.Campaign;
import me.sample.domain.CampaignState;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface CampaignRepository extends JpaRepository<Campaign, UUID>, JpaSpecificationExecutor<Campaign> {

    Stream<Campaign> findAllByStateIn(List<CampaignState> states);

    Optional<Campaign> findFirstByDistributionId(UUID distributionId);

    Optional<Campaign> findFirstByScheduledGeoposDisseminationId(UUID scheduledGeoposDisseminationId);
}
