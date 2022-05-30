package me.sample.job;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import me.sample.service.ScheduledGeoposDisseminationService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@DisallowConcurrentExecution
@Component
public class ScheduledGeoposDisseminationJob implements Job {

    public static final String JOB_GROUP_NAME = "SCHEDULED_GEOPOS_DISSEMINATION";

    public static final String JOB_DATA_KEY_ID = "ID";

    ScheduledGeoposDisseminationService scheduledGeoposDisseminationService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        UUID disseminationId = Optional.of(context.getMergedJobDataMap())
                .map((JobDataMap map) -> map.getString(JOB_DATA_KEY_ID))
                .map(UUID::fromString)
                .orElse(null);
        if (disseminationId == null) {
            log.error("No dissemination id associated for job: {}", context.getJobDetail().getKey().getName());

            return;
        }

        log.debug("Executing job for dissemination id: {}...", disseminationId);
        try {
            scheduledGeoposDisseminationService.executeDissemination(disseminationId);
        } catch (Exception exception) {
            log.error("Failed to execute job", exception);
        }
    }
}
