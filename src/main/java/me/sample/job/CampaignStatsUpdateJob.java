package me.sample.job;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.CampaignService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Компонент, выполняющий обновление статистики уведомлений для кампании.
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Component
public class CampaignStatsUpdateJob {

    CampaignService campaignService;

    @Scheduled(cron = "${job.campaign.stats.update.cron}")
    public void execute() {
        log.info("Performing campaign stats update...");
        campaignService.updateCampaignsStats();

        log.info("Performed campaign stats update");
    }
}
