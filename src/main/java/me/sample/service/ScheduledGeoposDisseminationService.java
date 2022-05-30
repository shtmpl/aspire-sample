package me.sample.service;

import me.sample.domain.ScheduledGeoposDissemination;
import me.sample.domain.Terminal;

import java.util.Optional;
import java.util.UUID;

public interface ScheduledGeoposDisseminationService {

    Optional<ScheduledGeoposDissemination> findPrioritisedDisseminationForTerminal(Terminal terminal, Double lat, Double lon);

    /**
     * Сохраняет новую рассылку
     */
    ScheduledGeoposDissemination saveDissemination(ScheduledGeoposDissemination data);

    ScheduledGeoposDissemination startDissemination(ScheduledGeoposDissemination found);

    ScheduledGeoposDissemination pauseDissemination(ScheduledGeoposDissemination found);

    ScheduledGeoposDissemination completeDissemination(ScheduledGeoposDissemination found);

    Optional<ScheduledGeoposDissemination> executeDissemination(UUID id);

    Optional<ScheduledGeoposDissemination> executeDisseminationForTerminal(Terminal terminal, Double lat, Double lon);

    Optional<UUID> deleteDissemination(UUID id);
}
