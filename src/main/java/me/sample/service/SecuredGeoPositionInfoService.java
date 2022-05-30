package me.sample.service;

import me.sample.domain.GeoPositionInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.UUID;

public interface SecuredGeoPositionInfoService {

    Long countGeopositions();

    Page<GeoPositionInfo> findGeopositions(Pageable pageable);

    Page<GeoPositionInfo> findGeopositions(Specification<GeoPositionInfo> specification, Pageable pageable);

    Optional<GeoPositionInfo> findGeoposition(UUID id);
}
