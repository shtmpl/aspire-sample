package me.sample.service.impl;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.CampaignService;
import me.sample.service.DistributionService;
import me.sample.service.ScheduledGeoposDisseminationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.Campaign;
import me.sample.domain.CampaignClient;
import me.sample.domain.CampaignState;
import me.sample.domain.Clients;
import me.sample.domain.Distribution;
import me.sample.domain.BadResourceException;
import me.sample.domain.ScheduledGeoposDissemination;
import me.sample.domain.NotificationStatistic;
import me.sample.repository.CampaignClientRepository;
import me.sample.repository.CampaignRepository;
import me.sample.repository.NotificationStatisticRepository;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@Service
public class CampaignServiceImpl implements CampaignService {

    private static final int BATCH_SIZE_CAMPAIGN_CLIENT_ID_SAVE = 1000;


    CampaignRepository campaignRepository;
    CampaignClientRepository campaignClientRepository;

    DistributionService distributionService;
    ScheduledGeoposDisseminationService scheduledGeoposDisseminationService;

    NotificationStatisticRepository notificationStatisticRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<Campaign> findCampaigns(Specification<Campaign> specification, Pageable pageable) {
        return campaignRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Campaign> findCampaign(UUID id) {
        return campaignRepository.findById(id);
    }

    @Override
    public Campaign saveCampaign(Campaign data) {
        UUID id = data.getId();
        if (id != null && campaignRepository.existsById(id)) {
            throw new BadResourceException(String.format("Campaign already exists for id: %s", id));
        }

        Distribution distribution = data.getDistribution();
        if (distribution != null) {
            distributionService.saveDissemination(distribution);
        }

        ScheduledGeoposDissemination scheduledGeoposDissemination = data.getScheduledGeoposDissemination();
        if (scheduledGeoposDissemination != null) {
            scheduledGeoposDisseminationService.saveDissemination(scheduledGeoposDissemination);
        }

        return campaignRepository.save(data);
    }

    @Override
    public Campaign updateCampaignClientsFromData(Campaign found, String clientDataContentType, String clientDataBase64) {
        UUID campaignId = found.getId();

        campaignClientRepository.deleteAllByCampaignId(campaignId);

        log.info("Parsing client id data of type: {}...", clientDataContentType);
        List<String> clientIds = Clients.parseClientIds(
                clientDataContentType,
                Base64.getDecoder().decode(clientDataBase64));
        log.info("Parsed {} client ids", clientIds.size());

        log.info("Saving campaign clients...");
        long campaignClientCount = 0;
        for (List<String> clientIdBatch : Lists.partition(clientIds, BATCH_SIZE_CAMPAIGN_CLIENT_ID_SAVE)) {
            campaignClientCount += saveCampaignClientIds(found, clientIdBatch);
        }
        log.info("Saved {} campaign clients", campaignClientCount);

        return campaignRepository.save(found.setClientBased(true));
    }

    private long saveCampaignClientIds(Campaign found, Collection<String> clientIds) {
        List<CampaignClient> clients = clientIds
                .stream()
                .map((String clientId) ->
                        CampaignClient.builder()
                                .campaign(found)
                                .build()
                                .setClientId(clientId))
                .collect(Collectors.toList());

        return (long) campaignClientRepository.saveAll(clients).size();
    }

    @Override
    public Optional<Campaign> startCampaign(UUID id) {
        Campaign found = campaignRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        return Optional.of(startCampaign(found));
    }

    @Override
    public Campaign startCampaign(Campaign found) {
        if (CampaignState.COMPLETED == found.getState()) {
            throw new UnsupportedOperationException("Campaign has already been completed");
        }

        found.setState(CampaignState.RUNNING);

        Optional.ofNullable(found.getDistribution())
                .ifPresent(distributionService::startDissemination);

        Optional.ofNullable(found.getScheduledGeoposDissemination())
                .ifPresent(scheduledGeoposDisseminationService::startDissemination);

        return campaignRepository.save(found);
    }

    @Override
    public Optional<Campaign> pauseCampaign(UUID id) {
        Campaign found = campaignRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        return Optional.of(pauseCampaign(found));
    }

    @Override
    public Campaign pauseCampaign(Campaign found) {
        if (CampaignState.COMPLETED == found.getState()) {
            throw new UnsupportedOperationException("Campaign has already been completed");
        }

        found.setState(CampaignState.PAUSE);

        Optional.ofNullable(found.getDistribution())
                .ifPresent(distributionService::pauseDissemination);

        Optional.ofNullable(found.getScheduledGeoposDissemination())
                .ifPresent(scheduledGeoposDisseminationService::pauseDissemination);

        return campaignRepository.save(found);
    }

    @Override
    public Campaign completeCampaign(Campaign found) {
        found.setState(CampaignState.COMPLETED);

        Optional.ofNullable(found.getDistribution())
                .ifPresent(distributionService::completeDissemination);

        Optional.ofNullable(found.getScheduledGeoposDissemination())
                .ifPresent(scheduledGeoposDisseminationService::completeDissemination);

        return campaignRepository.save(found);
    }

    @Override
    public void updateCampaignsStats() {
        try (Stream<Campaign> campaigns = campaignRepository.findAllByStateIn(Arrays.asList(
                CampaignState.RUNNING,
                CampaignState.PAUSE,
                CampaignState.COMPLETED))) {
            campaigns.forEach((Campaign campaign) ->
                    notificationStatisticRepository.findByCampaignId(campaign.getId()).ifPresent((NotificationStatistic statistic) ->
                            updateCampaignStats(campaign, statistic)));
        }
    }

    @Override
    public Optional<Campaign> updateCampaignStats(UUID id, NotificationStatistic statistic) {
        Campaign found = campaignRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        return Optional.of(updateCampaignStats(found, statistic));
    }

    @Override
    public Campaign updateCampaignStats(Campaign found, NotificationStatistic statistic) {
        return campaignRepository.save(setCampaignStats(found, statistic));
    }

    private Campaign setCampaignStats(Campaign result, NotificationStatistic statistic) {
        int totalCreated = statistic.getInStateCreated() +
                statistic.getInStateScheduled() +
                statistic.getInStateQueued() +
                statistic.getInStateAccepted() +
                statistic.getInStateRejected() +
                statistic.getInStateReceived() +
                statistic.getInStateAcknowledged() +
                statistic.getInStateFailed();
        int totalSent = statistic.getInStateAccepted()
                + statistic.getInStateReceived()
                + statistic.getInStateAcknowledged();
        int totalDelivered = statistic.getInStateReceived()
                + statistic.getInStateAcknowledged();
        int totalOpened = statistic.getInStateAcknowledged();
        int totalFailed = statistic.getInStateRejected() +
                statistic.getInStateFailed();

        return result
                .setCreated(totalCreated)
                .setSent(totalSent)
                .setDelivered(totalDelivered)
                .setOpened(totalOpened)
                .setFail(totalFailed);
    }

    @Override
    public Optional<UUID> deleteCampaign(UUID id) {
        Campaign found = campaignRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        return Optional.of(deleteCampaign(found));
    }

    @Override
    public UUID deleteCampaign(Campaign found) {
        Distribution distribution = found.getDistribution();
        if (distribution != null) {
            distributionService.deleteDissemination(distribution.getId());
        }

        ScheduledGeoposDissemination scheduledGeoposDissemination = found.getScheduledGeoposDissemination();
        if (scheduledGeoposDissemination != null) {
            scheduledGeoposDisseminationService.deleteDissemination(scheduledGeoposDissemination.getId());
        }

        campaignClientRepository.deleteAllByCampaignId(found.getId());

        campaignRepository.delete(found);

        return found.getId();
    }
}
