package me.sample.job;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.GeoAnalysisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Компонент, выполняющий обновление статистики уведомлений для кампании.
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Component
public class ClusterGeopositionAssociationJob {

    GeoAnalysisService geoAnalysisService;

    @NonFinal
    @Value("${job.cluster.geoposition.association.enabled}")
    boolean enabled;

    @Scheduled(cron = "${job.cluster.geoposition.association.cron}")
    public void execute() {
        if (!enabled) {
            log.warn("Job is disabled (application.yml: job.cluster.geoposition.association.enabled: false)");

            return;
        }

        log.info("Performing cluster geoposition association...");
        try {
            geoAnalysisService.associateGeopositions();
        } catch (Exception exception) {
            log.error("Failed to execute job", exception);
        }

        log.info("Performed cluster geoposition association");
    }
}
