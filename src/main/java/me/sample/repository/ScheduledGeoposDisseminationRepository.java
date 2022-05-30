package me.sample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import me.sample.domain.ScheduledGeoposDissemination;

import java.util.UUID;
import java.util.stream.Stream;

public interface ScheduledGeoposDisseminationRepository extends JpaRepository<ScheduledGeoposDissemination, UUID> {

    @Query("FROM ScheduledGeoposDissemination")
    Stream<ScheduledGeoposDissemination> findAsStreamAll();
}
