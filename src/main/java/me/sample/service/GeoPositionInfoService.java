package me.sample.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import me.sample.domain.GeoPositionInfo;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface GeoPositionInfoService {

    Long countGeopositions(Specification<GeoPositionInfo> specification);

    Page<GeoPositionInfo> findGeopositions(Specification<GeoPositionInfo> specification, Pageable pageable);

    Optional<GeoPositionInfo> findGeoposition(UUID id);

    GeoPositionInfo saveGeoposition(GeoPositionInfo data);

    CompletableFuture<GeoPositionInfo> saveGeopositionAsync(GeoPositionInfo data);
}
