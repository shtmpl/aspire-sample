package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import me.sample.repository.ScheduledGeoposDisseminationRepository;
import me.sample.repository.StoreRepository;
import me.sample.repository.TerminalRepository;
import me.sample.service.CampaignService;
import me.sample.service.NotificationService;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import me.sample.domain.Campaign;
import me.sample.domain.CampaignClientId;
import me.sample.domain.CampaignState;
import me.sample.job.ScheduledGeoposDisseminationJob;
import me.sample.domain.NotificationLimitValidationResult;
import me.sample.domain.Partner;
import me.sample.domain.PartnerState;
import me.sample.domain.ScheduledGeoposDissemination;
import me.sample.domain.Store;
import me.sample.domain.StoreState;
import me.sample.domain.Terminal;
import me.sample.domain.TerminalSpecifications;
import me.sample.repository.CampaignClientRepository;
import me.sample.repository.GeoPositionInfoRepository;
import me.sample.service.ScheduledGeoposDisseminationService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@Service
public class ScheduledGeoposDisseminationServiceImpl implements ScheduledGeoposDisseminationService {

    Scheduler scheduler;

    ApplicationEventPublisher applicationEventPublisher;

    ScheduledGeoposDisseminationRepository scheduledGeoposDisseminationRepository;

    StoreRepository storeRepository;

    TerminalRepository terminalRepository;
    GeoPositionInfoRepository geoPositionInfoRepository;
    CampaignClientRepository campaignClientRepository;

    NotificationService notificationService;

    @NonFinal
    CampaignService campaignService;

