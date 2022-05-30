package me.sample.job;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.TerminalService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Компонент, выполняющий обновление города для терминалов
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Component
public class TerminalCityUpdateJob {

    TerminalService terminalService;

    @NonFinal
    @Value("${job.synchronization.terminal.city.enabled}")
    boolean enabled;

    @Scheduled(cron = "${job.synchronization.terminal.city.cron}")
    public void execute() {
        if (!enabled) {
            log.warn("Job is disabled (application.yml: job.synchronization.terminal.city.enabled: false)");

            return;
        }

        log.info("Performing terminal city synchronization...");
        try {
            terminalService.syncTerminalCities();
        } catch (Exception exception) {
            log.error("Failed to execute job", exception);
        }

        log.info("Performed terminal city synchronization");
    }
}
