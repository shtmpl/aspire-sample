package me.sample.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.BankSynchronizationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Компонент, выполняющий синхронизацию данных
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class BankSynchronizationJob {

    private final BankSynchronizationService bankSynchronizationService;

    @Value("${job.synchronization.bank.enabled}")
    private boolean enabled;

    @Scheduled(cron = "${job.synchronization.bank.cron}")
    public void synchronize() {
        if (!enabled) {
            log.warn("Scheduled synchronization is disabled (application.yml: job.synchronization.bank.enabled: false)");

            return;
        }

        log.info("Performing scheduled synchronization...");
        bankSynchronizationService.syncResources();

        log.info("Performed scheduled synchronization");
    }
}
