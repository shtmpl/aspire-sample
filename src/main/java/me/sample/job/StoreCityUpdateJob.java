package me.sample.job;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import me.sample.service.StoreService;

/**
 * Компонент, выполняющий обновление города для торговых точек
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Component
public class StoreCityUpdateJob {

    StoreService storeService;

    @Scheduled(cron = "${job.synchronization.store.city.cron}")
    public void execute() {
        log.info("Performing store city synchronization...");
        try {
            storeService.syncStoreCities();
        } catch (Exception exception) {
            log.error("Failed to execute job", exception);
        }

        log.info("Performed store city synchronization");
    }
}
