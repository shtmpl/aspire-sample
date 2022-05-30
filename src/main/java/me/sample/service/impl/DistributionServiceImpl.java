package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.CampaignService;
import me.sample.service.NotificationService;
import me.sample.service.TerminalService;
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
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import me.sample.job.DistributionJob;
import me.sample.domain.Campaign;
import me.sample.domain.Distribution;
import me.sample.domain.NotificationLimitValidationResult;
import me.sample.domain.Terminal;
import me.sample.domain.TerminalSpecifications;
import me.sample.repository.CampaignClientRepository;
import me.sample.repository.DistributionRepository;
import me.sample.service.DistributionService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Сервис выполняет рассылку по времени
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@CacheConfig(cacheNames = DistributionServiceImpl.DISTRIBUTION_CACHE)
@Transactional
@Service
public class DistributionServiceImpl implements DistributionService {

    public static final String DISTRIBUTION_CACHE = "distribution";


    Scheduler scheduler;

    ApplicationEventPublisher applicationEventPublisher;

    DistributionRepository distributionRepository;
    CampaignClientRepository campaignClientRepository;

    TerminalService terminalService;
    NotificationService notificationService;

    @NonFinal
    CampaignService campaignService;

    @PostConstruct
    private void postConstruct() throws SchedulerException {
        // Удалить джобы рассылки, для которых в базе нет соответствующей сущности рассылки
        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.groupEquals(DistributionJob.JOB_GROUP_NAME))) {
            if (!distributionRepository.existsById(UUID.fromString(jobKey.getName()))) {
                log.warn("No corresponding Distribution found for job: {}. Deleting the job", jobKey);

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

    @Transactional(readOnly = true)
    @Override
    public Page<Distribution> findDisseminations(Specification<Distribution> specification, Pageable pageable) {
        return distributionRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Distribution> findDissemination(UUID id) {
        return distributionRepository.findById(id);
    }

    @Override
    public Distribution saveDissemination(Distribution data) {
        log.debug(".saveDissemination()");

        return distributionRepository.save(data);
    }

    @SneakyThrows(SchedulerException.class)
    @Override
    public Distribution startDissemination(Distribution found) {
        UUID id = found.getId();
        log.debug(".startDissemination(Dissemination.id: {}", id);

        NotificationLimitValidationResult campaignNotificationLimitValidation =
                notificationService.validateNotificationLimit(found.getCampaign());
        if (campaignNotificationLimitValidation.getFailed()) {
            throw new UnsupportedOperationException(campaignNotificationLimitValidation.getReason());
        }

        JobKey jobKey = JobKey.jobKey(String.valueOf(id), DistributionJob.JOB_GROUP_NAME);
        JobDetail jobDetail = JobBuilder.newJob(DistributionJob.class)
                .withIdentity(jobKey)
                .usingJobData(DistributionJob.JOB_DATA_KEY_ID, id.toString())
                .storeDurably()
                .requestRecovery()
                .build();

        TriggerKey triggerKey = TriggerKey.triggerKey(String.valueOf(id), DistributionJob.JOB_GROUP_NAME);
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .startAt(Date.valueOf(found.getStart()))
                .endAt(Date.valueOf(found.getEnd()))
                .withSchedule(CronScheduleBuilder.cronSchedule(found.getCron())
                        .withMisfireHandlingInstructionFireAndProceed())
                .build();

        scheduler.scheduleJob(jobDetail, Stream.of(trigger).collect(Collectors.toSet()), true);

        return found;
    }

    @SneakyThrows(SchedulerException.class)
    @Override
    public Distribution pauseDissemination(Distribution found) {
        UUID id = found.getId();
        log.debug(".pauseDissemination(Dissemination.id: {}", id);

        JobKey jobKey = JobKey.jobKey(String.valueOf(id), DistributionJob.JOB_GROUP_NAME);
        scheduler.pauseJob(jobKey);

        return found;
    }

    @SneakyThrows(SchedulerException.class)
    @Override
    public Distribution completeDissemination(Distribution found) {
        UUID id = found.getId();
        log.debug(".completeDissemination(Dissemination.id: {}", id);

        JobKey jobKey = JobKey.jobKey(String.valueOf(id), DistributionJob.JOB_GROUP_NAME);
        scheduler.pauseJob(jobKey);

        return found;
    }

    @Override
    public Optional<Distribution> executeDissemination(UUID id) {
        return distributionRepository.findById(id)
                .map(this::executeDissemination);
    }

    private Distribution executeDissemination(Distribution found) {
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

        // FIXME may cause out of memory, make iteration over pages
        long terminalLimit = campaignNotificationLimitValidation.getLimit() != null ?
                campaignNotificationLimitValidation.getLimit() - campaignNotificationLimitValidation.getCount() :
                Long.MAX_VALUE;

        Specification<Terminal> terminalSpecification = Specification.not(TerminalSpecifications.pushIdIsNull())
                .and(TerminalSpecifications.createForFilters(found.getFilters()));
        if (campaignClientRepository.existsByCampaignId(campaign.getId())) {
            terminalSpecification = terminalSpecification
                    .and(TerminalSpecifications.existsCampaignClientForCampaignId(campaign.getId()));
        }

        terminalService.findTerminals(terminalSpecification)
                .stream()
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
    public Optional<UUID> deleteDissemination(UUID id) {
        log.debug(".deleteDissemination(Dissemination.id: {})", id);

        Distribution found = distributionRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        distributionRepository.delete(found);

        applicationEventPublisher.publishEvent(DeleteDistributionEvent.builder()
                .disseminationId(id)
                .build());

        return Optional.of(id);
    }

    @SneakyThrows(SchedulerException.class)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDeleteDisseminationAfterCommit(DeleteDistributionEvent event) {
        UUID disseminationId = event.getDisseminationId();
        log.debug(".onDeleteDisseminationAfterCommit(id: {})", disseminationId);

        JobKey jobKey = JobKey.jobKey(String.valueOf(disseminationId), DistributionJob.JOB_GROUP_NAME);
        scheduler.deleteJob(jobKey);
    }
}
