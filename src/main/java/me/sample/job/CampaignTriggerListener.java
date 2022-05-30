package me.sample.job;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.repository.CampaignRepository;
import me.sample.service.CampaignService;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public class CampaignTriggerListener implements TriggerListener {

    public static final String NAME = "CAMPAIGN_TRIGGER_LISTENER";


    CampaignRepository campaignRepository;
    CampaignService campaignService;

    public CampaignTriggerListener(CampaignRepository campaignRepository,
                                   @Lazy CampaignService campaignService) {
        this.campaignService = campaignService;
        this.campaignRepository = campaignRepository;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        // NOP
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        // NOP
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode) {
        if (trigger.mayFireAgain()) {
            return;
        }

        JobKey jobKey = trigger.getJobKey();
        String jobName = jobKey.getName();
        String jobGroup = jobKey.getGroup();
        if (jobGroup == null) {
            return;
        }

        UUID disseminationId;
        switch (jobGroup) {
            case DistributionJob.JOB_GROUP_NAME:
                disseminationId = Optional.of(context.getMergedJobDataMap())
                        .map((JobDataMap map) -> map.getString(DistributionJob.JOB_DATA_KEY_ID))
                        .map(UUID::fromString)
                        .orElse(null);
                if (disseminationId == null) {
                    log.error("No dissemination id associated for job: {}", jobName);

                    return;
                }

                campaignRepository.findFirstByDistributionId(disseminationId)
                        .map(campaignService::completeCampaign);

                return;
            case ScheduledGeoposDisseminationJob.JOB_GROUP_NAME:
                disseminationId = Optional.of(context.getMergedJobDataMap())
                        .map((JobDataMap map) -> map.getString(ScheduledGeoposDisseminationJob.JOB_DATA_KEY_ID))
                        .map(UUID::fromString)
                        .orElse(null);
                if (disseminationId == null) {
                    log.error("No dissemination id associated for job: {}", jobName);

                    return;
                }

                campaignRepository.findFirstByScheduledGeoposDisseminationId(disseminationId)
                        .map(campaignService::completeCampaign);

                return;
            default:
                log.warn("Undefined group for job: {}", jobName);
        }
    }
}
