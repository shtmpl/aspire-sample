package me.sample.job;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import me.sample.service.AnalyticService;

/**
 * Компонент, выполняющий генерацию аналитического отчета для терминалов
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Component
public class AnalyticReportGenerationJob {

    AnalyticService analyticService;

    @Scheduled(cron = "${job.analytic.report.generation.cron}")
    public void execute() {
        log.info("Performing analytic report generation...");
        try {
            analyticService.generateAndSaveAnalyticReports();
        } catch (Exception exception) {
            log.error("Failed to execute job", exception);
        }

        log.info("Performed analytic report generation");
    }
}