    @PostConstruct
    private void postConstruct() throws SchedulerException {
        // Удалить джобы рассылки, для которых в базе нет соответствующей сущности рассылки
        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.groupEquals(ScheduledGeoposDisseminationJob.JOB_GROUP_NAME))) {
            if (!scheduledGeoposDisseminationRepository.existsById(UUID.fromString(jobKey.getName()))) {
                log.warn("No corresponding dissemination found for job: {}. Deleting the job", jobKey);

                // TODO отключить при переходе в прод режим, чтобы видеть наличие неконсистентности
                scheduler.deleteJob(jobKey);
            }
        }
    }

    @PreDestroy
    private void preDestroy() throws SchedulerException {
        scheduler.shutdown(false);
    }

    @Autowired
    public void setCampaignService(@Lazy CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    /**
     * Ищет все подходящие активные рассылки по геолокации, с магазинами находящимися
     * рядом с указанной геопозицией
     * <p>
     * FIXME: Remove debug logs
     */
    @Transactional(readOnly = true)
    @Override
    public Optional<ScheduledGeoposDissemination> findPrioritisedDisseminationForTerminal(Terminal terminal, Double lat, Double lon) {
        log.debug("findPrioritisedDisseminationForTerminal(terminal.id: {}, lon: {}, lat: {})", terminal.getId(), lon, lat);

        LocalDateTime now = LocalDateTime.now();
        log.debug("LocalDateTime.now() => {}", now);

        if (lat == null) {
            log.error("No lat provided", lat);

            return Optional.empty();
        }

        if (lon == null) {
            log.error("No lon provided", lat);

            return Optional.empty();
        }

        UUID companyId = terminal.getApplication().getCompany().getId();

        List<Map.Entry<Store, Double>> stores = storeRepository.findAllNeighbouringWithDistanceToGeoposition(lat, lon);
        List<ScheduledGeoposDissemination> geoposDisseminations = stores.stream()
                .peek((Map.Entry<Store, Double> entry) -> {
                    Store store = entry.getKey();
                    Double distanceToGeoposition = entry.getValue();

                    log.debug("Store: id: {}, lat: {}, lon: {}, distanceToGeoposition: {}",
                            store.getId(),
                            store.getLat(),
                            store.getLon(),
                            distanceToGeoposition);
                })
                .flatMap((Map.Entry<Store, Double> entry) -> {
                    Store store = entry.getKey();
                    Double distanceToGeoposition = entry.getValue();

                    return Stream.concat(
                            store.getScheduledGeoposDisseminations().stream()
                                    .map((ScheduledGeoposDissemination dissemination) ->
                                            new AbstractMap.SimpleImmutableEntry<>(dissemination, distanceToGeoposition)),
                            store.getPartner().getScheduledGeoposDisseminations().stream()
                                    .map((ScheduledGeoposDissemination dissemination) ->
                                            new AbstractMap.SimpleImmutableEntry<>(dissemination, distanceToGeoposition)));
                })
                .distinct()
                .filter((Map.Entry<ScheduledGeoposDissemination, Double> entry) -> {
                    ScheduledGeoposDissemination dissemination = entry.getKey();
                    Long disseminationRadius = dissemination.getCampaign().getRadius();
                    Double distanceToGeoposition = entry.getValue();

                    boolean result = distanceToGeoposition <= disseminationRadius;

                    log.debug("[{}] Dissemination id: {}. Test: distanceToGeoposition: {} <= disseminationRadius: {}",
                            result ? "Accepted" : "Rejected",
                            dissemination.getId(),
                            distanceToGeoposition,
                            disseminationRadius);

                    return result;
                }).map(Map.Entry::getKey)
                .filter((ScheduledGeoposDissemination dissemination) -> {
                    UUID disseminationCompanyId = dissemination.getCampaign().getCompany().getId();

                    boolean result = companyId.equals(disseminationCompanyId);

                    log.debug("[{}] Dissemination id: {}. Test: companyId: {} == Dissemination.companyId: {}",
                            result ? "Accepted" : "Rejected",
                            dissemination.getId(),
                            companyId,
                            disseminationCompanyId);

                    return result;
                })
                .filter((ScheduledGeoposDissemination dissemination) -> {
                    CampaignState disseminationState = dissemination.getCampaign().getState();

                    boolean result = CampaignState.RUNNING == disseminationState;

                    log.debug("[{}] Dissemination id: {}. Test: state: {} == RUNNING",
                            result ? "Accepted" : "Rejected",
                            dissemination.getId(),
                            disseminationState);

                    return result;
                })
                .filter((ScheduledGeoposDissemination dissemination) -> {
                    NotificationLimitValidationResult campaignNotificationLimitValidation =
                            notificationService.validateNotificationLimit(dissemination.getCampaign());

                    boolean result = !campaignNotificationLimitValidation.getFailed();

                    log.debug("[{}] Dissemination id: {}. Test: notificationLimit: count: {} < limit: {}",
                            result ? "Accepted" : "Rejected",
                            dissemination.getId(),
                            campaignNotificationLimitValidation.getCount(),
                            campaignNotificationLimitValidation.getLimit());

                    return result;
                })
                .filter((ScheduledGeoposDissemination dissemination) -> {
                    NotificationLimitValidationResult campaignTerminalNotificationLimitValidation =
                            notificationService.validateNotificationLimit(dissemination.getCampaign(), terminal);

                    boolean result = !campaignTerminalNotificationLimitValidation.getFailed();

                    log.debug("[{}] Dissemination id: {}. Test: notificationLimitPerTerminal: count: {} < limit: {}",
                            result ? "Accepted" : "Rejected",
                            dissemination.getId(),
                            campaignTerminalNotificationLimitValidation.getCount(),
                            campaignTerminalNotificationLimitValidation.getLimit());

                    return result;
                })
                .filter((ScheduledGeoposDissemination dissemination) -> {
                    // TODO нужно будет добавить учет часовых поясов ТТ и терминала
                    LocalDateTime disseminationStart = dissemination.getStart();
                    LocalDateTime disseminationEnd = dissemination.getEnd();

                    boolean result = now.isAfter(disseminationStart) &&
                            (now.isBefore(disseminationEnd) || now.isEqual(disseminationEnd));

                    log.debug("[{}] Dissemination id: {}. Test: Dissemination.start: {} < now: {} <= Dissemination.end: {}",
                            result ? "Accepted" : "Rejected",
                            dissemination.getId(),
                            disseminationStart,
                            now,
                            disseminationEnd);

                    return result;
                })
                .filter((ScheduledGeoposDissemination dissemination) -> {
                    boolean result = terminal.matches(dissemination.getFilters());

                    log.debug("[{}] Dissemination id: {}. Test: Dissemination.filters: {}",
                            result ? "Accepted" : "Rejected",
                            dissemination.getId(),
                            dissemination.getFilters());

                    return result;
                })
                .filter((ScheduledGeoposDissemination dissemination) -> {
                    UUID campaignId = dissemination.getCampaign().getId();
                    boolean clientLimitedCampaign = campaignClientRepository.existsByCampaignId(campaignId);
                    if (!clientLimitedCampaign) {
                        return true;
                    }

                    Object terminalClientIdValue = terminal.getProp(Terminal.PROP_KEY_CLIENT_ID);
                    if (terminalClientIdValue == null) {
                        return false;
                    }

                    String terminalClientId = String.valueOf(terminalClientIdValue);

                    boolean result = campaignClientRepository.existsById(CampaignClientId.builder()
                            .campaignId(campaignId)
                            .clientId(terminalClientId)
                            .build());

                    log.debug("[{}] Dissemination id: {}. Test: Campaign.client exists: {}",
                            result ? "Accepted" : "Rejected",
                            dissemination.getId(),
                            dissemination.getFilters());

                    return result;
                })
                .sorted(Comparator.comparing(ScheduledGeoposDissemination::getPriority).reversed())
                .collect(Collectors.toList());

        log.debug("GeoposDisseminations (filtered, sorted by priority):");
        geoposDisseminations.forEach((ScheduledGeoposDissemination dissemination) ->
                log.debug("GeoposDissemination: id: {}, priority: {}",
                        dissemination.getId(),
                        dissemination.getPriority()));

        return geoposDisseminations.stream().findFirst();
    }

    @Override
    public ScheduledGeoposDissemination saveDissemination(ScheduledGeoposDissemination data) {
        log.debug(".saveDissemination()");

        ScheduledGeoposDissemination result = scheduledGeoposDisseminationRepository.save(data);

        applicationEventPublisher.publishEvent(SaveScheduledGeoposDisseminationEvent.builder()
                .disseminationId(result.getId())
                .build());

        return result;
    }

    @SneakyThrows(SchedulerException.class)
    @Override
    public ScheduledGeoposDissemination startDissemination(ScheduledGeoposDissemination found) {
        UUID id = found.getId();
        log.debug(".startDissemination(Dissemination.id: {}", id);

        LocalDateTime startAt = found.getStart();
        if (startAt == null) {
            log.error("No start time provided for dissemination id: {}", id);

            return found;
        }

        LocalDateTime endAt = found.getEnd();
        if (endAt == null) {
            log.error("No end time provided for dissemination id: {}", id);

            return found;
        }

        String cron = found.getCron();
        if (cron == null || cron.isEmpty()) {
            log.error("Blank cron schedule provided for dissemination id: {}", id);

            return found;
        }

        NotificationLimitValidationResult campaignNotificationLimitValidation =
                notificationService.validateNotificationLimit(found.getCampaign());
        if (campaignNotificationLimitValidation.getFailed()) {
            throw new UnsupportedOperationException(campaignNotificationLimitValidation.getReason());
        }

        JobKey jobKey = JobKey.jobKey(String.valueOf(id), ScheduledGeoposDisseminationJob.JOB_GROUP_NAME);
        JobDetail jobDetail = JobBuilder.newJob(ScheduledGeoposDisseminationJob.class)
                .withIdentity(jobKey)
                .usingJobData(ScheduledGeoposDisseminationJob.JOB_DATA_KEY_ID, String.valueOf(id))
                .storeDurably()
                .requestRecovery()
                .build();

        TriggerKey triggerKey = TriggerKey.triggerKey(String.valueOf(id), ScheduledGeoposDisseminationJob.JOB_GROUP_NAME);
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .forJob(jobKey)
                .withIdentity(triggerKey)
                .startAt(Timestamp.valueOf(startAt))
                .endAt(Timestamp.valueOf(endAt))
                .withSchedule(CronScheduleBuilder.cronSchedule(cron)
                        .withMisfireHandlingInstructionFireAndProceed())
                .build();

        scheduler.scheduleJob(jobDetail, Stream.of(trigger).collect(Collectors.toSet()), true);

        return found;
    }

    @SneakyThrows(SchedulerException.class)
    @Override
    public ScheduledGeoposDissemination pauseDissemination(ScheduledGeoposDissemination found) {
        UUID id = found.getId();
        log.debug(".pauseDissemination(Dissemination.id: {}", id);

        JobKey jobKey = JobKey.jobKey(String.valueOf(id), ScheduledGeoposDisseminationJob.JOB_GROUP_NAME);
        scheduler.pauseJob(jobKey);

        return found;
    }

    @SneakyThrows(SchedulerException.class)
    @Override
    public ScheduledGeoposDissemination completeDissemination(ScheduledGeoposDissemination found) {
        UUID id = found.getId();
        log.debug(".completeDissemination(Dissemination.id: {}", id);

        JobKey jobKey = JobKey.jobKey(String.valueOf(id), ScheduledGeoposDisseminationJob.JOB_GROUP_NAME);
        scheduler.pauseJob(jobKey);

        return found;
    }

    @Override
    public Optional<ScheduledGeoposDissemination> executeDissemination(UUID id) {
        return scheduledGeoposDisseminationRepository.findById(id)
                .map(this::executeDissemination);
    }

    private ScheduledGeoposDissemination executeDissemination(ScheduledGeoposDissemination found) {
        UUID id = found.getId();
        log.debug(".executeDissemination(Dissemination.id: {})", id);

        Campaign campaign = found.getCampaign();

        NotificationLimitValidationResult campaignNotificationLimitValidation =
                notificationService.validateNotificationLimit(campaign);
        if (campaignNotificationLimitValidation.getFailed()) {
            log.warn("Dissemination paused. Reason: {}", campaignNotificationLimitValidation.getReason());

            campaignService.pauseCampaign(campaign.getId());

            return found;
        }

        Long disseminationRadius = campaign.getRadius();

        Set<UUID> storeIds = Stream.concat(
                found.getPartners().stream()
                        .filter((Partner partner) ->
                                PartnerState.ACTIVE == partner.getState())
                        .map(Partner::getStores)
                        .flatMap(Collection::stream),
                found.getStores().stream())
                .filter((Store store) ->
                        StoreState.ACTIVE == store.getState())
                .map(Store::getId)
                .collect(Collectors.toSet());

        log.debug("Found {} stores for dissemination", storeIds.size());
        if (storeIds.isEmpty()) {
            log.warn("No stores provided for dissemination id: {}", id);

            return found;
        }

        Set<UUID> terminalIds = geoPositionInfoRepository.findDistinctTerminalIdsWithinRadiusOfStores(disseminationRadius, storeIds);
        Specification<Terminal> terminalSpecification = Specification.not(TerminalSpecifications.pushIdIsNull())
                .and(TerminalSpecifications.idIn(terminalIds))
                .and(TerminalSpecifications.createForFilters(found.getFilters()));
        if (campaignClientRepository.existsByCampaignId(campaign.getId())) {
            terminalSpecification = terminalSpecification
                    .and(TerminalSpecifications.existsCampaignClientForCampaignId(campaign.getId()));
        }

        List<Terminal> terminals = terminalRepository.findAll(terminalSpecification);

        log.debug("Found {} terminals for dissemination", terminalIds.size());

        long terminalLimit = campaignNotificationLimitValidation.getLimit() != null ?
                campaignNotificationLimitValidation.getLimit() - campaignNotificationLimitValidation.getCount() :
                Long.MAX_VALUE;
        terminals.stream()
                .peek((Terminal terminal) ->
                        log.debug("Terminal.id: {}", terminal.getId()))
                .filter((Terminal terminal) -> {
                    NotificationLimitValidationResult campaignTerminalNotificationLimitValidation =
                            notificationService.validateNotificationLimit(campaign, terminal);
                    boolean validationFailed = campaignTerminalNotificationLimitValidation.getFailed();
                    if (validationFailed) {
                        log.debug("No notification performed for terminal: {}. Reason: {}",
                                terminal.getId(),
                                campaignTerminalNotificationLimitValidation.getReason());
                    }

                    return !validationFailed;
                })
                .filter((Terminal terminal) -> {
                    NotificationLimitValidationResult terminalNotificationLimitValidation =
                            notificationService.validateNotificationLimit(terminal);
                    boolean validationFailed = terminalNotificationLimitValidation.getFailed();
                    if (validationFailed) {
                        log.debug("No notification performed for terminal: {}. Reason: {}",
                                terminal.getId(),
                                terminalNotificationLimitValidation.getReason());
                    }

                    return !validationFailed;
                })
                .limit(terminalLimit)
                .forEach((Terminal terminal) ->
                        notificationService.sendNotification(terminal, campaign));

        return found;
    }

    @Override
    public Optional<ScheduledGeoposDissemination> executeDisseminationForTerminal(Terminal terminal, Double lat, Double lon) {
        log.debug(".executeDisseminationForTerminal(Terminal.id: {}, lat: {}, lon: {})", terminal.getId(), lat, lon);

        NotificationLimitValidationResult terminalNotificationLimitValidation =
                notificationService.validateNotificationLimit(terminal);
        boolean validationFailed = terminalNotificationLimitValidation.getFailed();
        if (validationFailed) {
            log.debug("No notification performed for terminal: {}. Reason: {}",
                    terminal.getId(),
                    terminalNotificationLimitValidation.getReason());

            return Optional.empty();
        }

        ScheduledGeoposDissemination found = findPrioritisedDisseminationForTerminal(terminal, lat, lon)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        Campaign campaign = found.getCampaign();

        notificationService.sendNotification(terminal, campaign);

        return Optional.of(found);
    }

    @Override
    public Optional<UUID> deleteDissemination(UUID id) {
        log.debug(".deleteDissemination(Dissemination.id: {})", id);

        ScheduledGeoposDissemination found = scheduledGeoposDisseminationRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        scheduledGeoposDisseminationRepository.delete(found);

        applicationEventPublisher.publishEvent(DeleteScheduledGeoposDisseminationEvent.builder()
                .disseminationId(id)
                .build());

        return Optional.of(id);
    }

    @SneakyThrows(SchedulerException.class)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDeleteDisseminationAfterCommit(DeleteScheduledGeoposDisseminationEvent event) {
        UUID disseminationId = event.getDisseminationId();
        log.debug(".onDeleteDisseminationAfterCommit(Dissemination.id: {})", disseminationId);

        JobKey jobKey = JobKey.jobKey(String.valueOf(disseminationId), ScheduledGeoposDisseminationJob.JOB_GROUP_NAME);
        scheduler.deleteJob(jobKey);
    }
}
