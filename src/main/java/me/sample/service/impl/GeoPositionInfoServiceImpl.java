package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.sample.service.GeoPositionInfoService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.GeoPositionInfo;
import me.sample.repository.GeoPositionInfoRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@CacheConfig(cacheNames = GeoPositionInfoServiceImpl.CACHE_NAME_GEOPOSITION)
@Transactional
@Service
public class GeoPositionInfoServiceImpl implements GeoPositionInfoService {

    public static final String CACHE_NAME_GEOPOSITION = "geoposinfo";

    GeoPositionInfoRepository geoPositionInfoRepository;

    @Override
    public Long countGeopositions(Specification<GeoPositionInfo> specification) {
        return geoPositionInfoRepository.count(specification);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<GeoPositionInfo> findGeopositions(Specification<GeoPositionInfo> specification, Pageable pageable) {
        return geoPositionInfoRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<GeoPositionInfo> findGeoposition(UUID id) {
        return geoPositionInfoRepository.findById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public GeoPositionInfo saveGeoposition(GeoPositionInfo data) {
        return geoPositionInfoRepository.save(data);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public CompletableFuture<GeoPositionInfo> saveGeopositionAsync(GeoPositionInfo data){
        return CompletableFuture.completedFuture(saveGeoposition(data));
    }
}
